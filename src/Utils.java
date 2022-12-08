import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Stream;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;

public class Utils {

/*
	public static List<Event> generateStandups(LocalDateTime start, int dayCount, Duration dur, ZoneId zone) {
		List<LocalDate> workingDays = generateWorkingDays(start.toLocalDate(), dayCount);
		return workingDays.stream()
				.map(d -> ZonedDateTime.of(d, start.toLocalTime(), zone))
				.map(zonedDt -> new Event(zonedDt, dur, "standup"))
				.collect(toList());
	}
*/

	private static boolean isWorkingDay(LocalDate d) {
		return d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY;
	}

	public static List<LocalDate> generateWorkingDays(LocalDate startDate, int dayCount) {
		return Stream.iterate(startDate, d -> d.plusDays(1))
				.filter(Utils::isWorkingDay)
				.limit(dayCount)
				.collect(toList());
	}

	// alternative implementation of generateWorkingDays
    // ************

	public static List<LocalDate> generateWorkingDays_alternative_implementation(LocalDate startDate, int dayCountInclusive) {
		return Stream.iterate(startDate, d -> d.with(nextWorkingDayAdjuster))
				.limit(dayCountInclusive)
				.collect(toList());
	}

	private static TemporalAdjuster nextWorkingDayAdjuster =
		t -> LocalDate.from(t).getDayOfWeek() == FRIDAY
				? t.with(TemporalAdjusters.next(MONDAY))
				: t.plus(1,DAYS);

	// ************
	// end alternative implementation of generateWorkingDays

	static String formatDuration(Duration d) {
		long hours = d.toHours();
		String hoursString = hours == 0 ? "" : hours + (hours == 1 ? "hr " : "hrs ");
		long minutes = d.minusHours(hours).toMinutes();
		return hoursString + minutes + "mins";
	}
}
