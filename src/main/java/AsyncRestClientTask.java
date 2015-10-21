import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import rx.Observer;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@FunctionalInterface
public interface AsyncRestClientTask<T> {

    Logger logger = LoggerFactory.getLogger(AsyncRestClientTask.class);

    int BUFFER_SECONDS = 1;

    T getSubject() throws RestClientException;

    default void submitWith(ScheduledExecutorService executorService, Observer<T> observer) {
        executorService.submit(new Runnable() {
            public void run() {
                T subject;
                try {
                    subject = getSubject();
                } catch (RestClientException e) {
                    if (e.getCause() instanceof RateLimitException) {
                        RateLimitException rateLimitException = (RateLimitException) e.getCause();
                        RateLimitInfo rateLimitInfo = rateLimitException.getRateLimitInfo();
                        long delay = rateLimitInfo.getResetMillis() - System.currentTimeMillis();

                        // increase exact delay by few more seconds to be sure Twitter will have updated reset time
                        delay += BUFFER_SECONDS * 1000;

                        logger.debug("Re-try task in {}ms (at {} + {}sec) due to rate limit", delay, rateLimitInfo.getResetDateTimeStr(), BUFFER_SECONDS);
                        executorService.schedule(this, delay, TimeUnit.MILLISECONDS);
                    } else {
                        observer.onError(e);
                    }
                    return;
                }

                try {
                    observer.onNext(subject);
                    observer.onCompleted();
                } catch (Throwable e) {
                    logger.error("Task failed", e);
                }
            }
        });
    }

}
