import java.time.*;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Stream;

import static java.time.DayOfWeek.*;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;

public class WorkPeriods {
    public static final LocalTime AM_START_TIME = LocalTime.of(9,5);
    public static final LocalTime PM_START_TIME = LocalTime.of(13, 30);
    public static final Duration WORK_PERIOD_LENGHT = Duration.ofHours(3).plusMinutes(30);
//    private static final LocalTime PM_START_TIME = AM_START_TIME.plusHours(9);

    public  static  WorkPeriod createMorningWorkPeriod(LocalDate date) {
        LocalDateTime startDateTime = LocalDateTime.of(date, AM_START_TIME);
        LocalDateTime endDateTime = startDateTime.plus(WORK_PERIOD_LENGHT);

        return WorkPeriod.of(startDateTime, endDateTime);
    }

    public  static  WorkPeriod createAfternoonWorkPeriod(LocalDate date) {
        LocalDateTime startDateTime = LocalDateTime.of(date, PM_START_TIME);
        LocalDateTime endDateTime = startDateTime.plus(WORK_PERIOD_LENGHT);

        return WorkPeriod.of(startDateTime, endDateTime);
    }

    public static List<WorkPeriod> generateWorkPeriods(LocalDate startDate, int dayCount) {
        List<LocalDate> workingDays = generateWorkingDays(startDate, dayCount);
        return generateWorkPeriods(workingDays);
    }

    private static List<WorkPeriod> generateWorkPeriods(List<LocalDate> workingDays) {
        return workingDays.stream()
                .flatMap(d -> Stream.of(createMorningWorkPeriod(d), createAfternoonWorkPeriod(d)))
                .collect(toList());
    }

    private static List<LocalDate> generateWorkingDays(LocalDate startDate, int dayCount) {
        return Stream.iterate(startDate, d -> d.with(nextWorkingDayAdjuster))
                .limit(dayCount)
                .collect(toList());
    }

    private final static TemporalAdjuster nextWorkingDayAdjuster =
            d -> DayOfWeek.from(d) != FRIDAY
                    ? d.plus(1, DAYS)
                    : d.with(TemporalAdjusters.next(MONDAY));

    private static boolean isWorkingDay(LocalDate d) {
        DayOfWeek dayOfWeek = d.getDayOfWeek();
        return ! (dayOfWeek == SATURDAY || dayOfWeek == SUNDAY);
    }

    public static void main(String[] args) {
        List<WorkPeriod> workPeriods = generateWorkPeriods(LocalDate.ofEpochDay(0), 3);
        workPeriods.forEach(System.out::println);
    }
}
