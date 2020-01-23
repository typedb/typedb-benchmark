package grakn.simulation.agents;

import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import graql.lang.Graql;

import java.util.List;
import java.util.stream.Collectors;

import static graql.lang.Graql.Token.Order.ASC;

public interface ContinentAgent extends ParallelAgent<String> {

    @Override
    default List<String> getParallelItems(AgentContext agentContext) {
        GraknClient.Transaction tx = agentContext.getGraknSession().transaction().read();

        List<ConceptMap> conceptMapList = tx.execute(
                Graql.match(
                        Graql.var().isa("continent").has("name", Graql.var("x"))
                ).get("x").sort("x", ASC)
        );

        return conceptMapList.stream()
                .map(c -> (String) c.get("x").asAttribute().value())
                .collect(Collectors.toList());
    }
}
