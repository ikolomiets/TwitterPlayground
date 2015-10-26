import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import rx.Observable;
import rx.Subscriber;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.*;

@Component
public class RxTwitterClient {

    private static final Logger logger = LoggerFactory.getLogger(RxTwitterClient.class);

    private static final int BUFFER_SECONDS = 1;

    private final TwitterClient twitterClient;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "executor-service"));

    @Autowired
    public RxTwitterClient(TwitterClient twitterClient) {
        this.twitterClient = twitterClient;
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        logger.debug("Shutting down RxTwitterClient");

        List<Runnable> runnables = executorService.shutdownNow();
        logger.debug("executorService shutdown: {} active task cancelled", runnables.size());
    }

    Observable<User> showUserById(long id) {
        return Observable.create(subscriber -> {
            Callable<User> showUserTask = () -> twitterClient.showUserById(id);
            submitRestClientTask(showUserTask, subscriber);
        });
    }

    public Observable<Long> getFollowersByUserId(long userId) {
        CursoredResultTaskFactory getFollowersTaskFactory = (cursor) -> () -> twitterClient.getFollowersByUserId(userId, cursor);
        return createObservableForCursoredResult(getFollowersTaskFactory);
    }

    public Observable<Long> getFriendsByUserId(long userId) {
        CursoredResultTaskFactory getFriendsTaskFactory = (cursor) -> () -> twitterClient.getFriendsByUserId(userId, cursor);
        return createObservableForCursoredResult(getFriendsTaskFactory);
    }

    private Observable<Long> createObservableForCursoredResult(CursoredResultTaskFactory cursoredResultTaskFactory) {
        return Observable.create(subscriber -> {
            Callable<CursoredResult> getCursoredResultTask = cursoredResultTaskFactory.createRestClientTask(null);
            submitRestClientTask(getCursoredResultTask, new Subscriber<CursoredResult>() {
                @Override
                public void onCompleted() {
                    // do nothing
                }

                @Override
                public void onError(Throwable e) {
                    if (!subscriber.isUnsubscribed())
                        subscriber.onError(e);
                }

                @Override
                public void onNext(CursoredResult cursoredResult) {
                    if (cursoredResult.getIds() != null) {
                        for (Long id : cursoredResult.getIds()) {
                            if (subscriber.isUnsubscribed()) {
                                return;
                            }
                            subscriber.onNext(id);
                        }
                    }

                    if (subscriber.isUnsubscribed())
                        return;

                    if (cursoredResult.isLast()) {
                        subscriber.onCompleted();
                    } else {
                        Callable<CursoredResult> getNextCursoredResultTask = cursoredResultTaskFactory.createRestClientTask(cursoredResult.getNextCursorStr());
                        submitRestClientTask(getNextCursoredResultTask, this);
                    }
                }
            });
        });
    }

    @FunctionalInterface
    private interface CursoredResultTaskFactory {

        Callable<CursoredResult> createRestClientTask(String cursor);

    }

    private <T> void submitRestClientTask(Callable<T> restClientTask, Subscriber<? super T> subscriber) {
        StackTraceElement[] localSideStackTrace = Thread.currentThread().getStackTrace();
        executorService.submit(new Runnable() {
            public void run() {
                if (subscriber.isUnsubscribed())
                    return;

                T result;
                try {
                    result = restClientTask.call();
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
                            subscriber.onError(cause);
                        } else {
                            subscriber.onError(e);
                        }
                    } else {
                        subscriber.onError(e);
                    }
                    return;
                }

                try {
                    subscriber.onNext(result);
                    if (!subscriber.isUnsubscribed())
                        subscriber.onCompleted();
                } catch (Throwable e) {
                    fixRemoteStackTrace(e, localSideStackTrace);
                    logger.error("Task failed", e);
                }
            }
        });
    }

    /**
     * This method changes the given remote cause, and it adds the also given local stacktrace.<br/>
     * If the remoteCause is an {@link ExecutionException} and it has a non-null inner
     * cause, this inner cause is unwrapped and the local stacktrace and exception message are added to the
     * that instead of the given remoteCause itself.
     *
     * <a href="https://github.com/hazelcast/hazelcast/blob/master/hazelcast/src/main/java/com/hazelcast/util/ExceptionUtil.java">Code is cortesy of Hazelcast</a>
     *
     * @param remoteCause         the remotely generated exception
     * @param localSideStackTrace the local stacktrace to add to the exception stacktrace
     */
    private static void fixRemoteStackTrace(Throwable remoteCause, StackTraceElement[] localSideStackTrace) {
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
