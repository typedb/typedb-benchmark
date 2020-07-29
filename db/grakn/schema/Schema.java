package grakn.simulation.db.grakn.schema;

public class Schema {
    public static final String CITY = "city";
    public static final String COUNTRY = "country";
    public static final String CONTINENT = "continent";

    public static final String COMPANY = "company";
    public static final String COMPANY_NAME = "company-name";
    public static final String COMPANY_NUMBER = "company-number";

    public static final String INCORPORATION = "incorporation";
    public static final String INCORPORATION_INCORPORATED = "incorporation-incorporated";
    public static final String INCORPORATION_INCORPORATING = "incorporation-incorporating";
    public static final String DATE_OF_INCORPORATION = "date-of-incorporation";


    public static final String PERSON = "person";
    public static final String EMAIL = "email";
    public static final String DATE_OF_BIRTH = "date-of-birth";
    public static final String GENDER = "gender";
    public static final String FORENAME = "forename";
    public static final String SURNAME = "surname";

    public static final String MARRIAGE = "marriage";
    public static final String MARRIAGE_HUSBAND = "marriage_husband";
    public static final String MARRIAGE_WIFE = "marriage_wife";
    public static final String MARRIAGE_ID = "marriage-id";

    public static final String BORN_IN = "born-in";
    public static final String BORN_IN_CHILD = "born-in_child";
    public static final String BORN_IN_PLACE_OF_BIRTH = "born-in_place-of-birth";

    public static final String RELOCATION = "relocation";
    public static final String RELOCATION_PREVIOUS_LOCATION = "relocation_previous-location";
    public static final String RELOCATION_NEW_LOCATION = "relocation_new-location";
    public static final String RELOCATION_RELOCATED_PERSON = "relocation_relocated-person";
    public static final String RELOCATION_DATE = "relocation-date";

    public static final String RESIDENCY = "residency";
    public static final String RESIDENCY_LOCATION = "residency_location";
    public static final String RESIDENCY_RESIDENT = "residency_resident";
    public static final String START_DATE = "start-date";
    public static final String END_DATE = "end-date";

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

    public static final String CONTRACT = "contract";

    public static final String WAGE = "wage";
    public static final String WAGE_VALUE = "wage-value";

    public static final String FRIENDSHIP = "friendship";
    public static final String FRIENDSHIP_FRIEND = "friendship_friend";

    public static final String PARENTSHIP = "parentship";
    public static final String PARENTSHIP_PARENT = "parentship_parent";
    public static final String PARENTSHIP_CHILD = "parentship_child";

    public static final String PRODUCT = "product";
    public static final String PRODUCT_BARCODE = "product-barcode";
    public static final String PRODUCT_NAME = "product-name";
    public static final String PRODUCT_DESCRIPTION = "product-description";

    public static final String PRODUCED_IN = "produced-in";
    public static final String PRODUCED_IN_PRODUCT = "produced-in_product";
    public static final String PRODUCED_IN_CONTINENT = "produced-in_continent";

    public static final String TRANSACTION = "transaction";
    public static final String TRANSACTION_BUYER = "transaction_buyer";
    public static final String TRANSACTION_SELLER = "transaction_seller";
    public static final String TRANSACTION_MERCHANDISE = "transaction_merchandise";
    public static final String VALUE = "value";
    public static final String PRODUCT_QUANTITY = "product-quantity";
    public static final String IS_TAXABLE = "is-taxable";


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
