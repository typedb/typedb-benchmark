package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.agents.world.CityAgent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class MarriageAgentBase extends CityAgent {

    public enum MarriageAgentField implements Field {
        MARRIAGE_IDENTIFIER, WIFE_EMAIL, HUSBAND_EMAIL, CITY_NAME
    }

    int numMarriagesPossible;

    @Override
    public final AgentResult iterate() {
        HashSet<HashMap<Field, Object>> allFieldValues = new HashSet<>();
        log().message("MarriageAgent", String.format("Simulation step %d", simulationStep()));
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        List<String> womenEmails;
        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("getSingleWomen"))) {
            womenEmails = getSingleWomen();
        }
        shuffle(womenEmails);

        List<String> menEmails;
        try (ThreadTrace trace = traceOnThread(this.registerMethodTrace("getSingleMen"))) {
            menEmails = getSingleMen();
        }
        shuffle(menEmails);

        int numMarriages = world().getScaleFactor();

        numMarriagesPossible = Math.min(numMarriages, Math.min(womenEmails.size(), menEmails.size()));

        if (numMarriagesPossible > 0) {
            for (int i = 0; i < numMarriagesPossible; i++) {
                String wifeEmail = womenEmails.get(i);
                String husbandEmail = menEmails.get(i);
                int marriageIdentifier = (wifeEmail + husbandEmail).hashCode();

                HashMap<Field, Object> fieldValues;
                try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("insertMarriage"))) {
                    fieldValues = insertMarriage(marriageIdentifier, wifeEmail, husbandEmail);
                }
                allFieldValues.add(fieldValues);
            }
            commitTxWithTracing();
        }
        return new AgentResult(allFieldValues);
    }

    protected LocalDateTime dobOfAdults() {
        return today().minusYears(world().AGE_OF_ADULTHOOD);
    }

    protected abstract List<String> getSingleWomen();

    protected abstract List<String> getSingleMen();

//    TODO Should this inner query be included at the top level?
//    private GraqlGet.Unfiltered getSinglePeopleOfGenderQuery(String gender, String marriageRole);

    protected abstract HashMap<Field, Object> insertMarriage(int marriageIdentifier, String wifeEmail, String husbandEmail);

    protected Pair<Integer, Integer> countBounds() {
        return new Pair<>(numMarriagesPossible, numMarriagesPossible);
    }
}
