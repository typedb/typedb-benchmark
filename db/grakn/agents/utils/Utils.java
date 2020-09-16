package grakn.simulation.db.grakn.agents.utils;

import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;

import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.getOnlyElement;

public class Utils {
    public static Object getOnlyAttributeOfThing(ConceptMap answer, GraknClient.Transaction tx, String varName, String attributeType) {
        return getOnlyElement(answer.get(varName).asThing().asRemote(tx).attributes(tx.getAttributeType(attributeType)).collect(Collectors.toList())).value();
    }
}
