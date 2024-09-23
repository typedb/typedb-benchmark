package typeql;

import org.junit.Test;
import org.ldbcouncil.snb.impls.workloads.interactive.InteractiveTest;
import org.ldbcouncil.snb.impls.workloads.typeql.interactive.TypeQLInteractiveDb;

import java.util.HashMap;
import java.util.Map;

public class TypeQLInteractiveTest extends InteractiveTest {

    public TypeQLInteractiveTest() {
        super(new TypeQLInteractiveDb());
    }

    String endpoint = "localhost:1729";
    String databaseName = "ldbcsnb";
    String queryDir = "queries";

    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("endpoint", endpoint);
//        properties.put("user", user);
//        properties.put("password", password);
        properties.put("databaseName", databaseName);
        properties.put("printQueryNames", "true");
        properties.put("printQueryStrings", "true");
        properties.put("printQueryResults", "true");
        properties.put("queryDir", queryDir);
        return properties;
    }

    @Test
    public void runTestQuery1() throws Exception {
        testQuery1();
    }

    @Test
    public void runTestQuery2() throws Exception {
        testQuery2();
    }

    @Test
    public void runTestQuery3() throws Exception {
        testQuery3();
    }

    @Test
    public void runTestQuery4() throws Exception {
        testQuery4();
    }

    @Test
    public void runTestQuery5() throws Exception {
        testQuery5();
    }

    @Test
    public void runTestQuery6() throws Exception {
        testQuery6();
    }

    @Test
    public void runTestQuery7() throws Exception {
        testQuery7();
    }
}
