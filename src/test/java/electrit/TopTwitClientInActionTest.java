package electrit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
        List<String> allTopUsers = new ArrayList<>();
        for (int page = 1; page <= 500; page++) {
            List<String> names = topTwitClient.getUsersOnPage(page);
            Assert.assertNotNull(names);
            Assert.assertEquals(10, names.size());
            allTopUsers.addAll(names);
        }

        for (int page = 0; page < 50; page++) {
            List<String> pageUsers = new LinkedList<>(allTopUsers.subList(page * 100, (page + 1) * 100));
            Assert.assertEquals(100, pageUsers.size());
            List<User> users = twitterClient.lookupUsersByName(pageUsers);
            for (User user : users) {
                pageUsers.remove(user.getScreenName());

                if (user.getFriendsCount() > 1000) {
                    logger.warn("Ignore overfriendly user {},{},{},{},{}", user.getId(), user.getScreenName(), user.getFollowersCount(), user.getFriendsCount(), user.isProtectedAccount());
                    continue;
                }

                if (user.isProtectedAccount()) {
                    logger.warn("Ignore protected user {},{},{},{},{}", user.getId(), user.getScreenName(), user.getFollowersCount(), user.getFriendsCount(), user.isProtectedAccount());
                    continue;
                }

                logger.info("{},{},{},{},{}", user.getId(), user.getScreenName(), user.getFollowersCount(), user.getFriendsCount(), user.isProtectedAccount());
            }

            for (String pageUser : pageUsers) {
                logger.warn("User ranked {} is not found: {}", (page * 100 + pageUsers.indexOf(pageUser)), pageUser);
            }
        }
    }
}