import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import rx.Observer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@FunctionalInterface
public interface RestClientTask<T> {

    Logger logger = LoggerFactory.getLogger(RestClientTask.class);

    int BUFFER_SECONDS = 1;

    T getSubject();

    default void submitWith(ScheduledExecutorService executorService, Observer<T> observer) {
        StackTraceElement[] localSideStackTrace = Thread.currentThread().getStackTrace();
        executorService.submit(new Runnable() {
            public void run() {
                T subject;
                try {
                    subject = getSubject();
                } catch (Throwable e) {
                    fixRemoteStackTrace(e, localSideStackTrace);
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
                    fixRemoteStackTrace(e, localSideStackTrace);
                    logger.error("Task failed", e);
                }
            }
        });
    }

    /**
     * This method changes the given remote cause, and it adds the also given local stacktrace.<br/>
     * If the remoteCause is an {@link java.util.concurrent.ExecutionException} and it has a non-null inner
     * cause, this inner cause is unwrapped and the local stacktrace and exception message are added to the
     * that instead of the given remoteCause itself.
     *
     * <a href="https://github.com/hazelcast/hazelcast/blob/7f8cd30e4e445473271d2e434ad939d156a151ca/hazelcast/src/main/java/com/hazelcast/util/ExceptionUtil.java#L129">Code is cortesy of Hazelcast</a>
     *
     * @param remoteCause         the remotely generated exception
     * @param localSideStackTrace the local stacktrace to add to the exception stacktrace
     */
    static void fixRemoteStackTrace(Throwable remoteCause, StackTraceElement[] localSideStackTrace) {
        Throwable throwable = remoteCause;
        if (remoteCause instanceof ExecutionException && throwable.getCause() != null) {
            throwable = throwable.getCause();
        }

        StackTraceElement[] remoteStackTrace = throwable.getStackTrace();
        StackTraceElement[] newStackTrace = new StackTraceElement[localSideStackTrace.length + remoteStackTrace.length];
        System.arraycopy(remoteStackTrace, 0, newStackTrace, 0, remoteStackTrace.length);
        newStackTrace[remoteStackTrace.length] = new StackTraceElement("------ End remote and begin local stack-trace ------", "", null, -1);
        System.arraycopy(localSideStackTrace, 1, newStackTrace, remoteStackTrace.length + 1, localSideStackTrace.length - 1);
        throwable.setStackTrace(newStackTrace);
    }

}
