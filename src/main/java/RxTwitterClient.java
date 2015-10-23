import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;
import rx.Subscriber;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class RxTwitterClient {

    private static final Logger logger = LoggerFactory.getLogger(RxTwitterClient.class);

    private final TwitterClient twitterClient;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "executor-service"));

    @Autowired
    public RxTwitterClient(TwitterClient twitterClient) {
        this.twitterClient = twitterClient;
    }

    Observable<User> showUserById(long id) {
        return Observable.create(subscriber -> {
            RestClientTask<User> showUserTask = () -> twitterClient.showUserById(id);
            showUserTask.submitWith(executorService, subscriber);
        });
    }

    public Observable<Long> getFollowersByUserId(long userId) {
        return Observable.create(subscriber -> {
            RestClientTask<CursoredResult> getFollowersTask = () -> twitterClient.getFollowersByUserId(userId, null);
            getFollowersTask.submitWith(executorService, new Subscriber<CursoredResult>() {
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
                        RestClientTask<CursoredResult> getNextFollowersTask = () -> twitterClient.getFollowersByUserId(userId, cursoredResult.getNextCursorStr());
                        getNextFollowersTask.submitWith(executorService, this);
                    }
                }
            });
        });
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        logger.debug("Shutting down RxTwitterClient");

        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(1, TimeUnit.MINUTES);
        logger.debug("executorService terminated? {}", terminated);
    }

}
