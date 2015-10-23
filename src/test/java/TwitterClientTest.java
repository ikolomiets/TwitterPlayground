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

    private static final long USER_ID = 146882655L;

    @Autowired
    private TwitterClient twitterClient;

    @Autowired
    private RateLimitsScoreborad rateLimitsScoreborad;

    @Test
    public void testShowUserById() throws Exception {
        User user = twitterClient.showUserById(USER_ID);
        logger.debug("Got {}", user);
        Assert.assertNotNull(user);
        Assert.assertEquals(USER_ID, user.getId());
    }

    @Test
    public void testUsersRateLimit() throws Exception {
        User user = twitterClient.showUserById(USER_ID);
        Assert.assertNotNull(user);

        RateLimitInfo rateLimitInfo = rateLimitsScoreborad.getRateLimitInfo("users");
        Assert.assertNotNull(rateLimitInfo);

        User user1 = twitterClient.showUserById(USER_ID);
        Assert.assertNotNull(user1);

        RateLimitInfo rateLimitInfo1 = rateLimitsScoreborad.getRateLimitInfo("users");
        Assert.assertNotNull(rateLimitInfo1);

        Assert.assertNotSame(rateLimitInfo, rateLimitInfo1);
        Assert.assertTrue("new remaining = old remaining - 1", rateLimitInfo.getRemaining() - rateLimitInfo1.getRemaining() == 1);
    }

}