package electrit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitsScoreborad {

    private Map<String, RateLimitInfo> rateLimits = new ConcurrentHashMap<>();

    public RateLimitInfo getRateLimitInfo(String resourceFamily) {
        return rateLimits.get(resourceFamily);
    }

    public void setRateLimitInfo(String resourceFamily, RateLimitInfo rateLimitInfo) {
        rateLimits.put(resourceFamily, rateLimitInfo);
    }

}
