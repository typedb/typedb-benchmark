package grakn.simulation.neo4j.action;

public class Model {

    public static final String CITY = "city";
    public static final String COUNTRY = "country";

    public static final String COMPANY_NAME = "companyName";
    public static final String COMPANY_NUMBER = "companyNumber";

    public static final String DATE_OF_INCORPORATION = "dateOfIncorporation";

    public static final String EMAIL = "email";
    public static final String DATE_OF_BIRTH = "dateOfBirth";
    public static final String GENDER = "gender";
    public static final String FORENAME = "forename";
    public static final String SURNAME = "surname";

    public static final String MARRIAGE_ID = "marriageId";

    public static final String BORN_IN = "born-in";
    public static final String RELOCATION_DATE = "relocationDate";

    public static final String START_DATE = "startDate";
    public static final String LOCATION_NAME = "locationName";

    public static final String CURRENCY = "currency";
    public static final String CONTRACT_CONTENT = "contractContent";
    public static final String CONTRACTED_HOURS = "contractedHours";

    public static final String WAGE = "wage";
    public static final String PRODUCT_BARCODE = "product-barcode";
    public static final String PRODUCT_NAME = "product-name";
    public static final String PRODUCT_DESCRIPTION = "product-description";

    public static final String VALUE = "value";
    public static final String PRODUCT_QUANTITY = "product-quantity";
    public static final String IS_TAXABLE = "is-taxable";

    private String name;

    Model(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
