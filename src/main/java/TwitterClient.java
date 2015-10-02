import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

@Component
public class TwitterClient {

    private static final String URL_SHOW_USER_BY_ID = "https://api.twitter.com/1.1/users/show.json?include_entities=false&user_id={user_id}";

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

}
