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
    public void testGetFollowersByUserId() throws Exception {
        final List<Long> ids = new ArrayList<Long>();

        twitterClient.getFollowersByUserId(USER_ID, new Observer<Long>() {
            public void onCompleted() {
                logger.debug("XXX onCompleted: got {} ids", ids.size());

            }

            public void onError(Throwable e) {
                logger.error("XXX onError: got {} ids", ids.size(), e);
                Assert.fail(e.getMessage());
            }

            public void onNext(Long id) {
                if (ids.contains(id)) {
                    Assert.fail("Duplicate id=" + id);
                }
                ids.add(id);
            }
        });

    }

}