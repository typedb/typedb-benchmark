package grakn.simulation.agents;

import grakn.client.GraknClient;
import grakn.client.answer.ConceptMap;
import grakn.simulation.common.Allocation;
import grakn.simulation.common.LogWrapper;
import grakn.simulation.common.RandomSource;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static grakn.simulation.common.ExecutorUtils.getOrderedAttribute;
import static java.util.stream.Collectors.toList;

public class ParentshipAgent implements CityAgent {

    private static final LogWrapper<World.City> LOG = new LogWrapper<>(LoggerFactory.getLogger(PersonBirthAgent.class), World.City::getTracker);

    @Override
    public void iterate(AgentContext context, RandomSource randomSource, World.City city) {
        // Find all people born today
        LocalDateTime dateToday = context.getLocalDateTime();

        // Query for married couples in the city who are not already in a parentship relation together

        GraknClient.Session session = context.getIterationGraknSessionFor(city.getCountry().getContinent().getName());

        try (GraknClient.Transaction tx = session.transaction().write()) {

            List<String> childrenEmails = getChildrenEmails(tx, city, dateToday);

            List<HashMap<String, String>> marriageEmails = getMarriageEmails(tx, city);

            if (marriageEmails.size() > 0 && childrenEmails.size() > 0) {
                LinkedHashMap<Integer, List<Integer>> childrenPerMarriage = Allocation.allocateEvenlyToMap(childrenEmails.size(), marriageEmails.size());

                for (Map.Entry<Integer, List<Integer>> childrenForMarriage : childrenPerMarriage.entrySet()) {
                    Integer marriageIndex = childrenForMarriage.getKey();
                    List<Integer> children = childrenForMarriage.getValue();

                    HashMap<String, String> marriage = marriageEmails.get(marriageIndex);

                    List<String> childEmails = new ArrayList<>();
                    for (Integer childIndex : children) {
                        childEmails.add(childrenEmails.get(childIndex));
                    }

                    insertParentShip(tx, city, marriage, childEmails);
                }
                tx.commit();
            }
        }
    }

    private List<HashMap<String, String>> getMarriageEmails(GraknClient.Transaction tx, World.City city) {
        GraqlGet.Sorted marriageQuery = Graql.match(
                Graql.var("city").isa("city")
                        .has("name", city.getName()),
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

        LOG.query(city, "getMarriageEmails", marriageQuery);
        List<ConceptMap> marriageAnswers = tx.execute(marriageQuery);

        return marriageAnswers
                .stream()
                .map(a -> new HashMap<String, String>() {{
                    put("wife-email", a.get("wife-email").asAttribute().value().toString());
                    put("husband-email", a.get("husband-email").asAttribute().value().toString());
                }})
                .collect(toList());
    }

    private List<String> getChildrenEmails(GraknClient.Transaction tx, World.City city, LocalDateTime dateToday) {

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

        LOG.query(city, "getChildrenEmails", childrenQuery);
        return getOrderedAttribute(tx, childrenQuery, "email");
    }

    private void insertParentShip(GraknClient.Transaction tx, World.City city, HashMap<String, String> marriage, List<String> childEmails) {
        ArrayList<Statement> matchStatements = new ArrayList<>(Arrays.asList(
                Graql.var("mother").isa("person").has("email", marriage.get("wife-email")),
                Graql.var("father").isa("person").has("email", marriage.get("husband-email"))
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

        GraqlInsert parentshipQuery = Graql.match(
                matchStatements
        ).insert(
                insertStatements
        );

        LOG.query(city, "insertParentShip", parentshipQuery);
        tx.execute(parentshipQuery);
    }
}
