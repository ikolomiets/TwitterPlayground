import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class TwitterClient {

    private static final String URL_SHOW_USER_BY_ID = "https://api.twitter.com/1.1/users/show.json?include_entities=false&user_id={user_id}";
    private static final String URL_FOLLOWERS_BY_ID = "https://api.twitter.com/1.1/followers/ids.json?cursor={cursor}&user_id={user_id}";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AsyncRestTemplate asyncRestTemplate;

    public User showUserById(long id) {
        return restTemplate.getForObject(URL_SHOW_USER_BY_ID, User.class, Long.toUnsignedString(id));
    }

    public void showUserByIdAsync(long id, ListenableFutureCallback<ResponseEntity<User>> callback) {
        ListenableFuture<ResponseEntity<User>> userFuture = asyncRestTemplate.getForEntity(URL_SHOW_USER_BY_ID, User.class, Long.toUnsignedString(id));
        userFuture.addCallback(callback);
    }

    public void getFollowersByUserId(long userId, String cursor, ListenableFutureCallback<ResponseEntity<CursoredResult>> callback) {
        ListenableFuture<ResponseEntity<CursoredResult>> future = asyncRestTemplate.getForEntity(URL_FOLLOWERS_BY_ID,
                CursoredResult.class,
                cursor == null ? "-1" : cursor,
                Long.toUnsignedString(userId)
        );
        future.addCallback(callback);
    }

    public List<Long> getFollowersByUserId(final long userId) throws Exception {
        final List<Long> followers = new ArrayList<Long>();

        final CountDownLatch complete = new CountDownLatch(1);
        final AtomicReference<Throwable> error = new AtomicReference<Throwable>();

        getFollowersByUserId(userId, null, new ListenableFutureCallback<ResponseEntity<CursoredResult>>() {
            public void onSuccess(ResponseEntity<CursoredResult> result) {
                CursoredResult cursoredResult = result.getBody();

                synchronized (followers) {
                    followers.addAll(cursoredResult.getIds());
                }

                if (cursoredResult.getNextCursor() != null)
                    getFollowersByUserId(userId, cursoredResult.getNextCursor(), this);
                else
                    complete.countDown();
            }

            public void onFailure(Throwable ex) {
                error.set(ex);
                complete.countDown();
            }
        });

        complete.await();

        //noinspection ThrowableResultOfMethodCallIgnored
        if (error.get() != null)
            throw new Exception("Unable to get followers", error.get());

        return followers;
    }

}
