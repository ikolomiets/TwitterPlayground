import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RateLimitInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private static final Pattern RESOURCE_FAMILY_PATTERN = Pattern.compile("^/\\d+\\.\\d+/(.+)/.+$");

    private final RateLimitsScoreborad rateLimitsScoreborad;

    public RateLimitInterceptor(RateLimitsScoreborad rateLimitsScoreborad) {
        this.rateLimitsScoreborad = rateLimitsScoreborad;
    }

    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String resourceFamily = parseResourceFamily(request);
        RateLimitInfo rateLimitInfo = null;
        if (resourceFamily != null) {
            rateLimitInfo = rateLimitsScoreborad.getRateLimitInfo(resourceFamily);
            if (rateLimitInfo != null && !rateLimitInfo.isAllowed()) {
                throw new RateLimitException(rateLimitInfo);
            }
        } else {
            logger.warn("Unable to parse resource family in the URI path: {}", request.getURI().getPath());
        }

        ClientHttpResponse response = execution.execute(request, body);

        if (resourceFamily != null) {
            HttpHeaders headers = response.getHeaders();
            List<String> limitValues = headers.get("x-rate-limit-limit");
            List<String> remainingValues = headers.get("x-rate-limit-remaining");
            List<String> resetValues = headers.get("x-rate-limit-reset");
            if (limitValues != null && !limitValues.isEmpty() && remainingValues != null && !remainingValues.isEmpty() && resetValues != null && !resetValues.isEmpty()) {
                int limit = Integer.parseInt(limitValues.get(0));
                int remaining = Integer.parseInt(remainingValues.get(0));
                long reset = Long.parseLong(resetValues.get(0));

                RateLimitInfo newRateLimitInfo = new RateLimitInfo(limit, remaining, reset);
                logger.debug("{}", newRateLimitInfo);

                rateLimitsScoreborad.setRateLimitInfo(resourceFamily, newRateLimitInfo);

                if (response.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    if (rateLimitInfo != null) {
                        logger.warn("Unexpected rate limit error response: before: {}; after: {}", rateLimitInfo, newRateLimitInfo);
                    }
                    throw new RateLimitException(newRateLimitInfo);
                }
            } else {
                logger.warn("Bad rate-limit headers in response: {} - {}, limit={}, remaining={}, reset={}",
                        response.getStatusCode(), response.getStatusText(), limitValues, remainingValues, resetValues);
            }
        }

        return response;
    }

    private String parseResourceFamily(HttpRequest request) {
        String resourceFamily = null;
        Matcher matcher = RESOURCE_FAMILY_PATTERN.matcher(request.getURI().getPath());
        if (matcher.matches()) {
            resourceFamily = matcher.group(1);
            logger.debug("rate limit resource family: '{}'", resourceFamily);
        }
        return resourceFamily;
    }

}