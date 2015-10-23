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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, RxTwitterClient.class})
public class RxTwitterClientTest {

    private static final Logger logger = LoggerFactory.getLogger(TwitterClientTest.class);

    private static final long USER_ID = 146882655L;

    @Autowired
    private RxTwitterClient rxTwitterClient;

    @Test
    public void testShowUserById() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        rxTwitterClient.showUserById(USER_ID).subscribe(new Observer<User>() {
            @Override
            public void onCompleted() {
                logger.debug("onCompleted");
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                logger.error("onError", e);
                latch.countDown();
            }

            @Override
            public void onNext(User user) {
                logger.debug("onNext: {}", user.toString());
            }
        });

        latch.await();
    }

    @Test
    public void testGetFollowersByUserId() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        rxTwitterClient.getFollowersByUserId(USER_ID).subscribe(new Observer<Long>() {
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
        });

        logger.debug("XXX Awaiting result...");
        latch.await();
        logger.debug("Done!");
    }
}