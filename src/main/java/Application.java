import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final ApplicationContext APPLICATION_CONTEXT = new AnnotationConfigApplicationContext(AppConfig.class);

    public static void main(String[] args) {
        TwitterClient twitterClient = APPLICATION_CONTEXT.getBean(TwitterClient.class);

        User user = twitterClient.showUserById(146882655);
        logger.debug("Got {}", user);
    }

}
