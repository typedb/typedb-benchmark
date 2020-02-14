package grakn.simulation.agents;

import grakn.client.GraknClient;
import grakn.simulation.common.LogWrapper;
import grakn.simulation.common.RandomSource;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

public class CompanyAgent implements CountryAgent {

    private static final int NUM_COMPANIES = 5;
    private static final LogWrapper<World.Country> LOG = new LogWrapper<>(LoggerFactory.getLogger(PersonBirthAgent.class), World.Country::getTracker);


    @Override
    public void iterate(AgentContext context, RandomSource randomSource, World.Country country) {
        List<String> companyNameAdjectives = context.getWorld().getAdjectives();
        List<String> companyNameNouns = context.getWorld().getNouns();
        Random random = randomSource.startNewRandom();

        String sessionKey = country.getContinent().getName();
        GraknClient.Session session = context.getIterationGraknSessionFor(sessionKey);

        try ( GraknClient.Transaction tx = session.transaction().write()) {
            for (int i = 0; i < NUM_COMPANIES; i++) {
                insertCompany(tx, country, context, random, companyNameAdjectives, companyNameNouns, i);
            }
            tx.commit();
        }
    }

    private void insertCompany(GraknClient.Transaction tx, World.Country country, AgentContext context, Random random, List<String> companyNameAdjectives, List<String> companyNameNouns, int i) {
        String adjective = companyNameAdjectives.get(random.nextInt(companyNameAdjectives.size()));
        String noun = companyNameNouns.get(random.nextInt(companyNameNouns.size()));
        LocalDateTime dateToday = context.getLocalDateTime();

        String companyName = StringUtils.capitalize(adjective) + StringUtils.capitalize(noun);

        String companyNumberString = companyName
                + i
                + context.getSimulationStep() + "_"
                + country.getName();
        int companyRegistrationNumber = companyNumberString.hashCode();

        GraqlInsert query =
                Graql.match(
                        Graql.var("country").isa("country")
                                .has("name", country.getName()))
                        .insert(Graql.var("company").isa("company")
                                        .has("company-name", companyName)
                                        .has("company-number", companyRegistrationNumber),
                                Graql.var("reg").isa("company-incorporation")
                                        .rel("incorporated-company", Graql.var("company"))
                                        .rel("incorporating-country", Graql.var("country"))
                                        .has("date-of-incorporation", dateToday)
                        );
        LOG.query(country, "insertCompany", query);
        tx.execute(query);
    }
}