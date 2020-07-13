package grakn.simulation.initialise;

import grabl.tracing.client.GrablTracing;
import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.client.GraknClient;
import grakn.simulation.world.World;
import grakn.simulation.yaml_tool.GraknYAMLException;
import grakn.simulation.yaml_tool.GraknYAMLLoader;
import graql.lang.Graql;
import graql.lang.query.GraqlDefine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static grabl.tracing.client.GrablTracing.tracing;
import static grabl.tracing.client.GrablTracing.tracingNoOp;
import static grabl.tracing.client.GrablTracing.withLogging;

public class Initialise {
    public static World world(int scaleFactor, Map<String, Path> files) {
        World world;
        try {
            world = new World(
                    scaleFactor,
                    files.get("continents.csv"),
                    files.get("countries.csv"),
                    files.get("cities.csv"),
                    files.get("female_forenames.csv"),
                    files.get("male_forenames.csv"),
                    files.get("surnames.csv"),
                    files.get("adjectives.csv"),
                    files.get("nouns.csv")
            );
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
            return null;
        }
        return world;
    }

    public static GrablTracing grablTracing(String grablTracingUri, String grablTracingOrganisation, String grablTracingRepository, String grablTracingCommit, String grablTracingUsername, String grablTracingToken, boolean disableTracing) {
        GrablTracing tracing;
        if (disableTracing) {
            tracing = withLogging(tracingNoOp());
        } else if (grablTracingUsername == null) {
            tracing = withLogging(tracing(grablTracingUri));
        } else {
            tracing = withLogging(tracing(grablTracingUri, grablTracingUsername, grablTracingToken));
        }
        GrablTracingThreadStatic.setGlobalTracingClient(tracing);
        GrablTracingThreadStatic.openGlobalAnalysis(grablTracingOrganisation, grablTracingRepository, grablTracingCommit);
        return tracing;
    }

    public static class Grakn {

        public static void schema(GraknClient.Session session, Map<String, Path> files) throws IOException {
            try (GrablTracingThreadStatic.ThreadContext context = GrablTracingThreadStatic.contextOnThread("schema", 0)) {
                // TODO: merge these two schema files once this issue is fixed
                // https://github.com/graknlabs/grakn/issues/5553
                schemaFile(session,
                        files.get("schema.gql"),
                        files.get("schema-pt2.gql"));
            }
        }

        private static void schemaFile(GraknClient.Session session, Path... schemaPath) throws IOException {
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

        public static void data(GraknClient.Session session, Map<String, Path> files) throws IOException, GraknYAMLException {
            try (GrablTracingThreadStatic.ThreadContext context = GrablTracingThreadStatic.contextOnThread("data", 0)) {
                dataFile(session,
                        files.get("data.yaml"),
                        files.get("currencies.yaml"),
                        files.get("country_currencies.yaml"),
                        files.get("country_languages.yaml"));
            }
        }

        private static void dataFile(GraknClient.Session session, Path... dataPath) throws IOException, GraknYAMLException {
            GraknYAMLLoader loader = new GraknYAMLLoader(session);
            for (Path path : dataPath) {
                loader.loadFile(path.toFile());
            }
        }
    }
}
