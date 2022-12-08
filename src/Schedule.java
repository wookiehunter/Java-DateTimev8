import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class Schedule {

	private final ZoneId zoneId;
	private final List<WorkPeriod> scheduledPeriods;
	private final NavigableSet<Event> events;
	private final boolean successful;

	public Schedule(ZoneId zoneId, List<WorkPeriod> scheduledPeriods, NavigableSet<Event> events, boolean success) {
		this.zoneId = zoneId;
		this.scheduledPeriods = scheduledPeriods;
		this.events = events;
		this.successful = success;
	}

	@Override
	public String toString() {

		if (!successful) return "Schedule unsuccessful: insufficent time for tasks";

		MidnightSplitter midnightSplitter = new MidnightSplitter();

		Collection<WorkPeriod> printablePeriods = scheduledPeriods.stream()
				.flatMap(p -> midnightSplitter.splitAtAllMidnights(p, zoneId))
				.collect(toList());
		Map<LocalDateTime, String> dateTimeToPeriodOutput = printablePeriods.stream()
				.collect(groupingBy(WorkPeriod::getStartTime, mapping(WorkPeriod::toString, joining())));

		List<Event> printableEvents = events.stream()
				.flatMap(e -> midnightSplitter.splitAtAllMidnights(e, zoneId))
				.collect(toList());
		Map<LocalDateTime, String> dateTimeToEventOutput = printableEvents.stream()
				.collect(groupingBy(e -> e.getLocalStartDateTime(zoneId), mapping(e -> e.toString(zoneId), joining())));

		Map<LocalDateTime, String> dateTimeToObjectOutput = new HashMap<>(dateTimeToPeriodOutput);
		dateTimeToObjectOutput.putAll(dateTimeToEventOutput);

		NavigableMap<LocalDate, String> dateToCalendarObjectOutput = dateTimeToObjectOutput.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(groupingBy(e -> e.getKey().toLocalDate(), TreeMap::new, mapping(Map.Entry::getValue,joining())));

		StringBuilder sb = new StringBuilder();

		for (LocalDate date : dateToCalendarObjectOutput.keySet()) {
			sb.append("\n");
			sb.append(date);
			sb.append(dateToCalendarObjectOutput.get(date));
		}

		return sb.toString();
	}

	List<WorkPeriod> getScheduledPeriods() {
		return scheduledPeriods;
	}

	boolean isSuccessful() {
		return successful;
	}

	static class MidnightSplitter {

		// Method to assist displaying a schedule by the day. It breaks a Splittable at midnight local time (using
		// the supplied zone, if necessary), breaking it repeatedly if the Splittable spans more than one midnight.
		public <E extends Splittable<E>> Stream<E> splitAtAllMidnights(E e, ZoneId zone) {
			LocalDateTime currentMidnight = e.getLocalStartDateTime(zone).truncatedTo(ChronoUnit.DAYS);
			Deque<E> splittingResult = new ArrayDeque<>();
			splittingResult.add(e);
			List<E> lastElementSplit;   // this List will contain one or two elements, depending on whether
			// the last element in the Dequeu spans midnight local time
			do  {
				E currentElement = splittingResult.pollLast();
				currentMidnight = currentMidnight.plusDays(1);
				lastElementSplit = currentElement.split(currentMidnight, zone);
				splittingResult.addAll(lastElementSplit);
			} while (lastElementSplit.size() != 1);
			return splittingResult.stream();
		}

	}
}
