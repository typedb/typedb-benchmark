package grakn.simulation.config;

import grakn.simulation.agents.AgeUpdateAgent;
import grakn.simulation.agents.CompanyAgent;
import grakn.simulation.agents.EmploymentAgent;
import grakn.simulation.agents.FriendshipAgent;
import grakn.simulation.agents.MarriageAgent;
import grakn.simulation.agents.ParentshipAgent;
import grakn.simulation.agents.PersonBirthAgent;
import grakn.simulation.agents.ProductAgent;
import grakn.simulation.agents.RelocationAgent;
import grakn.simulation.agents.TransactionAgent;
import grakn.simulation.agents.base.AgentRunner;
import grakn.simulation.agents.common.CityAgentRunner;
import grakn.simulation.agents.common.ContinentAgentRunner;
import grakn.simulation.agents.common.CountryAgentRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Schema {

    static HashMap<String, AgentRunner<?>> AGENTS;
    static {
        AGENTS = new HashMap<>();
        AGENTS.put("MarriageAgent", new CityAgentRunner(MarriageAgent.class));
        AGENTS.put("PersonBirthAgent", new CityAgentRunner(PersonBirthAgent.class));
        AGENTS.put("AgeUpdateAgent", new CityAgentRunner(AgeUpdateAgent.class));
        AGENTS.put("ParentshipAgent", new CityAgentRunner(ParentshipAgent.class));
        AGENTS.put("RelocationAgent", new CityAgentRunner(RelocationAgent.class));
        AGENTS.put("CompanyAgent", new CountryAgentRunner(CompanyAgent.class));
        AGENTS.put("EmploymentAgent", new CityAgentRunner(EmploymentAgent.class));
        AGENTS.put("ProductAgent", new ContinentAgentRunner(ProductAgent.class));
        AGENTS.put("TransactionAgent", new CountryAgentRunner(TransactionAgent.class));
        AGENTS.put("FriendshipAgent", new CityAgentRunner(FriendshipAgent.class));
    }

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

    public enum SamplingFunction {

        EVERY("every", new int[0]),
        LOG("log", new int[0]);

        private final String name;
        private final int[] acceptedArgs;

        SamplingFunction(String name, int[] acceptedArgs) {
            this.name = name;
            this.acceptedArgs = acceptedArgs;
        }

        public static SamplingFunction getByName(String name) {
            for (SamplingFunction samplingFunction : SamplingFunction.values()) {
                if(samplingFunction.getName().equals(name)) {
                    return samplingFunction;
                }
            }
            throw new IllegalArgumentException("Function name not recognised");
        }

        public String getName() {
            return name;
        }

        public int[] getAcceptedArgs() {
            return acceptedArgs;
        }

        public static Function<Integer, Boolean> applyArg(SamplingFunction sampling, Integer arg) {
            switch (sampling) {
                case EVERY:
                    if (arg < 1) throw new IllegalArgumentException("`every` requires an argument of 1 or greater");
                    return i -> i % arg == 0;
                case LOG:
                    if (arg < 2) throw new IllegalArgumentException("`log` requires a base of 2 or greater");
                    // return true if the logarithm of `i` to the given base is an integer
                    return i -> ((int) Math.log(i) / Math.log(arg) % 1) == 0;
                default:
                    throw new IllegalArgumentException(String.format("Function not available, possible functions are %s",
                            Arrays.stream(SamplingFunction.values()).map(Enum::name).collect(Collectors.toSet())));
            }
        }
    }

}
