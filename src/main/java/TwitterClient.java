import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TwitterClient {

    private static final String URL_SHOW_USER_BY_ID = "https://api.twitter.com/1.1/users/show.json?include_entities=false&user_id={user_id}";
    private static final String URL_FOLLOWERS_BY_ID = "https://api.twitter.com/1.1/followers/ids.json?cursor={cursor}&user_id={user_id}";

    private final RestTemplate restTemplate;

    @Autowired
    public TwitterClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public User showUserById(long id) {
        return restTemplate.getForObject(URL_SHOW_USER_BY_ID, User.class, Long.toUnsignedString(id));
    }

    public CursoredResult getFollowersByUserId(long userId, String cursor) {
        return restTemplate.getForObject(URL_FOLLOWERS_BY_ID, CursoredResult.class, cursor == null ? "-1" : cursor, Long.toUnsignedString(userId));
    }

}
