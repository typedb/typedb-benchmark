package grakn.simulation.test;

import grakn.simulation.db.common.agents.interaction.PersonBirthAgentBase;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.HashMap;

import static grakn.simulation.test.SimulationsUnderTest.graknSimulation;
import static grakn.simulation.test.SimulationsUnderTest.neo4jSimulation;
import static org.junit.Assert.assertEquals;

@RunWith(SimulationTestSuite.class)
public class SimulationTest {

    @Test
    public void testPersonBirthAgent() {
        Collection<HashMap<PersonBirthAgentBase.Field, Object>> graknFields = graknSimulation.getResultHandler().getResultForAgent("PersonBirthAgent").getAllFieldValues();
        Collection<HashMap<PersonBirthAgentBase.Field, Object>> neo4jFields = neo4jSimulation.getResultHandler().getResultForAgent("PersonBirthAgent").getAllFieldValues();
        assertEquals(graknFields, neo4jFields);
    }

    @Test
    public void testAgeUpdateAgent() {

    }
}
