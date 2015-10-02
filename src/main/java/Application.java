import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFutureCallback;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final ApplicationContext APPLICATION_CONTEXT = new AnnotationConfigApplicationContext(AppConfig.class);

    public static void main(String[] args) {
        TwitterClient twitterClient = APPLICATION_CONTEXT.getBean(TwitterClient.class);

        User user = twitterClient.showUserById(146882655);
        logger.debug("Got {}", user);

        twitterClient.showUserByIdAsync(146882655, new ListenableFutureCallback<ResponseEntity<User>>() {
            public void onSuccess(ResponseEntity<User> result) {
                logger.debug("Got async {}", result.getBody());
            }

            public void onFailure(Throwable ex) {
                logger.error("showUserByIdAsync failed", ex);
            }
        });
    }

}
