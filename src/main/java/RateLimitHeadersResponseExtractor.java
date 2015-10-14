import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseExtractor;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class RateLimitHeadersResponseExtractor<T> extends AbstractDelegatingResponseExtractor<T> {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitHeadersResponseExtractor.class);

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public RateLimitHeadersResponseExtractor(ResponseExtractor<T> delegate) {
        super(delegate);
    }

    @Override
    protected void doExtractData(ClientHttpResponse response) {
        HttpHeaders headers = response.getHeaders();
        List<String> limit = headers.get("x-rate-limit-limit");
        List<String> remaining = headers.get("x-rate-limit-remaining");
        List<String> reset = headers.get("x-rate-limit-reset");
        if (limit != null && !limit.isEmpty() && remaining != null && !remaining.isEmpty() && reset != null && !reset.isEmpty()) {
            logger.debug("limit={}, remaining={}, reset={} ({})", limit.get(0), remaining.get(0), reset.get(0), DATE_FORMAT.format(new Date(Long.parseLong(reset.get(0)) * 1000)));
        }
    }

}
