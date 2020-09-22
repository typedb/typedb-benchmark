package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.AgentResultSet;
import grakn.simulation.db.common.agents.base.IterationContext;
import grakn.simulation.db.common.world.World;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface CompanyAgentBase extends InteractionAgent<World.Country> {

    default AgentResultSet iterate(Agent<World.Country, ?> agent, World.Country country, IterationContext iterationContext) {

        int numCompanies = iterationContext.world().getScaleFactor();
        for (int i = 0; i < numCompanies; i++) {
            String adjective = agent.pickOne(iterationContext.world().getAdjectives());
            String noun = agent.pickOne(iterationContext.world().getNouns());

            int companyNumber = agent.uniqueId(iterationContext, i);
            String companyName = StringUtils.capitalize(adjective) + StringUtils.capitalize(noun) + "-" + companyNumber;
            agent.newAction("insertCompany");
            try (ThreadTrace trace = traceOnThread(agent.action())) {
                insertCompany(country, iterationContext.today(), companyNumber, companyName);
            }
        }
        agent.commitAction();
        return null;
    }

    void insertCompany(World.Country country, LocalDateTime today, int companyNumber, String companyName);

}