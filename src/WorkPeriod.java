import java.time.LocalDateTime;

public class WorkPeriod {
    private final LocalDateTime start;
    private final LocalDateTime end;

    public WorkPeriod(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public static WorkPeriod of(LocalDateTime start, LocalDateTime end) {
        return new WorkPeriod(start, end);
    }
}
