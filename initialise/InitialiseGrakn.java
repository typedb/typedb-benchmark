package grakn.simulation.initialise;

import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.GraknClient;
import grakn.simulation.driver.GraknClientWrapper;
import grakn.simulation.yaml_tool.GraknYAMLException;
import grakn.simulation.yaml_tool.GraknYAMLLoader;
import graql.lang.Graql;
import graql.lang.query.GraqlDefine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class InitialiseGrakn {
    public static void initialise(String graknKeyspace, Map<String, Path> files, GraknClientWrapper driver) throws IOException, GraknYAMLException {
        try (GraknClient.Session session = driver.getClient().session(graknKeyspace)) {
            // TODO: merge these two schema files once this issue is fixed
            // https://github.com/graknlabs/grakn/issues/5553
            try (GrablTracingThreadStatic.ThreadContext context = GrablTracingThreadStatic.contextOnThread("schema", 0)) {
                loadSchema(session,
                        files.get("schema.gql"),
                        files.get("schema-pt2.gql"));
            }
            try (GrablTracingThreadStatic.ThreadContext context = GrablTracingThreadStatic.contextOnThread("data", 0)) {
                loadData(session,
                        files.get("data.yaml"),
                        files.get("currencies.yaml"),
                        files.get("country_currencies.yaml"),
                        files.get("country_languages.yaml"));
            }
        }
    }

    private static void loadSchema(GraknClient.Session session, Path... schemaPath) throws IOException {
        System.out.println(">>>> trace: loadSchema: start");
        for (Path path : schemaPath) {
            String schemaQuery = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

            System.out.println(">>>> trace: loadSchema - session.tx.write: start");
            try (GraknClient.Transaction tx = session.transaction().write()) {
                System.out.println(">>>> trace: loadSchema - session.tx.write: end");
                tx.execute((GraqlDefine) Graql.parse(schemaQuery));
                tx.commit();
            }
        }
        System.out.println(">>>> trace: loadSchema: end");
    }

    private static void loadData(GraknClient.Session session, Path... dataPath) throws IOException, GraknYAMLException {
        GraknYAMLLoader loader = new GraknYAMLLoader(session);
        for (Path path : dataPath) {
            loader.loadFile(path.toFile());
        }
    }
}
