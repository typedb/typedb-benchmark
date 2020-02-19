package grakn.simulation.agents;

import grakn.client.GraknClient;
import grakn.simulation.common.DeterministicExecute;
import grakn.simulation.common.LogWrapper;
import grakn.simulation.common.RandomSource;
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import static grakn.simulation.common.Allocation.doAllocation;

public class EmploymentAgent implements CityAgent {

    private static final int NUM_EMPLOYMENTS = 5;
    private static final int NUM_COMPANIES = 5;
    private static final LogWrapper<World.City> LOG = new LogWrapper<>(LoggerFactory.getLogger(PersonBirthAgent.class), World.City::getTracker);
    private static final double MIN_ANNUAL_WAGE = 18000.00;
    private static final double MAX_ANNUAL_WAGE = 80000.00;
    private static final double MIN_CONTRACTED_HOURS = 30.0;
    private static final double MAX_CONTRACTED_HOURS = 70.0;
    private static final int MIN_CONTRACT_CHARACTER_LENGTH = 200;
    private static final int MAX_CONTRACT_CHARACTER_LENGTH = 600;



    private static class RandomValueGenerator {
        private final Random random;

        private static double doubleInterpolate(double in, double min, double max) {
            return (in * (max - min)) + min;
        }

        private static int boundRandomInt(Random random, int min, int max) {
            return random.nextInt(max - min) + min;
        }

        private RandomValueGenerator(Random random) {
            this.random = random;
        }

        private String getContractContent() {
            return RandomStringUtils.random(boundRandomInt(random, MIN_CONTRACT_CHARACTER_LENGTH, MAX_CONTRACT_CHARACTER_LENGTH), 0, Character.MAX_CODE_POINT, true, true, null, random);
        }

        private double getContractedHours() {
            return doubleInterpolate(random.nextDouble(), MIN_CONTRACTED_HOURS, MAX_CONTRACTED_HOURS);
        }

        private double getAnnualWage() {
            return doubleInterpolate(random.nextDouble(), MIN_ANNUAL_WAGE, MAX_ANNUAL_WAGE);
        }
    }

    @Override
    public void iterate(AgentContext context, RandomSource randomSource, World.City city) {
        String sessionKey = city.getCountry().getContinent().getName();
        GraknClient.Session session = context.getIterationGraknSessionFor(sessionKey);

        Random random = randomSource.startNewRandom();
        RandomValueGenerator randomAttributeGenerator = new RandomValueGenerator(random);

        LocalDateTime employmentDate = context.getLocalDateTime().minusYears(2);

        try ( GraknClient.Transaction tx = session.transaction().write()) {
            List<String> employeeEmails = getEmployeeEmails(tx, city, employmentDate);
            List<Long> companyNumbers = getCompanyNumbers(tx, city);

            doAllocation(employeeEmails, companyNumbers, (employeeEmail, companyNumber) -> insertEmployment(tx, employmentDate, city, randomAttributeGenerator, employeeEmail, companyNumber));
            tx.commit();
        }
    }

    private List<Long> getCompanyNumbers(GraknClient.Transaction tx, World.City city) {
        GraqlGet companyNumbersQuery = CompanyAgent.getCompanyNumbersInCountryQuery(city.getCountry());
        LOG.query(city, "getEmployeeEmails", companyNumbersQuery);

        return DeterministicExecute.getAttributeAnswers(tx, companyNumbersQuery, "company-number", NUM_COMPANIES);
    }

    private List<String> getEmployeeEmails(GraknClient.Transaction tx, World.City city, LocalDateTime earliestDate) {

        GraqlGet.Unfiltered getEmployeeEmailsQuery = PersonBirthAgent.cityResidentsQuery(city, earliestDate);
        LOG.query(city, "getEmployeeEmails", getEmployeeEmailsQuery);
        return DeterministicExecute.getAttributeAnswers(tx, getEmployeeEmailsQuery, "email", NUM_EMPLOYMENTS);
    }

    private void insertEmployment(GraknClient.Transaction tx, LocalDateTime employmentDate, World.City city, RandomValueGenerator randomAttributeGenerator, String employeeEmail, Long companyNumber){
        GraqlInsert insertEmploymentQuery = Graql.match(
                Graql.var("city").isa("city").has("name", city.getName()),
                Graql.var("p").isa("person").has("email", employeeEmail),
                Graql.var("company").isa("company").has("company-number", companyNumber)
        ).insert(
                Graql.var("emp").isa("employment")
                        .rel("employment_employee", Graql.var("p"))
                        .rel("employment_employer", Graql.var("company"))
                        .rel("employment_contract", Graql.var("contract"))
                        .has("start-date", employmentDate)
                        .has("annual-wage", randomAttributeGenerator.getAnnualWage()),
                Graql.var("locates").isa("locates")
                        .rel("locates_located", Graql.var("emp"))
                        .rel("locates_location", Graql.var("city")),
                Graql.var("contract").isa("employment-contract")
                        .has("contract-content", randomAttributeGenerator.getContractContent())
                        .has("contracted-hours", randomAttributeGenerator.getContractedHours())
        );
        LOG.query(city, "insertEmployment", insertEmploymentQuery);
        tx.execute(insertEmploymentQuery);
    }
}
