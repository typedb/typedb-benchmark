package grakn.simulation.benchmark

import grakn.simulation.db.common.agent.insight.*
import grakn.simulation.db.common.agent.write.AgeUpdateAgent
import grakn.simulation.db.common.agent.write.PersonBirthAgent
import grakn.simulation.db.grakn.action.insight.*
import grakn.simulation.db.grakn.action.read.GraknBirthsInCityAction
import grakn.simulation.db.grakn.action.write.GraknUpdateAgesOfPeopleInCityAction
import grakn.simulation.db.neo4j.action.insight.*
import grakn.simulation.db.neo4j.action.read.Neo4jBirthsInCityAction
import grakn.simulation.db.neo4j.action.write.Neo4jUpdateAgesOfPeopleInCityAction
import groovy.text.GStringTemplateEngine

import java.time.LocalDateTime

class Report {

    static LinkedHashMap<String, String> agentDescriptions() {
        LinkedHashMap<String, String> desc = [:]

        desc."PersonBirth" = """
Adds people to the world simulation. This involves adding a single entity with a large number of attributes attached.
"""
        desc."Employment" = """
Finds existing people and makes them employees of companies.
"""
        return desc
    }

    static String outputFile = "/Users/jamesfletcher/programming/simulation/benchmark/tmp/report.tex"
    def engine

    static void main(String[] args) {
        def report = new Report()
        def output = report.render()
        def writer = new FileWriter(new File(outputFile))
        writer.write(output)
        writer.close()
    }

    Report() {
        engine = new GStringTemplateEngine()
    }

    abstract class Section {
        abstract String title()

        abstract String render()
    }

    class DbAgentSection extends Section {
        private List<String> queries
        private String name
        private String description

        DbAgentSection(String name, List<String> queries) {
            this.queries = queries
            this.name = name
            this.description = agentDescriptions().get(name)
        }

        @Override
        String title() {
            return name
        }

        @Override
        String render() {
            return engine.createTemplate(new File("benchmark/templates/agent_queries.tex")).make([databaseName: title(), queries: queries])
        }
    }

    class AgentSection extends Section {
        private final Class<?> agentClass
        private List<DbAgentSection> dbAgents = new ArrayList<>()
        private String title
        private String agentName

        AgentSection(Class<?> agentClass, List<String> graknQueries, List<String> neo4jQueries) {
            this.agentClass = agentClass
            this.agentName = agentClass.getSimpleName()
            this.title = this.agentName

            String suffix = "Agent"
            if (this.title.endsWith(suffix)) {
                this.title = this.title.substring(0, this.title.length() - suffix.length())
            }
            dbAgents.add(new DbAgentSection("Grakn", graknQueries))
            dbAgents.add(new DbAgentSection("Neo4j", neo4jQueries))
        }

        AgentSection(Class<?> agentClass, String graknQueries, String neo4jQueries) {
            this(agentClass, Arrays.asList(graknQueries), Arrays.asList(neo4jQueries))
        }

        @Override
        String title() {
            return this.title
        }

        @Override
        String render() {
            List<String> renderedDbAgents = []
            for (dbAgent in dbAgents) {
                renderedDbAgents.add(dbAgent.render())
            }
            return engine.createTemplate(new File("benchmark/templates/agent_section.tex")).make([agentSectionTitle: title(), agentName: this.agentName, renderedDbAgents: renderedDbAgents])
        }
    }

    String renderAgentSection() {
        String output = ""
        for (agentSection in agentSections()) {
            output = output.concat(agentSection.render())
        }
        return output
    }

    static String renderIntroduction() {
        return new File("benchmark/templates/introduction.tex").readLines().join("\n")
    }

    static String renderAgentSectionIntroduction() {
        return new File("benchmark/templates/agent_intro.tex").readLines().join("\n")
    }

    static String renderEnd() {
        return '\\end{document}'
    }

    String render() {
        String output = renderIntroduction() + "\n"
        output = output.concat(renderAgentSectionIntroduction())
        output = output.concat(renderAgentSection())
        output = output.concat(renderEnd())
        return output
    }

    List<AgentSection> agentSections() {
        String dummyString = "{}"
        long dummyLong = 12345
        double dummyDouble = 1.0
        String cityName = "{}"
        String destinationCity = "{}"
        String continentName = "{}"
        String email = "{}"
        long age = 5
        LocalDateTime dummyDate = LocalDateTime.of(0, 1, 1, 0, 0)
        LocalDateTime earliestDate = LocalDateTime.of(0, 1, 1, 0, 0)
        LocalDateTime relocationDate = LocalDateTime.of(0, 1, 1, 0, 0)

        List<AgentSection> agentSections = Arrays.asList(
                new AgentSection(
                        ArbitraryOneHopAgent.class,
                        GraknArbitraryOneHopAction.query().toString(),
                        Neo4jArbitraryOneHopAction.query()
                ),
                new AgentSection(
                        FindCurrentResidentsAgent.class,
                        GraknFindCurrentResidentsAction.query().toString(),
                        Neo4jFindCurrentResidentsAction.query()
                ),
                new AgentSection(
                        FindResidentsAgent.class,
                        GraknFindResidentsAction.query().toString(),
                        Neo4jFindResidentsAction.query()
                ),
                new AgentSection(
                        FindSpecificMarriageAgent.class,
                        GraknFindSpecificMarriageAction.query().toString(),
                        Neo4jFindSpecificMarriageAction.query()
                ),
                new AgentSection(
                        FindSpecificPersonAgent.class,
                        GraknFindSpecificPersonAction.query().toString(),
                        Neo4jFindSpecificPersonAction.query()
                ),
//                Disabled due to long runtimes
//                new AgentSection(
//                        FindTransactionCurrencyAgent.class,
//                        GraknFindTransactionCurrencyAction.query().toString(),
//                        Neo4jFindTransactionCurrencyAction.query()
//                ),
                new AgentSection(
                        FourHopAgent.class,
                        GraknFourHopAction.query().toString(),
                        Neo4jFourHopAction.query()
                ),
                new AgentSection(
                        MeanWageAgent.class,
                        GraknMeanWageOfPeopleInWorldAction.query().toString(),
                        Neo4jMeanWageOfPeopleInWorldAction.query()
                ),
                new AgentSection(
                        ThreeHopAgent.class,
                        GraknThreeHopAction.query().toString(),
                        Neo4jThreeHopAction.query()
                ),
                new AgentSection(
                        TwoHopAgent.class,
                        GraknTwoHopAction.query().toString(),
                        Neo4jTwoHopAction.query()
                ),
                new AgentSection(
                        PersonBirthAgent.class,
                        GraknBirthsInCityAction.query(cityName, dummyDate).toString(),
                        Neo4jBirthsInCityAction.query()
                ),
                new AgentSection(
                        AgeUpdateAgent.class,
                        Arrays.asList(
                                GraknUpdateAgesOfPeopleInCityAction.getPeopleBornInCityQuery(cityName).toString(),
                                GraknUpdateAgesOfPeopleInCityAction.deleteHasQuery(email).toString(),
                                GraknUpdateAgesOfPeopleInCityAction.insertNewAgeQuery(email, age).toString()
                        ),
                        Arrays.asList(
                                Neo4jUpdateAgesOfPeopleInCityAction.query()
                        )
                ),
//                new AgentSection(
//                        RelocationAgent.class,
//                        Arrays.asList(
//                                GraknResidentsInCityAction.query(cityName, earliestDate).toString(),
//                                GraknCitiesInContinentAction.query(cityName, continentName).toString(),
//                                GraknInsertRelocationAction.query(email, cityName, destinationCity, relocationDate).toString()
//                        ),
//                        Arrays.asList(
//                                Neo4jResidentsInCityAction.query(),
//                                Neo4jCitiesInContinentAction.query(),
//                                Neo4jInsertRelocationAction.endPastResidenciesQuery(),
//                                Neo4jInsertRelocationAction.createRelocationQuery()
//                        )
//                )
        )
        return agentSections
    }
}
