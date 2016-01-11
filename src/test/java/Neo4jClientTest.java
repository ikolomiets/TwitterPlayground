import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Neo4jConfig.class})
public class Neo4jClientTest {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jClientTest.class);

    @Autowired
    private Neo4jClient neo4jClient;

    @Test
    public void testCreateRelation() throws Exception {
        User from = new User();
        from.setId(100);

        User to = new User();
        to.setId(101);

        neo4jClient.createRelation(from, to);
    }

    @Test
    public void testCreateFollowerRelations() throws Exception {
        User user = new User();
        user.setId(0);
        user.setScreenName("gosha");

        neo4jClient.mergeUser(user);

        List<Long> followers = new ArrayList<>();
        for (long i = 1; i < 200; i++)
            followers.add(i);

        neo4jClient.createFollowerRelations(followers, user);
    }

    @Test
    public void testMergeUser() throws Exception {
        User user = new User();
        user.setId(99);
        user.setScreenName("gosha");

        neo4jClient.mergeUser(user);
    }

    @Test
    public void testGetUserById() throws Exception {
        User user = neo4jClient.getUserById(114557496);
        logger.debug("testGetUserById: got {}", user);
    }

}