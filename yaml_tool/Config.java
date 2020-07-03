package grakn.simulation.yaml_tool;

import java.util.List;

public class Config {
    private List<AgentConfig> agents;
    private String sampling;

    public List<AgentConfig> getAgents() {
        return agents;
    }

    public void setAgents(List<AgentConfig> agents) {
        this.agents = agents;
    }

    public String getSampling() {
        return sampling;
    }

    public void setSampling(String sampling) {
        this.sampling = sampling;
    }
}
