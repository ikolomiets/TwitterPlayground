import java.io.IOException;

public class RateLimitException extends IOException {

    private final RateLimitInfo rateLimitInfo;

    public RateLimitException(RateLimitInfo rateLimitInfo) {
        super(rateLimitInfo.toString());
        this.rateLimitInfo = rateLimitInfo;
    }

    public RateLimitInfo getRateLimitInfo() {
        return rateLimitInfo;
    }

}
