/*
 * Copyright (C) 2021 Vaticle
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.vaticle.typedb.benchmark.report


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

        AgentSection(Class<?> agentClass, List<String> typeDBQueries, List<String> neo4jQueries) {
            this.agentClass = agentClass
            this.agentName = agentClass.getSimpleName()
            this.title = this.agentName

            String suffix = "Agent"
            if (this.title.endsWith(suffix)) {
                this.title = this.title.substring(0, this.title.length() - suffix.length())
            }
            dbAgents.add(new DbAgentSection("TypeDB", typeDBQueries))
            dbAgents.add(new DbAgentSection("Neo4j", neo4jQueries))
        }

        AgentSection(Class<?> agentClass, String typeDBQueries, String neo4jQueries) {
            this(agentClass, Arrays.asList(typeDBQueries), Arrays.asList(neo4jQueries))
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
        String cityName = "{}"
        String email = "{}"
        long age = 5
        LocalDateTime dummyDate = LocalDateTime.of(0, 1, 1, 0, 0)

        List<AgentSection> agentSections = Arrays.asList(
                new AgentSection(
                        ArbitraryOneHopAgent.class,
                        TypeDBArbitraryOneHopAgent.query().toString(),
                        Neo4jArbitraryOneHopAgent.query()
                ),
                new AgentSection(
                        FindCurrentResidentsAgent.class,
                        TypeDBFindCurrentResidentsAgent.query().toString(),
                        Neo4jFindCurrentResidentsAgent.query()
                ),
                new AgentSection(
                        FindLivedInAgent.class,
                        TypeDBFindLivedInAgent.query().toString(),
                        Neo4jFindLivedInAgent.query()
                ),
                new AgentSection(
                        FindSpecificMarriageAgent.class,
                        TypeDBFindSpecificMarriageAgent.query().toString(),
                        Neo4jFindSpecificMarriageAgent.query()
                ),
                new AgentSection(
                        FindSpecificPersonAgent.class,
                        TypeDBFindSpecificPersonAgent.query().toString(),
                        Neo4jFindSpecificPersonAgent.query()
                ),
//                Disabled due to long runtimes
//                new AgentSection(
//                        FindTransactionCurrencyAgent.class,
//                        TypeDBFindTransactionCurrencyAction.query().toString(),
//                        Neo4jFindTransactionCurrencyAction.query()
//                ),
                new AgentSection(
                        FourHopAgent.class,
                        TypeDBFourHopAgent.query().toString(),
                        Neo4jFourHopAgent.query()
                ),
                new AgentSection(
                        MeanWageAgent.class,
                        TypeDBMeanWageAgent.query().toString(),
                        Neo4jMeanWageAgent.query()
                ),
                new AgentSection(
                        ThreeHopAgent.class,
                        TypeDBThreeHopAgent.query().toString(),
                        Neo4jThreeHopAgent.query()
                ),
                new AgentSection(
                        TwoHopAgent.class,
                        TypeDBTwoHopAgent.query().toString(),
                        Neo4jTwoHopAgent.query()
                ),
                new AgentSection(
                        PersonBirthAgent.class,
                        TypeDBPersonBirthAgent.query(cityName, dummyDate).toString(),
                        Neo4jPersonBirthAgent.query()
                ),
                new AgentSection(
                        AgeUpdateAgent.class,
                        Arrays.asList(
                                TypeDBAgeUpdateAgent.getPeopleBornInCityQuery(cityName).toString(),
                                TypeDBAgeUpdateAgent.deleteHasQuery(email).toString(),
                                TypeDBAgeUpdateAgent.insertNewAgeQuery(email, age).toString()
                        ),
                        Arrays.asList(
                                Neo4jAgeUpdateAgent.query()
                        )
                ),
        )
        return agentSections
    }
}
