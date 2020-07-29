package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.utils.Allocation;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.agents.world.CityAgent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class ParentshipAgent extends CityAgent {

    protected enum Email {
        WIFE, HUSBAND
    }

    @Override
    public final void iterate() {
        // Query for married couples in the city who are not already in a parentship relation together
        List<String> childrenEmails;
        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("getChildrenEmailsBorn"))) {
            childrenEmails = getChildrenEmailsBorn(today());
        }
        List<HashMap<Email, String>> marriageEmails;
        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("getMarriageEmails"))) {
            marriageEmails = getMarriageEmails();
        }

        if (marriageEmails.size() > 0 && childrenEmails.size() > 0) {
            LinkedHashMap<Integer, List<Integer>> childrenPerMarriage = Allocation.allocateEvenlyToMap(childrenEmails.size(), marriageEmails.size());

            for (Map.Entry<Integer, List<Integer>> childrenForMarriage : childrenPerMarriage.entrySet()) {
                Integer marriageIndex = childrenForMarriage.getKey();
                List<Integer> children = childrenForMarriage.getValue();

                HashMap<Email, String> marriage = marriageEmails.get(marriageIndex);

                List<String> childEmails = new ArrayList<>();
                for (Integer childIndex : children) {
                    childEmails.add(childrenEmails.get(childIndex));
                }
                try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("insertParentShip"))) {
                    insertParentShip(marriage, childEmails);
                }
            }
            commitTxWithTracing();
        }
    }

    abstract protected List<HashMap<Email, String>> getMarriageEmails();

    abstract protected List<String> getChildrenEmailsBorn(LocalDateTime dateToday);

    abstract protected void insertParentShip(HashMap<Email, String> marriage, List<String> childEmails);

    protected Pair<Integer, Integer> countBounds() {
        return new Pair<>(0, world().getScaleFactor());
    }
}
