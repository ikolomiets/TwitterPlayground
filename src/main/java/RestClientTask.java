import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import rx.Observer;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@FunctionalInterface
public interface RestClientTask<T> {

    Logger logger = LoggerFactory.getLogger(RestClientTask.class);

    int BUFFER_SECONDS = 1;

    T getSubject();

    default void submitWith(ScheduledExecutorService executorService, Observer<T> observer) {
        executorService.submit(new Runnable() {
            public void run() {
                T subject;
                try {
                    subject = getSubject();
                } catch (Throwable e) {
                    if (e instanceof RestClientException) {
                        Throwable cause = e.getCause();
                        if (cause instanceof RateLimitException) {
                            RateLimitException rateLimitException = (RateLimitException) cause;
                            RateLimitInfo rateLimitInfo = rateLimitException.getRateLimitInfo();
                            long delay = rateLimitInfo.getResetMillis() - System.currentTimeMillis();

                            // increase exact delay by few more seconds to be sure Twitter will have updated reset time
                            delay += BUFFER_SECONDS * 1000;

                            logger.debug("Re-try task in {}ms (at {} + {}sec) due to rate limit", delay, rateLimitInfo.getResetDateTimeStr(), BUFFER_SECONDS);
                            executorService.schedule(this, delay, TimeUnit.MILLISECONDS);
                        } else if (cause instanceof ProtectedUserAccountException) {
                            logger.warn("Protected user account: {}", cause.getMessage());
                            observer.onError(cause);
                        } else {
                            observer.onError(e);
                        }
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
