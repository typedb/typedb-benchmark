package grakn.simulation.db.neo4j.agents.interaction;

import java.util.HashMap;

public class Neo4jQuery {
    private final String template;
    private final HashMap<String, Object> parameters;

    Neo4jQuery(String template, HashMap<String, Object> parameters) {
        this.template = template;
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return template + " with parameters: " + parameters.toString();
    }

    public String template() {
        return template;
    }

    public HashMap<String, Object> parameters(){
        return new HashMap<>(parameters);
    }
}
