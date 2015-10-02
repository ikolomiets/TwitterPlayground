import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, TwitterClient.class})
public class TwitterClientTest {

    private static final Logger logger = LoggerFactory.getLogger(TwitterClientTest.class);

    @Autowired
    private TwitterClient twitterClient;

    @Test
    public void testShowUserById() throws Exception {
        User user = twitterClient.showUserById(146882655);
        logger.debug("Got {}", user);
        Assert.assertNotNull(user);


        Assert.assertEquals("https://pbs.twimg.com/profile_images/507133713386598400/7m1JJ2h2_normal.jpeg", user.getProfileImageUrlHttps());
    }

}