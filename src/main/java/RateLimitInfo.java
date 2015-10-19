import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RateLimitInfo {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss z");

    private final int limit;
    private final int remaining;
    private final long reset;

    public RateLimitInfo(int limit, int remaining, long reset) {
        this.limit = limit;
        this.remaining = remaining;
        this.reset = reset;
    }

    public int getLimit() {
        return limit;
    }

    public int getRemaining() {
        return remaining;
    }

    public long getResetMillis() {
        return reset * 1000;
    }

    public Date getResetDateTime() {
        return new Date(getResetMillis());
    }

    public String getResetDateTimeStr() {
        return DATE_FORMAT.format(getResetDateTime());
    }

    public boolean isAllowed() {
        return remaining > 0 || getResetMillis() < System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "RateLimitInfo{" +
                "limit=" + limit +
                ", remaining=" + remaining +
                ", reset=" + reset + " (" + getResetDateTimeStr() + ")" +
                '}';
    }
}
