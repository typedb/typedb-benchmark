package grakn.simulation.db.grakn.agents.interaction;

import grakn.simulation.db.common.agents.interaction.EmploymentAgent;
import grakn.simulation.db.grakn.context.GraknDriver;

//public class GraknEmploymentAgent extends EmploymentAgent<GraknDriver> {
//    public GraknEmploymentAgent(GraknDriver dbDriver, SessionStrategy sessionStrategy) {
//        super(dbDriver, sessionStrategy);
//    }

    //    @Override
//    public List<Long> getCompanyNumbers(World.Country country, int numCompanies) {
//        GraqlGet companyNumbersQuery = CompanyAgent.getCompanyNumbersInCountryQuery(country);
//        return tx().getOrderedAttribute(companyNumbersQuery, COMPANY_NUMBER, numCompanies);
//    }
//
//    @Override
//    public List<String> getEmployeeEmails(World.City city, int numEmployments, LocalDateTime earliestDate) {
//        GraqlGet getEmployeeEmailsQuery = cityResidentsQuery(city, earliestDate);
//        return tx().getOrderedAttribute(getEmployeeEmailsQuery, EMAIL, numEmployments);
//    }
//
//    @Override
//    public ActionResult insertEmployment(World.City worldCity, String employeeEmail, long companyNumber, LocalDateTime employmentDate, double wageValue, String contractContent, double contractedHours) {
//        Statement city = Graql.var(CITY);
//        Statement person = Graql.var(PERSON);
//        Statement company = Graql.var(COMPANY);
//        Statement country = Graql.var(COUNTRY);
//        Statement locationHierarchy = Graql.var(LOCATION_HIERARCHY);
//        Statement employment = Graql.var(EMPLOYMENT);
//        Statement wage = Graql.var(WAGE);
//        Statement locates = Graql.var(LOCATES);
//        Statement contract = Graql.var(CONTRACT);
//        Statement currency = Graql.var(CURRENCY);
//
//        GraqlInsert insertEmploymentQuery = Graql.match(
//                city
//                        .isa(CITY)
//                        .has(LOCATION_NAME, worldCity.name()),
//                person
//                        .isa(PERSON)
//                        .has(EMAIL, employeeEmail),
//                company
//                        .isa(COMPANY)
//                        .has(COMPANY_NUMBER, companyNumber),
//                country
//                        .isa(COUNTRY)
//                        .has(CURRENCY, currency),
//                locationHierarchy
//                        .isa(LOCATION_HIERARCHY)
//                        .rel(city)
//                        .rel(country)
//        ).insert(
//                employment
//                        .isa(EMPLOYMENT)
//                        .rel(EMPLOYMENT_EMPLOYEE, person)
//                        .rel(EMPLOYMENT_EMPLOYER, company)
//                        .rel(EMPLOYMENT_CONTRACT, contract)
//                        .rel(EMPLOYMENT_WAGE, wage)
//                        .has(START_DATE, employmentDate),
//                wage
//                        .isa(WAGE)
//                        .has(WAGE_VALUE, wageValue)
//                        .has(CURRENCY, currency), //TODO Should this be inferred rather than inserted?
//                locates
//                        .isa(LOCATES)
//                        .rel(LOCATES_LOCATION, city)
//                        .rel(LOCATES_LOCATED, employment),
//                contract
//                        .isa(CONTRACT)
//                        .has(CONTRACT_CONTENT, contractContent)
//                        .has(CONTRACTED_HOURS, contractedHours)
//        );
//        return Action.singleResult(tx().execute(insertEmploymentQuery));
//    }
//
////    @Override
////    public ActionResult resultsForTesting(ConceptMap answer) {
////        return new ActionResult() {
////            {
////                put(EmploymentAgentField.CITY_NAME, tx().getOnlyAttributeOfThing(answer, CITY, LOCATION_NAME));
////                put(EmploymentAgentField.PERSON_EMAIL, tx().getOnlyAttributeOfThing(answer, PERSON, EMAIL));
////                put(EmploymentAgentField.COMPANY_NUMBER, tx().getOnlyAttributeOfThing(answer, COMPANY, COMPANY_NUMBER));
////                put(EmploymentAgentField.START_DATE, tx().getOnlyAttributeOfThing(answer, EMPLOYMENT, START_DATE));
////                put(EmploymentAgentField.WAGE, tx().getOnlyAttributeOfThing(answer, WAGE, WAGE_VALUE));
////                put(EmploymentAgentField.CURRENCY, tx().getOnlyAttributeOfThing(answer, WAGE, CURRENCY));
////                put(EmploymentAgentField.CONTRACT_CONTENT, tx().getOnlyAttributeOfThing(answer, CONTRACT, CONTRACT_CONTENT));
////                put(EmploymentAgentField.CONTRACTED_HOURS, tx().getOnlyAttributeOfThing(answer, CONTRACT, CONTRACTED_HOURS));
////            }
////        };
////    }
//}
