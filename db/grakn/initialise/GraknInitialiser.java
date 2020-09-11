package grakn.simulation.db.grakn.initialise;

import grakn.client.GraknClient;
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

public class GraknInitialiser {

    private final GraknClient.Session session;
    private final Map<String, Path> initialisationDataPaths;

    public GraknInitialiser(GraknClient.Session session, Map<String, Path> initialisationDataPaths) {
        this.session = session;
        this.initialisationDataPaths = initialisationDataPaths;
    }

    public void initialise() {
        try {
            initialiseSchema();
            initialiseData();
        } catch (IOException | YAMLException e) {
            e.printStackTrace();
        }
    }

    private void initialiseSchema() throws IOException {
        // TODO: merge these two schema files once this issue is fixed
        // https://github.com/graknlabs/grakn/issues/5553
        loadSchemaFile(
                initialisationDataPaths.get("schema.gql"),
                initialisationDataPaths.get("schema-pt2.gql")
        );
    }

    private void loadSchemaFile(Path... schemaPath) throws IOException {
        System.out.println(">>>> trace: loadSchema: start");
        for (Path path : schemaPath) {
            String schemaQuery = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            System.out.println(">>>> trace: loadSchema - session.tx.write: start");
            try (GraknClient.Transaction tx = session.transaction(GraknClient.Transaction.Type.WRITE)) {
                System.out.println(">>>> trace: loadSchema - session.tx.write: end");
                tx.execute((GraqlDefine) Graql.parse(schemaQuery));
                tx.commit();
            }
        }
        System.out.println(">>>> trace: loadSchema: end");
    }

    private void initialiseData() throws IOException, YAMLException {
        YAMLLoader loader = new GraknYAMLLoader(session, initialisationDataPaths);
        loader.loadFile(initialisationDataPaths.get("grakn_data.yml").toFile());
    }
}
