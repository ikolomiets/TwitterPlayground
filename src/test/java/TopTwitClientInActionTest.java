import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, TwitterClient.class})
public class TopTwitClientInActionTest {

    private static final Logger logger = LoggerFactory.getLogger(TopTwitClientInActionTest.class);

    private final TopTwitClient topTwitClient;

    @Autowired
    private TwitterClient twitterClient;

    public TopTwitClientInActionTest() throws Exception {
        this.topTwitClient = new TopTwitClient();
    }

    @Test
    public void testAction() throws Exception {
        for (int page = 1; page < 200; page++) {
            List<String> names = topTwitClient.getUsersOnPage(page);
            Assert.assertNotNull(names);
            Assert.assertTrue(names.size() > 0);

            List<User> users = twitterClient.lookupUsersByName(names);
            Assert.assertNotNull(users);
            Assert.assertTrue(users.size() > 0);

            List<Long> ids = users.stream().map(User::getId).collect(Collectors.toList());

            logger.debug("XXX page={}, names={}, ids={}", page, names, ids);
        }
    }
}