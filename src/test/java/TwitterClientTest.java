import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.CountDownLatch;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, TwitterClient.class})
public class TwitterClientTest {

    private static final Logger logger = LoggerFactory.getLogger(TwitterClientTest.class);

    private static final long USER_ID = 146882655L;

    @Autowired
    private TwitterClient twitterClient;

    @Test
    public void testShowUserById() throws Exception {
        User user = twitterClient.showUserById(USER_ID);
        logger.debug("Got {}", user);
        Assert.assertNotNull(user);
        Assert.assertEquals(USER_ID, user.getId());
    }

    @Test
    public void testShowUserByIdAsync() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        twitterClient.showUserByIdAsync(USER_ID, new ListenableFutureCallback<ResponseEntity<User>>() {
            public void onSuccess(ResponseEntity<User> result) {
                User user = result.getBody();
                logger.debug("Got async {}", user);
                Assert.assertNotNull(user);
                Assert.assertEquals(USER_ID, user.getId());
                latch.countDown();
            }

            public void onFailure(Throwable ex) {
                logger.error("showUserByIdAsync failed", ex);
                Assert.fail(ex.getMessage());
                latch.countDown();
            }
        });

        latch.await();
    }


    @Test
    public void testGetFollowersByUserId() throws Exception {
        // todo
    }

    @Test
    public void testGetFollowersByUserIdAsync() throws Exception {
        final CountDownLatch complete = new CountDownLatch(1);

        twitterClient.getFollowersByUserId(USER_ID, null, new ListenableFutureCallback<ResponseEntity<CursoredResult>>() {
            public void onSuccess(ResponseEntity<CursoredResult> result) {
                Assert.assertNotNull(result);
                Assert.assertNotNull(result.getBody());

                Assert.assertNotNull("ids is not null", result.getBody().getIds());
                Assert.assertTrue("ids is not empty", !result.getBody().getIds().isEmpty());
                Assert.assertEquals(5000, result.getBody().getIds().size());
                logger.debug("ids={}", result.getBody().getIds());

                Assert.assertNotNull("next_cursor is not null", result.getBody().getNextCursor());
                Assert.assertNotNull("previous_cursor is not null", result.getBody().getPreviousCursor());

                complete.countDown();
            }

            public void onFailure(Throwable ex) {
                logger.error("getFollowersByUserId failed", ex);
                Assert.fail(ex.getMessage());

                complete.countDown();
            }
        });

        complete.await();
    }
}