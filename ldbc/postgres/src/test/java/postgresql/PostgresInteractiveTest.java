package postgresql;

import org.junit.Test;
import org.ldbcouncil.snb.impls.workloads.interactive.InteractiveTest;
import org.ldbcouncil.snb.impls.workloads.postgres.interactive.PostgresInteractiveDb;

import java.util.HashMap;
import java.util.Map;

public class PostgresInteractiveTest extends InteractiveTest {

    public PostgresInteractiveTest() {
        super(new PostgresInteractiveDb());
    }

    String endpoint = "jdbc:postgresql://localhost:5432/ldbcsnb";
    String user = "postgres";
    String password = "mysecretpassword";
    String databaseName = "ldbcsnb";
    String jdbcDriver = "org.postgresql.Driver";
    String queryDir = "queries";

    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("endpoint", endpoint);
        properties.put("user", user);
        properties.put("password", password);
        properties.put("databaseName", databaseName);
        properties.put("jdbcDriver", jdbcDriver);
        properties.put("printQueryNames", "true");
        properties.put("printQueryStrings", "true");
        properties.put("printQueryResults", "true");
        properties.put("queryDir", queryDir);
        return properties;
    }

    @Test
    public void runTestQuery2() throws Exception {
        testQuery2();
    }
}
