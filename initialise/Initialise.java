package grakn.simulation.initialise;

import grabl.tracing.client.GrablTracing;
import grabl.tracing.client.GrablTracingThreadStatic;
import grakn.simulation.world.World;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static grabl.tracing.client.GrablTracing.tracing;
import static grabl.tracing.client.GrablTracing.tracingNoOp;
import static grabl.tracing.client.GrablTracing.withLogging;

public class Initialise {
    public static World initialiseWorld(int scaleFactor, Map<String, Path> files) {
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

    public static GrablTracing initialiseGrablTracing(String grablTracingUri, String grablTracingOrganisation, String grablTracingRepository, String grablTracingCommit, String grablTracingUsername, String grablTracingToken, boolean disableTracing) {
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
}
