package grakn.simulation.db.common.context;

import grakn.simulation.db.common.agents.base.DbOperationController;
import grakn.simulation.db.common.world.Region;
import org.slf4j.Logger;

public abstract class DbDriver {
//    TODO should have enums for tracing for the universal names of DB operations that we want to compare across
//     different backends

    public enum TracingLabel {
        START_OP("startOp"),
        CLOSE_OP("closeOp"),
        SAVE_OP("saveOp");

        private String name;

        TracingLabel(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public abstract DbOperationController getDbOpController(Region region, Logger logger);
}
