package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
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

public class ParentshipAgent extends GraknAgent<World.City> implements grakn.simulation.db.common.agents.interaction.ParentshipAgent {

    @Override
    public List<HashMap<Email, String>> getMarriageEmails(World.City city) {
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

        log().query("getMarriageEmails", marriageQuery);
        List<ConceptMap> marriageAnswers = tx().execute(marriageQuery);

        return marriageAnswers
                .stream()
                .map(a -> new HashMap<Email, String>() {{
                    put(Email.WIFE, a.get("wife-email").asAttribute().value().toString());
                    put(Email.HUSBAND, a.get("husband-email").asAttribute().value().toString());
                }})
                .collect(toList());
    }

    @Override
    public List<String> getChildrenEmailsBorn(World.City worldCity, LocalDateTime dateToday) {

        GraqlGet.Unfiltered childrenQuery = Graql.match(
                Graql.var("c").isa(CITY)
                        .has(LOCATION_NAME, worldCity.name()),
                Graql.var("child").isa(PERSON)
                        .has(EMAIL, Graql.var(EMAIL))
                        .has(DATE_OF_BIRTH, dateToday),
                Graql.var("bi").isa(BORN_IN)
                        .rel(BORN_IN_PLACE_OF_BIRTH, "c")
                        .rel(BORN_IN_CHILD, "child")
        ).get();

        log().query("getChildrenEmails", childrenQuery);
        return tx().getOrderedAttribute(childrenQuery, EMAIL, null);
    }

    @Override
    public void insertParentShip(HashMap<Email, String> marriage, List<String> childEmails) {
        ArrayList<Statement> matchStatements = new ArrayList<>(Arrays.asList(
                Graql.var("mother").isa(PERSON).has(EMAIL, marriage.get(Email.WIFE)),
                Graql.var("father").isa(PERSON).has(EMAIL, marriage.get(Email.HUSBAND))
        ));

        Statement parentship = Graql.var("par");

        ArrayList<Statement> insertStatements = new ArrayList<>(Arrays.asList(
                parentship.isa(PARENTSHIP)
                        .rel(PARENTSHIP_PARENT, "father")
                        .rel(PARENTSHIP_PARENT, "mother")
        ));

        // This model currently inserts a single relation that combines both parents and all of the children they had.
        // They these children at the same time, and will not have any subsequently. This could be represented as
        // multiple ternary relations instead, each with both parents and one child.
        for (int i = 0; i < childEmails.size(); i++) {
            String childEmail = childEmails.get(i);
            Statement childVar = Graql.var("child-" + i);
            matchStatements.add(childVar.isa(PERSON).has(EMAIL, childEmail));
            insertStatements.add(parentship.rel(PARENTSHIP_CHILD, childVar));
        }

        GraqlInsert parentshipQuery = Graql.match(
                matchStatements
        ).insert(
                insertStatements
        );

        log().query("insertParentShip", parentshipQuery);
        tx().execute(parentshipQuery);
    }
}
