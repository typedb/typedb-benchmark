package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.interaction.FriendshipAgentBase;
import grakn.simulation.db.common.world.World;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;
import graql.lang.statement.StatementAttribute;

import java.time.LocalDateTime;
import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;
import static grakn.simulation.db.grakn.agents.interaction.RelocationAgent.cityResidentsQuery;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.FRIENDSHIP;
import static grakn.simulation.db.grakn.schema.Schema.FRIENDSHIP_FRIEND;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.START_DATE;

public class FriendshipAgent extends GraknAgent<World.City> implements FriendshipAgentBase {

    @Override
    public List<String> getResidentEmails(World.City city, LocalDateTime earliestDate) {
        GraqlGet cityResidentsQuery = cityResidentsQuery(city, earliestDate);
        return tx().getOrderedAttribute(cityResidentsQuery, EMAIL, null);
    }

    @Override
    public AgentResult insertFriendship(LocalDateTime today, String friend1Email, String friend2Email) {

        Statement person1 = Graql.var("p1");
        Statement person2 = Graql.var("p2");
        Statement friendship = Graql.var();

        StatementAttribute friend1EmailVar = Graql.var().val(friend1Email);
        StatementAttribute friend2EmailVar = Graql.var().val(friend2Email);
        StatementAttribute startDate = Graql.var().val(today);

        GraqlInsert insertFriendshipQuery = Graql.match(
                person1
                        .isa(PERSON).has(EMAIL, friend1EmailVar),
                person2
                        .isa(PERSON).has(EMAIL, friend2EmailVar),
                Graql.not(
                        friendship
                                .isa(FRIENDSHIP)
                                .rel(FRIENDSHIP_FRIEND, person1)
                                .rel(FRIENDSHIP_FRIEND, person2)
                )
        ).insert(
                Graql.var(FRIENDSHIP)
                        .isa(FRIENDSHIP)
                        .rel(FRIENDSHIP_FRIEND, person1)
                        .rel(FRIENDSHIP_FRIEND, person2)
                        .has(START_DATE, startDate)
        );
        return optional_single_result(tx().execute(insertFriendshipQuery));
    }

    @Override
    public AgentResult resultsForTesting(ConceptMap answer) {
        return new AgentResult() {
            {
                put(FriendshipField.FRIEND1_EMAIL, tx().getOnlyAttributeOfThing(answer, "p1", EMAIL));
                put(FriendshipField.FRIEND2_EMAIL, tx().getOnlyAttributeOfThing(answer, "p2", EMAIL));
                put(FriendshipField.START_DATE, tx().getOnlyAttributeOfThing(answer, FRIENDSHIP, START_DATE));
            }
        };
    }
}
