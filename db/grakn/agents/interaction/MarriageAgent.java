package grakn.simulation.db.grakn.agents.interaction;

import grakn.client.answer.ConceptMap;
import grakn.simulation.db.common.agents.base.AgentResult;
import grakn.simulation.db.common.agents.interaction.MarriageAgentBase;
import grakn.simulation.db.common.world.World;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;
import graql.lang.statement.StatementAttribute;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.db.grakn.schema.Schema.CITY;
import static grakn.simulation.db.grakn.schema.Schema.DATE_OF_BIRTH;
import static grakn.simulation.db.grakn.schema.Schema.EMAIL;
import static grakn.simulation.db.grakn.schema.Schema.END_DATE;
import static grakn.simulation.db.grakn.schema.Schema.GENDER;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATED;
import static grakn.simulation.db.grakn.schema.Schema.LOCATES_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.LOCATION_NAME;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_HUSBAND;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_ID;
import static grakn.simulation.db.grakn.schema.Schema.MARRIAGE_WIFE;
import static grakn.simulation.db.grakn.schema.Schema.PERSON;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_LOCATION;
import static grakn.simulation.db.grakn.schema.Schema.RESIDENCY_RESIDENT;

public class MarriageAgent extends GraknAgent<World.City> implements MarriageAgentBase {

    @Override
    public List<String> getSingleWomen(World.City city, LocalDateTime dobOfAdults) {
        return getUnmarriedPeopleOfGender(city, "female", dobOfAdults);
    }

    @Override
    public List<String> getSingleMen(World.City city, LocalDateTime dobOfAdults) {
        return getUnmarriedPeopleOfGender(city, "male", dobOfAdults);
    }

    public List<String> getUnmarriedPeopleOfGender(World.City city, String gender, LocalDateTime dobOfAdults) {
        Statement personVar = Graql.var(PERSON);
        Statement cityVar = Graql.var(CITY);
        String marriageRole;
        if (gender.equals("female")) {
            marriageRole = MARRIAGE_WIFE;
        } else if (gender.equals("male")) {
            marriageRole = MARRIAGE_HUSBAND;
        } else {
            throw new IllegalArgumentException("Gender must be male or female");
        }
        GraqlGet query = Graql.match(
                personVar.isa(PERSON).has(GENDER, gender).has(EMAIL, Graql.var(EMAIL)).has(DATE_OF_BIRTH, Graql.var(DATE_OF_BIRTH)),
                Graql.var(DATE_OF_BIRTH).lte(dobOfAdults),
                Graql.not(Graql.var("m").isa(MARRIAGE).rel(marriageRole, personVar)),
                Graql.var("r").isa(RESIDENCY).rel(RESIDENCY_RESIDENT, personVar).rel(RESIDENCY_LOCATION, cityVar),
                Graql.not(Graql.var("r").has(END_DATE, Graql.var(END_DATE))),
                cityVar.isa(CITY).has(LOCATION_NAME, city.name())
        ).get(EMAIL);
        return tx().getOrderedAttribute(query, EMAIL, null);
    }

    @Override
    public AgentResult insertMarriage(World.City worldCity, int marriageIdentifier, String wifeEmail, String husbandEmail) {
        Statement husband = Graql.var("husband");
        Statement wife = Graql.var("wife");
        Statement city = Graql.var(CITY);
        Statement marriage = Graql.var("marriage");

        StatementAttribute cityNameVar = Graql.var().val(worldCity.name());
        StatementAttribute marriageIdentifierVar = Graql.var().val(marriageIdentifier);
        StatementAttribute husbandEmailVar = Graql.var().val(husbandEmail);
        StatementAttribute wifeEmailVar = Graql.var().val(wifeEmail);

        GraqlInsert marriageQuery = Graql.match(
                husband.isa(PERSON).has(EMAIL, husbandEmailVar),
                wife.isa(PERSON).has(EMAIL, wifeEmailVar),
                city.isa(CITY).has(LOCATION_NAME, cityNameVar)
        ).insert(
                marriage.isa(MARRIAGE)
                        .rel(MARRIAGE_HUSBAND, husband)
                        .rel(MARRIAGE_WIFE, wife)
                        .has(MARRIAGE_ID, marriageIdentifierVar),
                Graql.var().isa(LOCATES).rel(LOCATES_LOCATED, marriage).rel(LOCATES_LOCATION, city)
        );
        return single_result(tx().execute(marriageQuery));
    }

    @Override
    public AgentResult resultsForTesting(ConceptMap answer) {
        return new AgentResult(){{
            put(MarriageAgentField.MARRIAGE_IDENTIFIER, tx().getOnlyAttributeOfThing(answer, "marriage", MARRIAGE_ID));
            put(MarriageAgentField.WIFE_EMAIL, tx().getOnlyAttributeOfThing(answer, "wife", EMAIL));
            put(MarriageAgentField.HUSBAND_EMAIL, tx().getOnlyAttributeOfThing(answer, "husband", EMAIL));
            put(MarriageAgentField.CITY_NAME, tx().getOnlyAttributeOfThing(answer, CITY, LOCATION_NAME));
        }};
    }

//    protected int checkCount() {
//        Statement husband = Graql.var("husband");
//        Statement wife = Graql.var("wife");
//        Statement city = Graql.var(CITY);
//        Statement marriage = Graql.var(MARRIAGE);
//
//        Statement cityNameVar = Graql.var().val(worldCity.name());
//        Statement marriageIdentifierVar = Graql.var(MARRIAGE_ID);
//        Statement wifeEmailVar = Graql.var("wife-email");
//        Statement husbandEmailVar = Graql.var("husband-email");
//
//        GraqlGet.Aggregate countQuery = Graql.match(
//                husband.isa(PERSON).has(EMAIL, husbandEmailVar),
//                wife.isa(PERSON).has(EMAIL, wifeEmailVar),
//                city.isa(CITY).has(LOCATION_NAME, cityNameVar),
//                marriage.isa(MARRIAGE)
//                        .rel(MARRIAGE_WIFE, wife)
//                        .rel(MARRIAGE_HUSBAND, husband)
//                        .has(MARRIAGE_ID, marriageIdentifierVar),
//                Graql.var().isa(LOCATES).rel(LOCATES_LOCATED, marriage).rel(LOCATES_LOCATION, city)
//        ).get().count();
//        log().query("checkCount", countQuery);
//        return tx().count(countQuery);
//    }
}
