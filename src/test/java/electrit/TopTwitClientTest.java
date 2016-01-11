package electrit;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopTwitClientTest {

    private static final Logger logger = LoggerFactory.getLogger(TwitterClientTest.class);

    private TopTwitClient topTwitClient;

    public TopTwitClientTest() throws Exception {
        this.topTwitClient = new TopTwitClient();
    }

    @Test
    public void testGetPageUsers() throws Exception {
        List<String> result1 = topTwitClient.getUsersOnPage(1);
        Assert.assertNotNull(result1);
        Assert.assertEquals(10, result1.size());
        List<String> result2 = topTwitClient.getUsersOnPage(2);
        Assert.assertNotNull(result2);
        Assert.assertEquals(10, result2.size());

        Set<String> set1 = new HashSet<>(result1);
        Set<String> set2 = new HashSet<>(result2);

        Assert.assertEquals(10, set1.size());
        Assert.assertEquals(10, set2.size());

        set1.addAll(set2);

        Assert.assertEquals(20, set1.size());
    }

    @Test
    public void testGetTopUsers() throws Exception {
        List<String> topUsers = topTwitClient.getTopUsers(100);
        Assert.assertNotNull(topUsers);
        Assert.assertEquals(100, topUsers.size());
        Assert.assertEquals(100, new HashSet<>(topUsers).size());

        for (String topUser : topUsers) {
            logger.debug("Twitter URL: https://twitter.com/{}", topUser);
        }

        logger.debug("top users: {}", topUsers);
    }
}