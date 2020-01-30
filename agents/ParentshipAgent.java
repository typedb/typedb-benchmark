package grakn.simulation.agents;

import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import grakn.simulation.common.RandomSource;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ParentshipAgent implements CityAgent {

    @Override
    public void iterate(AgentContext context, RandomSource randomSource, World.City city) {

        // Find all people born today
        LocalDateTime dateToday = context.getLocalDateTime();
        GraqlGet.Unfiltered childrenQuery = Graql.match(
                Graql.var("c").isa("city")
                        .has("name", city.getName()),
                Graql.var("child").isa("person")
                        .has("email", Graql.var("email"))
                        .has("date-of-birth", dateToday),
                Graql.var("bi").isa("born-in")
                        .rel("born-in_place-of-birth", "c")
                        .rel("born-in_child", "child")
        ).get();

        // Query for married couples in the city who are not already in a parentship relation together
        GraqlGet.Unfiltered marriageQuery = Graql.match(
                Graql.var("city").isa("city")
                        .has("name", city.getName()),
                Graql.var("m").isa("marriage")
                        .rel("marriage_husband", Graql.var("husband"))
                        .rel("marriage_wife", Graql.var("wife"))
                        .has("identifier", Graql.var("marriage-id")),
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
        ).get();

        Random random = randomSource.startNewRandom();

        GraknClient.Session session = context.getIterationGraknSessionFor(city.getCountry().getContinent().getName());

        try (GraknClient.Transaction tx = session.transaction().write()) {

            List<ConceptMap> childrenAnswers = tx.execute(childrenQuery);
            List<ConceptMap> marriageAnswers = tx.execute(marriageQuery);

            if (marriageAnswers.size() > 0) {
                Collections.shuffle(marriageAnswers, random);

                int numChildrenPerMarriage = childrenAnswers.size() / marriageAnswers.size();
                int remainder = childrenAnswers.size() % marriageAnswers.size();

                // Distribute the children among the marriage results
                int childrenIndex = 0;

                for (int i = 0; i < marriageAnswers.size(); i++) {
                    int childrenToAdd = numChildrenPerMarriage;
                    if (i < remainder) {
                        childrenToAdd++;
                    }
                    List<String> childEmails = new ArrayList<>();
                    for (int j = 0; j < childrenToAdd; j++) {
                        childEmails.add(childrenAnswers.get(childrenIndex).get("email").asAttribute().value().toString());
                        childrenIndex++;
                    }

                    String motherEmail = marriageAnswers.get(i).get("wife-email").asAttribute().value().toString();
                    String husbandEmail = marriageAnswers.get(i).get("husband-email").asAttribute().value().toString();

                    tx.execute(buildParentshipInsertQuery(motherEmail, husbandEmail, childEmails));
                }
                tx.commit();
            }
        }
    }

    private GraqlInsert buildParentshipInsertQuery(String motherEmail, String fatherEmail, List<String> childEmails) {

        ArrayList<Statement> matchStatements = new ArrayList<>(Arrays.asList(
                Graql.var("mother").isa("person").has("email", motherEmail),
                Graql.var("father").isa("person").has("email", fatherEmail)
        ));

        Statement parentship = Graql.var("par");

        ArrayList<Statement> insertStatements = new ArrayList<>(Arrays.asList(
                parentship.isa("parentship")
                .rel("parentship_parent", "father")
                .rel("parentship_parent", "mother")
        ));

        for (int i = 0; i < childEmails.size(); i++) {
            String childEmail = childEmails.get(i);
            Statement childVar = Graql.var("child-" + i);
            matchStatements.add(childVar.isa("person").has("email", childEmail));
            insertStatements.add(parentship.rel("parentship_child", childVar));
        }

        return Graql.match(
                matchStatements
        ).insert(
                insertStatements
        );
    }
}
