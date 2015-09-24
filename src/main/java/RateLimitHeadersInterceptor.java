import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.List;

public class RateLimitHeadersInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitHeadersInterceptor.class);

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse response = execution.execute(request, body);

        HttpHeaders headers = response.getHeaders();
        List<String> limit = headers.get("x-rate-limit-limit");
        List<String> remaining = headers.get("x-rate-limit-remaining");
        List<String> reset = headers.get("x-rate-limit-reset");
        if (limit != null && !limit.isEmpty() && remaining != null && !remaining.isEmpty() && reset != null && !reset.isEmpty()) {
            logger.debug("limit={}, remaining={}, reset={}", limit.get(0), remaining.get(0), reset.get(0));
        }

        return response;
    }

}
