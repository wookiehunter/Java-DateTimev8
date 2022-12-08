import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        Clock testClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

        SchedulerCalendar calendar = new SchedulerCalendar();

        calendar.addTask("answer email", 1, 0);
        calendar.addTask("write security report", 4, 0);
        calendar.addTask("write balloons report", 4, 0);


        LocalDate clockDate = LocalDate.now(testClock);
        LocalDate mondayStart = clockDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        List<WorkPeriod> periods = WorkPeriods.generateWorkPeriods(LocalDate.now(testClock), 3);
        calendar.addWorkPeriods(periods);

        ZonedDateTime meetingStartTime = ZonedDateTime.of(mondayStart, LocalTime.of(7, 30), ZoneId.of("America/New_York"));
        Event nyPlanningMeeting = Event.of(meetingStartTime, Duration.ofHours(1), "New York Planning");
        calendar.addEvent(nyPlanningMeeting);

        Schedule schedule = calendar.createSchedule(mondayStart, ZoneId.of("Europe/London"));

        System.out.println(schedule);
    }
}
