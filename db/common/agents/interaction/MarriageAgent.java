package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.world.CityAgent;

import java.time.LocalDateTime;
import java.util.List;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class MarriageAgent extends CityAgent {

    @Override
    public final void iterate() {
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

        int numMarriagesPossible = Math.min(numMarriages, Math.min(womenEmails.size(), menEmails.size()));

        if (numMarriagesPossible > 0) {
            for (int i = 0; i < numMarriagesPossible; i++) {
                String wifeEmail = womenEmails.get(i);
                String husbandEmail = menEmails.get(i);
                int marriageIdentifier = (wifeEmail + husbandEmail).hashCode();
                try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("insertMarriage"))) {
                    insertMarriage(marriageIdentifier, wifeEmail, husbandEmail);
                }
            }
            tx().commitWithTracing();
        }
    }

    protected LocalDateTime dobOfAdults() {
        return today().minusYears(world().AGE_OF_ADULTHOOD);
    }

    protected abstract List<String> getSingleWomen();

    protected abstract List<String> getSingleMen();

//    TODO Should this inner query be included at the top level?
//    private GraqlGet.Unfiltered getSinglePeopleOfGenderQuery(String gender, String marriageRole);

    protected abstract void insertMarriage(int marriageIdentifier, String wifeEmail, String husbandEmail);
}
