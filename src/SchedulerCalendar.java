import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toCollection;

public class SchedulerCalendar {

    final private NavigableSet<WorkPeriod> workPeriods = new TreeSet<>(); // ordered by start time
    final private NavigableSet<Event> events = new TreeSet<>();           // ordered by start time
    final private List<Task> tasks = new ArrayList<>();                   // no natural order, user-ordered by priority

    public Schedule createSchedule(LocalDateTime scheduleStart, ZoneId zoneId) {
        ArrayList<WorkPeriod> overwrittenPeriods = new ArrayList<>(overwritePeriodsWithEvents(zoneId));
        boolean success = populatePeriods(overwrittenPeriods, tasks, scheduleStart, zoneId);
        return new Schedule(zoneId, overwrittenPeriods, events, success);
    }

    private boolean populatePeriods(List<WorkPeriod> periods, List<Task> tasks, LocalDateTime scheduleStart, ZoneId zoneId) {
        Deque<TaskPart> taskParts = tasks.stream()
                .map(TaskPart::wholeOf)
                .collect(Collectors.toCollection(ArrayDeque::new));
        periods.forEach(p -> p.populateTaskPartList(taskParts, scheduleStart, zoneId));
        return taskParts.isEmpty();
    }

    public Schedule createSchedule(LocalDate testDate, ZoneId zoneId) {
        return createSchedule(testDate.atStartOfDay(), zoneId);
    }

    NavigableSet<WorkPeriod> overwritePeriodsWithEvents(ZoneId zone) {
        //TODO (maybe) save overwritePeriodsByEvents from having to consider periods and events in the past
        NavigableSet<WorkPeriod> overwrittenPeriods = new TreeSet<>();
        WorkPeriod period = workPeriods.isEmpty() ? null : workPeriods.first();
        NavigableSet<Event> unscheduledEvents = new TreeSet<>(events);
        Event event = unscheduledEvents.pollFirst();
        while (period != null && event != null) {
            if (!period.getEndTime().isAfter(event.getLocalStartDateTime(zone))) {
                // non-overlapping, period first
                overwrittenPeriods.add(period);
                period = workPeriods.higher(period);
            } else if (!period.getStartTime().isBefore(event.getLocalEndDateTime(zone))) {
                // non-overlapping, event first
                event = unscheduledEvents.higher(event);
            } else if (period.getStartTime().isBefore(event.getLocalStartDateTime(zone))) {
                // overlapping, period starts first
                List<WorkPeriod> split = period.split(event.getLocalStartDateTime(zone), zone);
                overwrittenPeriods.add(split.get(0));
                period = split.get(1);
            } else if (period.getEndTime().isAfter(event.getLocalEndDateTime(zone))) {
                // overlapping, event starts first or at same time
                period = period.split(event.getLocalEndDateTime(zone), zone).get(1);
                event = unscheduledEvents.higher(event);
            } else {
                // event encloses period
                period = workPeriods.higher(period);
            }
        }
        if (period != null) {
            overwrittenPeriods.add(period);
            overwrittenPeriods.addAll(workPeriods.tailSet(period));
        }
        return overwrittenPeriods;
    }

    public SchedulerCalendar addWorkPeriod(WorkPeriod p) {
        WorkPeriod preceding = workPeriods.floor(p);
        WorkPeriod following = workPeriods.ceiling(p);
        if (preceding != null && !preceding.getEndTime().isBefore(p.getStartTime())) {
            throw new IllegalArgumentException("Work Periods cannot overlap: " + preceding + "," + p);
        } else if (following != null && !following.getStartTime().isAfter(p.getEndTime())) {
            throw new IllegalArgumentException("Work Periods cannot overlap: " + p + "," + following);
        }
        workPeriods.add(p);
        return this;
    }

    public SchedulerCalendar addWorkPeriods(List<WorkPeriod> periods) {
        for (WorkPeriod wp : periods) {
            addWorkPeriod(wp);
        }
        return this;
    }

    public SchedulerCalendar addTask(String description, int hours, int minutes) {
        addTask(new Task(hours, minutes, description));
        return this;
    }

    public SchedulerCalendar addTask(Task task) {
        tasks.add(task);
        return this;
    }

    public SchedulerCalendar addEvent(Event e) {
        events.add(e);
        return this;
    }

    public SchedulerCalendar addEvent(ZonedDateTime eventDateTime, Duration duration, String description) {
        addEvent(Event.of(eventDateTime, eventDateTime.plus(duration), description));
        return this;
    }
}