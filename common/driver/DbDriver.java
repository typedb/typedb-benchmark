package grakn.simulation.common.driver;

import grakn.simulation.common.world.Region;
import org.slf4j.Logger;

public abstract class DbDriver<DB_OPERATION extends DbOperation> {
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

    public abstract void close();

    public abstract DbOperationFactory<DB_OPERATION> getDbOperationFactory(Region region, Logger logger);
}
