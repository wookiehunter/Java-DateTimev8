import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class WorkPeriod implements Comparable<WorkPeriod>, Splittable<WorkPeriod> {

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private List<TaskPart> taskParts;

    static final Duration MINIMUM_DURATION = Duration.ofMinutes(5);

    private WorkPeriod(LocalDateTime startTime, LocalDateTime endTime, List<TaskPart> taskParts) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.taskParts = taskParts;
    }

    private WorkPeriod(LocalDateTime startTime, LocalDateTime endTime)  {
        this(startTime, endTime, new ArrayList<>());
    }

    public static WorkPeriod of(LocalDateTime start, LocalDateTime end, List<TaskPart> taskParts) {
        return new WorkPeriod(start, end, taskParts);
    }

    public static WorkPeriod of(LocalDateTime start, LocalDateTime end) {
        return new WorkPeriod(start, end);
    }

    public boolean contains(LocalDateTime ldt) {
        return ldt.isAfter(startTime) && ldt.isBefore(endTime);
    }

    LocalDateTime getStartTime() {
        return startTime;
    }

    LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
        String workPeriodHeader = "\n\tWork Period: " + timeFormatter.format(startTime) + " to " + timeFormatter.format(endTime);
        StringBuilder sb = new StringBuilder(workPeriodHeader);
        for (TaskPart t : taskParts) {
            sb.append("\n\t\t").append(t);
        }
        return sb.toString();
    }

    List<TaskPart> getTaskParts() {
        return taskParts;
    }

    public void populateTaskPartList(Deque<TaskPart> taskParts, LocalDateTime startTime, ZoneId timeZone) {
        Duration available = getEffectiveDuration(startTime, timeZone);
        TaskPart currentTaskPart = taskParts.poll();
        while (currentTaskPart != null && available.compareTo(currentTaskPart.getDuration()) >= 0) {
            addTaskPart(currentTaskPart);
            available = available.minus(currentTaskPart.getDuration());
            currentTaskPart = taskParts.poll();
        }
        if (currentTaskPart != null) {
            // not worth switching to a new task for just a few minutes
            if (available.compareTo(WorkPeriod.MINIMUM_DURATION) >= 0) {
                TaskPart tp2 = currentTaskPart.split(available);
                addTaskPart(currentTaskPart);
                taskParts.offerFirst(tp2);
            } else {
                taskParts.offerFirst(currentTaskPart);
            }
        }
    }

    Duration getEffectiveDuration(ZoneId zoneId) {
        return getEffectiveDuration(startTime, zoneId);
    }

    Duration getEffectiveDuration(LocalDateTime scheduleStart, ZoneId zoneId) {
        ZonedDateTime wpStartZdt = ZonedDateTime.of(startTime, zoneId);
        ZonedDateTime scheduleStartZdt = ZonedDateTime.of(scheduleStart, zoneId);
        ZonedDateTime effectiveStartTime = wpStartZdt.isAfter(scheduleStartZdt) ? wpStartZdt : scheduleStartZdt;

        ZonedDateTime endZdt = ZonedDateTime.of(endTime, zoneId);
        return Duration.between(effectiveStartTime, endZdt);
    }

    void addTaskPart(TaskPart taskPart) {
        taskParts.add(taskPart);
    }

    @Override
    public int compareTo(WorkPeriod otherWorkPeriod) {
        return startTime.compareTo(otherWorkPeriod.startTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkPeriod that = (WorkPeriod) o;

        return startTime.equals(that.startTime) && endTime.equals(that.endTime) && taskParts.equals(that.taskParts);
    }

    @Override
    public int hashCode() {
        int result = startTime.hashCode();
        result = 31 * result + endTime.hashCode();
        result = 31 * result + taskParts.hashCode();
        return result;
    }

    // WorkPeriods may need to be split on two occasions: during scheduling, when they are still empty
    // and can be split by Events; and during calendar display, when they have been populated and may
    // need to be split on a midnight.
    @Override
    public List<WorkPeriod> split(LocalDateTime splitTime, ZoneId ignore) {
        if (! (splitTime.isAfter(startTime) && splitTime.isBefore(endTime))) {
            return Arrays.asList(this);
        } else if (taskParts.isEmpty()) {
            return Arrays.asList(WorkPeriod.of(startTime, splitTime), WorkPeriod.of(splitTime, endTime));
        } else {
            WorkPeriod first = WorkPeriod.of(startTime, splitTime);
            Duration available = first.getEffectiveDuration(ZoneOffset.UTC);
            List<TaskPart> taskParts = new ArrayList<>(getTaskParts());
            TaskPart currentTaskPart = taskParts.remove(0);
            while (currentTaskPart != null && available.compareTo(currentTaskPart.getDuration()) >= 0) {
                first.addTaskPart(currentTaskPart);
                available = available.minus(currentTaskPart.getDuration());
                currentTaskPart = taskParts.isEmpty() ? null : taskParts.remove(0);
            }
            if (currentTaskPart != null) {
                // there isn't room for the whole of the current task part in the first split
                if (! available.isZero()) {
                    TaskPart tp2 = currentTaskPart.split(available);
                    first.addTaskPart(currentTaskPart);
                    taskParts.add(0, tp2);
                } else {
                    taskParts.add(0, currentTaskPart);
                }
            }
            WorkPeriod second = WorkPeriod.of(splitTime, endTime, taskParts);
            return Arrays.asList(first, second);
        }
    }

    @Override
    public LocalDateTime getLocalStartDateTime(ZoneId zone) {
        return startTime;
    }
}