import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);

        TwitterClient twitterClient = ctx.getBean(TwitterClient.class);

        User user = twitterClient.showUserById(146882655);
        logger.debug("Got {}", user);
    }

}
