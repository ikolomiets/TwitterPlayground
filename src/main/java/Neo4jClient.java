import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class Neo4jClient {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jClient.class);

    private static final String QUERY_CREATE_REL = "MERGE (from:User {userId: {1}}) MERGE (to:User {userId: {2}}) MERGE (from)-[:FOLLOWS]->(to)";

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public Neo4jClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public User getUserById(long userId) {
        logger.debug("getUserById: userId: {}", userId);
        try {
            return jdbcTemplate.queryForObject("MATCH (u:User{userId: {1}}) RETURN u.userId as userId, u.screenName as screenName", (rs, rowNum) -> {
                User user = new User();
                user.setId(rs.getLong("userId"));
                user.setScreenName(rs.getString("screenName"));

                return user;
            }, userId);
        } catch (IncorrectResultSizeDataAccessException e) {
            return null;
        }
    }

    @Transactional
    public void mergeUser(User user) {
        logger.debug("mergeUser: userId: {}, screenName: {}", user.getId(), user.getScreenName());
        jdbcTemplate.update("MERGE (u:User{userId: {1}}) SET u.screenName={2}", user.getId(), user.getScreenName());
    }

    @Transactional
    public void createRelation(User from, User to) {
        logger.debug("createRelation (userId: {})-[:FOLLOWS]->(userId: {})", from.getId(), to.getId());
        jdbcTemplate.update(QUERY_CREATE_REL, from.getId(), to.getId());
    }

    @Transactional
    public void createFollowerRelations(List<Long> followerIds, User to) {
        logger.debug("createFollowerRelations: for user id={} and {} followers", to.getId(), followerIds.size());

        StringBuilder sb = new StringBuilder();
        sb.append("MATCH (to:User) WHERE to.userId=").append(to.getId());
        int counter = 0;
        for (Long followerId : followerIds) {
            String from = "from" + counter++;
            sb.append(" MERGE (").append(from).append(":User{userId:").append(followerId).append("}) MERGE(").append(from).append(")-[:FOLLOWS]->(to)");
        }

        jdbcTemplate.update(sb.toString());
    }

}
