package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.utils.Pair;
import grakn.simulation.db.common.agents.world.CountryAgent;
import org.apache.commons.lang3.StringUtils;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public abstract class CompanyAgent extends CountryAgent {

    int numCompanies;

    @Override
    public final AgentResultSet iterate() {

        numCompanies = world().getScaleFactor();

        for (int i = 0; i < numCompanies; i++) {
            String adjective = pickOne(world().getAdjectives());
            String noun = pickOne(world().getNouns());

            int companyNumber = uniqueId(i);
            String companyName = StringUtils.capitalize(adjective) + StringUtils.capitalize(noun) + "-" + companyNumber;
            try (ThreadTrace trace = traceOnThread(this.checkMethodTrace("insertCompany"))) {
                insertCompany(companyNumber, companyName);
            }
        }
        commitTxWithTracing();
        return null;
    }

    protected abstract void insertCompany(int companyNumber, String companyName);

    protected Pair<Integer, Integer> countBounds() {
        return new Pair<>(numCompanies, numCompanies);
    }
}