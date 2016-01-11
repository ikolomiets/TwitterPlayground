import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Neo4jConfig.class})
public class Neo4jJdbcTemplateTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testCreateRelation() throws Exception {
        DataSource dataSource = jdbcTemplate.getDataSource();

        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();

        System.err.println("supportsBatchUpdates: " + connection.getMetaData().supportsBatchUpdates());

        for (int i = 0; i < 10; i++) {
            statement.addBatch("MATCH (to:User) WHERE to.userId=104 MERGE (from:User{userId:105}) MERGE (from)-[:FOLLOWS]->(to)");
        }

        int[] updatedRows = statement.executeBatch();

        System.out.println(Arrays.asList(updatedRows));
    }

}