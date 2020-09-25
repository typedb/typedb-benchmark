package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.interaction.ParentshipAgentBase;
import grakn.simulation.db.common.world.World;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.BORN_IN;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_CHILD;
import static grakn.simulation.db.grakn.schema.Schema.BORN_IN_PLACE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.DATE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATED;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_HUSBAND;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_ID;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_WIFE;
import static grakn.simulation.db.grakn.schema.Schema.PARENTSHIP;
import static grakn.simulation.db.grakn.schema.Schema.PARENTSHIP_CHILD;
import static grakn.simulation.db.grakn.schema.Schema.PARENTSHIP_PARENT;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static java.util.stream.Collectors.toList;

public class ParentshipAgent extends GraknAgent<World.City> implements ParentshipAgentBase {

    @Override
    public List<HashMap<SpouseType, String>> getMarriageEmails(World.City city) {
        GraqlGet.Sorted marriageQuery = Graql.match(
                Graql.var(CITY).isa(CITY)
                        .has(LOCATION_NAME, city.name()),
                Graql.var("m").isa(MARRIAGE)
                        .rel(MARRIAGE_HUSBAND, Graql.var("husband"))
                        .rel(MARRIAGE_WIFE, Graql.var("wife"))
                        .has(MARRIAGE_ID, Graql.var(MARRIAGE_ID)),
                Graql.not(
                        Graql.var("par").isa(PARENTSHIP)
                                .rel(PARENTSHIP_PARENT, "husband")
                                .rel(PARENTSHIP_PARENT, "wife")
                ),
                Graql.var("husband").isa(PERSON)
                        .has(EMAIL, Graql.var("husband-email")),
                Graql.var("wife").isa(PERSON)
                        .has(EMAIL, Graql.var("wife-email")),
                Graql.var().isa(LOCATES)
                        .rel(LOCATES_LOCATED, Graql.var("m"))
                        .rel(LOCATES_LOCATION, Graql.var(CITY))
        ).get().sort(MARRIAGE_ID);
        return tx().execute(marriageQuery)
                .stream()
                .map(a -> new HashMap<SpouseType, String>() {{
                    put(SpouseType.WIFE, a.get("wife-email").asAttribute().value().toString());
                    put(SpouseType.HUSBAND, a.get("husband-email").asAttribute().value().toString());
                }})
                .collect(toList());
    }

    @Override
    public List<String> getChildrenEmailsBorn(World.City worldCity, LocalDateTime today) {
        GraqlGet.Unfiltered childrenQuery = Graql.match(
                Graql.var("c").isa(CITY)
                        .has(LOCATION_NAME, worldCity.name()),
                Graql.var("child").isa(PERSON)
                        .has(EMAIL, Graql.var(EMAIL))
                        .has(DATE_OF_BIRTH, today),
                Graql.var("bi").isa(BORN_IN)
                        .rel(BORN_IN_PLACE_OF_BIRTH, "c")
                        .rel(BORN_IN_CHILD, "child")
        ).get();
        return tx().getOrderedAttribute(childrenQuery, EMAIL, null);
    }

    @Override
    public AgentResult insertParentShip(HashMap<SpouseType, String> marriage, String childEmail) {
        // Parentship where parents have multiple children is represented as multiple ternary relations, each with
        // both parents and one child. They had these children at the same time, and will not have any subsequently.
        Statement parentship = Graql.var(PARENTSHIP);
        Statement child = Graql.var("child");
        Statement mother = Graql.var("mother");
        Statement father = Graql.var("father");

        GraqlInsert parentshipQuery = Graql.match(
                mother.isa(PERSON).has(EMAIL, marriage.get(SpouseType.WIFE)),
                father.isa(PERSON).has(EMAIL, marriage.get(SpouseType.HUSBAND)),
                child.isa(PERSON).has(EMAIL, childEmail)
        ).insert(
                parentship.isa(PARENTSHIP)
                        .rel(PARENTSHIP_PARENT, father)
                        .rel(PARENTSHIP_PARENT, mother)
                        .rel(PARENTSHIP_CHILD, child)
        );
        return single_result(tx().execute(parentshipQuery));
    }

    @Override
    public AgentResult resultsForTesting(ConceptMap answer) {
        return new AgentResult() {
            {
                put(ParentshipField.WIFE_EMAIL, tx().getOnlyAttributeOfThing(answer, "mother", EMAIL));
                put(ParentshipField.HUSBAND_EMAIL, tx().getOnlyAttributeOfThing(answer, "father", EMAIL));
                put(ParentshipField.CHILD_EMAIL, tx().getOnlyAttributeOfThing(answer, "child", EMAIL));
            }
        };
    }
}
