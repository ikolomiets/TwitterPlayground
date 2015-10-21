import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import rx.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;


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

    @Test
    public void testGetFollowersByUserId() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        Observer<Long> observer = new Observer<Long>() {
            final List<Long> ids = new ArrayList<>();

            public void onCompleted() {
                logger.debug("XXX onCompleted: got {} ids", ids.size());
                latch.countDown();
            }

            public void onError(Throwable e) {
                logger.error("XXX onError: got {} ids", ids.size(), e);
                latch.countDown();
            }

            public void onNext(Long id) {
                if (ids.contains(id)) {
                    logger.warn("XXX Got duplicate: {}", id);
                    return;
                }

                ids.add(id);

                if (ids.size() % 1000 == 0) {
                    logger.debug("XXX Got {} ids so far...", ids.size());
                }
            }
        };

        twitterClient.getFollowersByUserId(USER_ID, observer);

        logger.debug("XXX Awaiting result...");
        latch.await();
        logger.debug("Done!");
    }

    @Test
    public void testAsyncShowUserById() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<User> userRef = new AtomicReference<>();
        twitterClient.showUserById(USER_ID, new Observer<User>() {
            @Override
            public void onCompleted() {
                logger.debug("XXX onCompleted");
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                logger.error("XXX onError: {}", e.getMessage());
                latch.countDown();
            }

            @Override
            public void onNext(User user) {
                Assert.assertNotNull(user);
                userRef.set(user);
            }
        });

        logger.debug("XXX Waiting for result");
        latch.await();

        Assert.assertNotNull("User is not null", userRef.get());
    }
}