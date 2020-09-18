package grakn.simulation.config;

public enum AgentMode {
    TRACE(true, true),
    RUN(false, true),
    OFF(false, false);

    private final Boolean trace;
    private final Boolean run;

    AgentMode(Boolean trace, Boolean run) {
        this.trace = trace;
        this.run = run;
    }

    public Boolean getTrace() {
        return trace;
    }

    public Boolean getRun() {
        return run;
    }
}
