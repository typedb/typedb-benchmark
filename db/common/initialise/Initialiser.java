package grakn.simulation.db.common.initialise;

import grabl.tracing.client.GrablTracing;
import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.db.common.driver.DriverWrapper;
import grakn.simulation.db.common.world.World;
import grakn.simulation.db.common.yaml_tool.YAMLException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static grabl.tracing.client.GrablTracing.tracing;
import static grabl.tracing.client.GrablTracing.tracingNoOp;
import static grabl.tracing.client.GrablTracing.withLogging;

public abstract class Initialiser {

    protected final Map<String, Path> files;

    public Initialiser(Map<String, Path> files) {
        this.files = files;
    }

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

    public abstract void initialise(DriverWrapper driverWrapper, String databaseName) throws IOException, YAMLException;

}
