package grakn.simulation.db.common.context;

public abstract class DatabaseContext<TRANSACTION extends DatabaseTransaction> {
    public enum TracingLabel {
        OPEN_CLIENT("openClient"),
        CLOSE_CLIENT("closeClient"),
        OPEN_SESSION("openSession"),
        CLOSE_SESSION("closeSession"),
        OPEN_TRANSACTION("openTx"),
        CLOSE_TRANSACTION("closeTx"),
        COMMIT_TRANSACTION("commitTx"),
        EXECUTE("execute"),
        STREAM_AND_SORT("streamAndSort");

        private String name;

        TracingLabel(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    abstract public TRANSACTION tx(String sessionKey, LogWrapper log, String tracker);
}
