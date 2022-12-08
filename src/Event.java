import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

public class Event implements Comparable<Event>, Splittable<Event> {

    private final ZonedDateTime startTime;
    private final ZonedDateTime endTime;
    private final String description;

    private Event(ZonedDateTime startTime, ZonedDateTime endTime, String description) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
    }

    public static Event of(ZonedDateTime startTime, ZonedDateTime endTime, String description) {
        return new Event(startTime, endTime, description);
    }

    public static Event of(ZonedDateTime startTime, Duration duration, String description) {
        return new Event(startTime, startTime.plus(duration), description);
    }

    @Override
    public int compareTo(Event e) {
        return startTime.toInstant().compareTo(e.startTime.toInstant());
    }

    public LocalDateTime getLocalStartDateTime(ZoneId zone) {
        return startTime.withZoneSameInstant(zone).toLocalDateTime();
    }

    public LocalDateTime getLocalEndDateTime(ZoneId zone) {
        return endTime.withZoneSameInstant(zone).toLocalDateTime();
    }

    public Event withStartTime(ZonedDateTime startTime) {
        return Event.of(startTime, endTime, description);
    }

    public Event withEndTime(ZonedDateTime endTime) {
        return Event.of(startTime, endTime, description);
    }

    // For use in displaying a schedule
    public String toString(ZoneId zone) {
        Duration dur = Duration.between(startTime, endTime);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
        return ("\n\t" + description + ": " + timeFormatter.format(startTime.withZoneSameInstant(zone)) + ", duration = " + Utils.formatDuration(dur));
    }

    public List<Event> split(LocalDateTime ldtSplitTime, ZoneId zone) {
        ZonedDateTime zdtSplitTime = ZonedDateTime.of(ldtSplitTime, zone);
        if (zdtSplitTime.isAfter(startTime) && zdtSplitTime.isBefore(endTime)) {
            return Arrays.asList(Event.of(startTime, zdtSplitTime, description), Event.of(zdtSplitTime, endTime, description));
        } else {
            return Arrays.asList(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Event event = (Event) o;

        if (!startTime.equals(event.startTime))
            return false;
        if (!endTime.equals(event.endTime))
            return false;
        return description.equals(event.description);
    }

    @Override
    public int hashCode() {
        int result = startTime.hashCode();
        result = 31 * result + endTime.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Event{" + "startTime=" + startTime + ", endTime=" + endTime + ", description='" + description + '\'' + '}';
    }
}
