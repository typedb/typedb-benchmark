package grakn.simulation.db.neo4j.initialise;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.db.common.driver.DriverWrapper;
import grakn.simulation.db.common.initialise.Initialiser;
import grakn.simulation.db.common.yaml_tool.YAMLException;
import grakn.simulation.db.common.yaml_tool.YAMLLoader;
import grakn.simulation.db.neo4j.yaml_tool.Neo4jYAMLLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class Neo4jInitialiser extends Initialiser {

    public Neo4jInitialiser(Map<String, Path> files) {
        super(files);
    }

    protected void initialiseData(DriverWrapper.Session session) throws IOException, YAMLException {
        try (GrablTracingThreadStatic.ThreadContext context = GrablTracingThreadStatic.contextOnThread("db/common/data", 0)) {
            YAMLLoader loader = new Neo4jYAMLLoader(session, files);
            loader.loadFile(files.get("neo4j_data.yaml").toFile());
        }
    }

    @Override
    public void initialise(DriverWrapper driverWrapper, String databaseName) throws IOException, YAMLException {
        DriverWrapper.Session session = driverWrapper.session(databaseName);
        initialiseData(session);
        // TODO Add key constraints
    }
}
