package grakn.simulation.db.neo4j.agents.interaction;

import java.util.Arrays;

public class Neo4jQuery {
    private final String template;
    private final Object[] parameters;

    Neo4jQuery(String template, Object[] parameters) {
        this.template = template;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return template + " with parameters: " + Arrays.toString(parameters);
    }

    public String template() {
        return template;
    }

    public Object[] parameters(){
        return parameters;
    }
}
