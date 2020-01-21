package grakn.simulation.common;

import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import graql.lang.Graql;

import java.util.List;
import java.util.stream.Collectors;

import static graql.lang.Graql.Token.Order.ASC;

public class GraknUtil {

    public static List<String> getSortedAttributeValues(GraknClient.Session session, String attributeName) {
        GraknClient.Transaction tx = session.transaction().read();

        List<ConceptMap> conceptMapList = tx.execute(
                Graql.match(
                        Graql.var("x").isa(attributeName)
                ).get("x").sort("x", ASC)
        );

        return conceptMapList.stream()
                .map(c -> (String) c.get("x").asAttribute().value())
                .collect(Collectors.toList());
    }
}
