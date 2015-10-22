import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import rx.Observer;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Component
public class TwitterClient {

    private static final Logger logger = LoggerFactory.getLogger(TwitterClient.class);

    private static final String URL_SHOW_USER_BY_ID = "https://api.twitter.com/1.1/users/show.json?include_entities=false&user_id={user_id}";
    private static final String URL_FOLLOWERS_BY_ID = "https://api.twitter.com/1.1/followers/ids.json?cursor={cursor}&user_id={user_id}";

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "executor-service"));

    @Autowired
    private RestTemplate restTemplate;

    public User showUserById(long id) {
        return restTemplate.getForObject(URL_SHOW_USER_BY_ID, User.class, Long.toUnsignedString(id));
    }

    public void showUserById(long id, Observer<User> observer) {
        RestClientTask<User> showUserTask = () -> showUserById(id);
        showUserTask.submitWith(executorService, observer);
    }

    public CursoredResult getFollowersByUserId(long userId, String cursor) {
        return restTemplate.getForObject(URL_FOLLOWERS_BY_ID, CursoredResult.class, cursor == null ? "-1" : cursor, Long.toUnsignedString(userId));
    }

    public void getFollowersByUserId(long userId, Observer<Long> observer) {
        RestClientTask<CursoredResult> getFollowersTask = () -> getFollowersByUserId(userId, (String) null);
        getFollowersTask.submitWith(executorService, new Observer<CursoredResult>() {
            @Override
            public void onCompleted() {
                // do nothing
            }

            @Override
            public void onError(Throwable e) {
                observer.onError(e);
            }

            @Override
            public void onNext(CursoredResult cursoredResult) {
                if (cursoredResult.getIds() != null)
                    cursoredResult.getIds().forEach(observer::onNext);

                if (cursoredResult.isLast()) {
                    observer.onCompleted();
                } else {
                    RestClientTask<CursoredResult> getNextFollowersTask = () -> getFollowersByUserId(userId, cursoredResult.getNextCursorStr());
                    getNextFollowersTask.submitWith(executorService, this);
                }
            }
        });
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        logger.debug("Shutting down TwitterClient");

        executorService.shutdown();
        boolean terminated = executorService.awaitTermination(1, TimeUnit.MINUTES);
        logger.debug("executorService terminated? {}", terminated);
    }

}
