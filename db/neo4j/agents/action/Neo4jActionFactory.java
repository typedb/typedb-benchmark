package grakn.simulation.db.neo4j.agents.action;

import grakn.simulation.db.common.agents.action.ActionFactory;
import grakn.simulation.db.neo4j.agents.interaction.Neo4jDbOperationController;

public class Neo4jActionFactory extends ActionFactory<?, ?> {
    public Neo4jActionFactory(Neo4jDbOperationController dbOperationController) {
        super(dbOperationController);
    }
}
