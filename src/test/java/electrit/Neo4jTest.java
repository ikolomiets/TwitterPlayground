package electrit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Neo4jConfig.class})
public class Neo4jTest {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jTest.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testAction() throws Exception {

        // Create unique index on userId:
        // CREATE CONSTRAINT ON (u:User) ASSERT u.userId IS UNIQUE

        // Delete all relations:
        // MATCH (u1:User)-[r:FOLLOWS]->(u2) DELETE r

        // MERGE (user:User {userId: 2}) MERGE (follower:User {userId: 6}) MERGE (follower)-[:FOLLOWS]->(user)

        String query = "MATCH (u:User) RETURN u";

        jdbcTemplate.query(query, rs -> {
            do {
                //noinspection unchecked
                Map<String, Object> node = (Map<String, Object>) rs.getObject("u");
                logger.debug("node: {}, userId: {}", node, node.get("userId"));
            } while (rs.next());
        });

    }
}