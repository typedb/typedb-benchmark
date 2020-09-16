package grakn.simulation.db.grakn.initialise;

import grakn.client.GraknClient;
import grakn.simulation.db.common.driver.DriverWrapper;
import grakn.simulation.db.common.initialise.Initialiser;
import grakn.simulation.db.common.yaml_tool.YAMLException;
import grakn.simulation.db.common.yaml_tool.YAMLLoader;
import grakn.simulation.db.grakn.yaml_tool.GraknYAMLLoader;
import graql.lang.Graql;
import graql.lang.query.GraqlDefine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class GraknInitialiser extends Initialiser {

    public GraknInitialiser(Map<String, Path> files) {
        super(files);
    }

    private void initialiseSchema(DriverWrapper.Session session) throws IOException {
        // TODO: merge these two schema files once this issue is fixed
        // https://github.com/graknlabs/grakn/issues/5553
        schemaFile(session,
                files.get("schema.gql"),
                files.get("schema-pt2.gql"));
    }

    private static void schemaFile(DriverWrapper.Session session, Path... schemaPath) throws IOException {
        System.out.println(">>>> trace: loadSchema: start");
        for (Path path : schemaPath) {
            String schemaQuery = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            System.out.println(">>>> trace: loadSchema - session.tx.write: start");
            try (GraknClient.Transaction tx = session.transaction().forGrakn()) {
                System.out.println(">>>> trace: loadSchema - session.tx.write: end");
                tx.execute((GraqlDefine) Graql.parse(schemaQuery));
                tx.commit();
            }
        }
        System.out.println(">>>> trace: loadSchema: end");
    }

    @Override
    protected void initialiseData(DriverWrapper.Session session) throws IOException, YAMLException {
        YAMLLoader loader = new GraknYAMLLoader(session, files);
        loader.loadFile(files.get("grakn_data.yml").toFile());
    }

    @Override
    public void initialise(DriverWrapper driverWrapper, String databaseName) throws IOException, YAMLException {
        DriverWrapper.Session session = driverWrapper.session(databaseName);
        initialiseSchema(session);
        initialiseData(session);
    }
}
