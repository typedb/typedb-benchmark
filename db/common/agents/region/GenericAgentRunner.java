//package grakn.simulation.db.common.agents.region;
//
//import grakn.simulation.db.common.agents.base.Agent;
//import grakn.simulation.db.common.agents.base.AgentRunner;
//import grakn.simulation.db.common.agents.base.IterationContext;
//import grakn.simulation.db.common.context.DatabaseContext;
//import grakn.simulation.db.common.world.Region;
//import grakn.simulation.utils.RandomSource;
//
//import java.util.List;
//
//import static java.util.stream.Collectors.toList;
//
//public class GenericAgentRunner<REGION extends Region, CONTEXT extends DatabaseContext> extends AgentRunner<REGION, CONTEXT> {
//
//    private SessionStrategy sessionStrategy;
//
//    public enum SessionStrategy {
//        CITY, COUNTRY, CONTINENT
//    }
//
//    public GenericAgentRunner(Class<? extends Agent<REGION, CONTEXT>> agentClass, CONTEXT backendContext, SessionStrategy sessionStrategy) {
//        super(agentClass, backendContext);
//        this.sessionStrategy = sessionStrategy;
//    }
//
//    @Override
//    protected List<REGION> getParallelItems(IterationContext iterationContext) {
//        return iterationContext.getWorld().getCities().collect(toList());
//    }
//
//    @Override
//    protected String getSessionKey(IterationContext iterationContext, RandomSource randomSource, REGION region) {
//        switch (sessionStrategy) {
//            case CITY:
//                return city.name();
//            case COUNTRY:
//                return city.country().name();
//            case CONTINENT:
//                return city.country().continent().name();
//            default:
//                throw new IllegalArgumentException("Unexpected session strategy: " + sessionStrategy.name());
//        }
//    }
//}
//
