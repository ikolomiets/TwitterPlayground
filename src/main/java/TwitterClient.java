import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import rx.Observer;


@Component
public class TwitterClient {

    private static final String URL_SHOW_USER_BY_ID = "https://api.twitter.com/1.1/users/show.json?include_entities=false&user_id={user_id}";
    private static final String URL_FOLLOWERS_BY_ID = "https://api.twitter.com/1.1/followers/ids.json?cursor={cursor}&user_id={user_id}";

    @Autowired
    private RestTemplate restTemplate;

    public User showUserById(long id) throws RateLimitException {
        try {
            return restTemplate.getForObject(URL_SHOW_USER_BY_ID, User.class, Long.toUnsignedString(id));
        } catch (RestClientException e) {
            if (e.getCause() instanceof RateLimitException) {
                throw (RateLimitException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    public CursoredResult getFollowersByUserId(long userId, String cursor) {
        return restTemplate.getForObject(URL_FOLLOWERS_BY_ID, CursoredResult.class, cursor == null ? "-1" : cursor, Long.toUnsignedString(userId));
    }

    public void getFollowersByUserId(long userId, Observer<Long> observer) {
        String cursor = null;
        do {
            CursoredResult result;
            try {
                result = getFollowersByUserId(userId, cursor);
            } catch (Throwable e) {
                observer.onError(e);
                return;
            }
            for (Long id : result.getIds())
                observer.onNext(id);

            cursor = result.getNextCursor();
        } while (cursor != null);

        observer.onCompleted();
    }

}
