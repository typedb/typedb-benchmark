package grakn.simulation.world.agents;

import grakn.client.GraknClient.Session;
import grakn.client.GraknClient.Transaction;
import grakn.client.answer.ConceptMap;
import grakn.simulation.framework.context.SimulationContext;
import grakn.simulation.world.entities.Continent;
import grakn.simulation.framework.agents.ParallelAgent;
import graql.lang.Graql;

import java.util.List;
import java.util.stream.Collectors;

import static graql.lang.Graql.Token.Order.ASC;

public interface ContinentAgent extends ParallelAgent<Continent> {

    @Override
    default List<Continent> getParallelList(SimulationContext context) {

        Session session = context.getGraknSession();
        Transaction tx = session.transaction().read();

        List<ConceptMap> conceptMapList = tx.execute(
                Graql.match(
                        Graql.var().isa("continent").has("continent-name", Graql.var("cn"))
                ).get("cn").sort("cn", ASC) // Sort to maintain determinism
        );

        return conceptMapList.stream()
                .map(c -> new Continent((String) c.get("cn").asAttribute().value()))
                .collect(Collectors.toList());
    }
}
