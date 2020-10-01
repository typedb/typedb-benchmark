package grakn.simulation.db.common.agents.interaction;

import grabl.tracing.client.GrablTracingThreadStatic.ThreadTrace;
import grakn.simulation.db.common.agents.base.Agent;
import grakn.simulation.db.common.agents.base.ActionResult;
import grakn.simulation.db.common.agents.base.ActionResultList;
import grakn.simulation.db.common.agents.base.SimulationContext;
import grakn.simulation.db.common.world.World;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

import static grabl.tracing.client.GrablTracingThreadStatic.traceOnThread;

public interface CompanyAgentBase extends InteractionAgent<World.Country> {

    enum CompanyAgentField implements Agent.ComparableField {
        COMPANY_NUMBER, COMPANY_NAME, DATE_OF_INCORPORATION, COUNTRY
    }

    default void iterate(Agent<World.Country, ?> agent, World.Country country, SimulationContext simulationContext) {

        int numCompanies = simulationContext.world().getScaleFactor();
        ActionResultList agentResultSet = new ActionResultList();
        for (int i = 0; i < numCompanies; i++) {
            String adjective = agent.pickOne(simulationContext.world().getAdjectives());
            String noun = agent.pickOne(simulationContext.world().getNouns());

            int companyNumber = agent.uniqueId(simulationContext, i);
            String companyName = StringUtils.capitalize(adjective) + StringUtils.capitalize(noun) + "-" + companyNumber;
            agent.startDbOperation("insertCompany");
            try (ThreadTrace trace = traceOnThread(agent.action())) {
                agentResultSet.add(insertCompany(country, simulationContext.today(), companyNumber, companyName));
            }
        }
        agent.saveDbOperation();
        return agentResultSet;
    }

    ActionResult insertCompany(World.Country country, LocalDateTime today, int companyNumber, String companyName);
}