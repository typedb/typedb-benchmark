package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.grakn.driver.GraknClientWrapper;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static grakn.simulation.db.grakn.agents.interaction.ExecutorUtils.getOrderedAttribute;
import static java.util.stream.Collectors.toList;

public class ParentshipAgent extends grakn.simulation.db.common.agents.interaction.ParentshipAgent<GraknClientWrapper.Session, GraknClientWrapper.Transaction> {

    @Override
    protected List<HashMap<Email, String>> getMarriageEmails() {
        GraqlGet.Sorted marriageQuery = Graql.match(
                Graql.var("city").isa("city")
                        .has("location-name", city().name()),
                Graql.var("m").isa("marriage")
                        .rel("marriage_husband", Graql.var("husband"))
                        .rel("marriage_wife", Graql.var("wife"))
                        .has("marriage-id", Graql.var("marriage-id")),
                Graql.not(
                        Graql.var("par").isa("parentship")
                                .rel("parentship_parent", "husband")
                                .rel("parentship_parent", "wife")
                ),
                Graql.var("husband").isa("person")
                        .has("email", Graql.var("husband-email")),
                Graql.var("wife").isa("person")
                        .has("email", Graql.var("wife-email")),
                Graql.var().isa("locates")
                        .rel("locates_located", Graql.var("m"))
                        .rel("locates_location", Graql.var("city"))
        ).get().sort("marriage-id");

        log().query("getMarriageEmails", marriageQuery);
        List<ConceptMap> marriageAnswers = tx().forGrakn().execute(marriageQuery).get();

        return marriageAnswers
                .stream()
                .map(a -> new HashMap<Email, String>() {{
                    put(Email.WIFE, a.get("wife-email").asAttribute().value().toString());
                    put(Email.HUSBAND, a.get("husband-email").asAttribute().value().toString());
                }})
                .collect(toList());
    }

    @Override
    protected List<String> getChildrenEmailsBorn(LocalDateTime dateToday) {

        GraqlGet.Unfiltered childrenQuery = Graql.match(
                Graql.var("c").isa("city")
                        .has("location-name", city().name()),
                Graql.var("child").isa("person")
                        .has("email", Graql.var("email"))
                        .has("date-of-birth", dateToday),
                Graql.var("bi").isa("born-in")
                        .rel("born-in_place-of-birth", "c")
                        .rel("born-in_child", "child")
        ).get();

        log().query("getChildrenEmails", childrenQuery);
        return getOrderedAttribute(tx().forGrakn(), childrenQuery, "email");
    }

    @Override
    protected void insertParentShip(HashMap<Email, String> marriage, List<String> childEmails) {
        ArrayList<Statement> matchStatements = new ArrayList<>(Arrays.asList(
                Graql.var("mother").isa("person").has("email", marriage.get(Email.WIFE)),
                Graql.var("father").isa("person").has("email", marriage.get(Email.HUSBAND))
        ));

        Statement parentship = Graql.var("par");

        ArrayList<Statement> insertStatements = new ArrayList<>(Arrays.asList(
                parentship.isa("parentship")
                        .rel("parentship_parent", "father")
                        .rel("parentship_parent", "mother")
        ));

        // This model currently inserts a single relation that combines both parents and all of the children they had.
        // They these children at the same time, and will not have any subsequently. This could be represented as
        // multiple ternary relations instead, each with both parents and one child.
        for (int i = 0; i < childEmails.size(); i++) {
            String childEmail = childEmails.get(i);
            Statement childVar = Graql.var("child-" + i);
            matchStatements.add(childVar.isa("person").has("email", childEmail));
            insertStatements.add(parentship.rel("parentship_child", childVar));
        }

        GraqlInsert parentshipQuery = Graql.match(
                matchStatements
        ).insert(
                insertStatements
        );

        log().query("insertParentShip", parentshipQuery);
        tx().forGrakn().execute(parentshipQuery);
    }
}
