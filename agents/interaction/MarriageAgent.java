package grakn.simulation.agents.interaction;

import grakn.simulation.agents.world.CityAgent;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;

import java.time.LocalDateTime;
import java.util.List;

import static grakn.simulation.common.ExecutorUtils.getOrderedAttribute;

public abstract class MarriageAgent extends CityAgent {

    @Override
    public final void iterate() {
        // Find bachelors and bachelorettes who are considered adults and who are not in a marriage and pair them off randomly
        List<String> womenEmails = getSingleWomen();
        shuffle(womenEmails);

        List<String> menEmails = getSingleMen();
        shuffle(menEmails);

        int numMarriages = world().getScaleFactor();

        int numMarriagesPossible = Math.min(numMarriages, Math.min(womenEmails.size(), menEmails.size()));

        if (numMarriagesPossible > 0) {

            for (int i = 0; i < numMarriagesPossible; i++) {
                insertMarriage(womenEmails.get(i), menEmails.get(i));
            }
            tx().commit();
        }
    }

    protected LocalDateTime dobOfAdults() {
        return today().minusYears(world().AGE_OF_ADULTHOOD);
    }

    protected abstract List<String> getSingleWomen();

    protected abstract List<String> getSingleMen();

//    TODO Should this inner query be included at the top level?
//    private GraqlGet.Unfiltered getSinglePeopleOfGenderQuery(String gender, String marriageRole);

    protected abstract void insertMarriage(String wifeEmail, String husbandEmail);
}
