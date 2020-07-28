package grakn.simulation.db.grakn.agents.interaction;

public class Schema {
    public static final String CITY = "city";
    public static final String COUNTRY = "country";
    public static final String COMPANY = "company";
    public static final String COMPANY_NAME = "company-name";
    public static final String COMPANY_NUMBER = "company-number";

    public static final String INCORPORATION = "incorporation";

    public static final String PERSON = "person";
    public static final String EMAIL = "email";
    public static final String DATE_OF_BIRTH = "date-of-birth";
    public static final String GENDER = "gender";
    public static final String FORENAME = "forename";
    public static final String SURNAME = "surname";
    public static final String BORN_IN = "born-in";

    public static final String RELOCATION = "relocation";

    public static final String RESIDENCY = "residency";
    public static final String START_DATE = "start-date";

    public static final String LOCATION_HIERARCHY = "location-hierarchy";
    public static final String LOCATION_NAME = "location-name";

    public static final String LOCATES = "locates";
    public static final String LOCATES_LOCATED = "locates_located";
    public static final String LOCATES_LOCATION = "locates_location";

    public static final String CURRENCY = "currency";
    public static final String EMPLOYMENT = "employment";
    public static final String EMPLOYMENT_EMPLOYEE = "employment_employee";
    public static final String EMPLOYMENT_EMPLOYER = "employment_employer";
    public static final String EMPLOYMENT_CONTRACT = "employment_contract";
    public static final String EMPLOYMENT_WAGE = "employment_wage";
    public static final String CONTRACT_CONTENT = "contract-content";
    public static final String CONTRACTED_HOURS = "contracted-hours";

    public static final String WAGE = "wage";
    public static final String WAGE_VALUE = "wage-value";


    private String name;

    Schema(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
