package org.ldbcouncil.snb.impls.workloads.typeql;

import org.ldbcouncil.snb.driver.DbException;
import org.ldbcouncil.snb.driver.control.LoggingService;
import org.ldbcouncil.snb.driver.workloads.interactive.queries.*;
import org.ldbcouncil.snb.driver.workloads.interactive.queries.LdbcInsert1AddPerson.Organization;
import org.ldbcouncil.snb.impls.workloads.QueryType;
import org.ldbcouncil.snb.impls.workloads.db.BaseDb;
import org.ldbcouncil.snb.impls.workloads.typeql.operationhandlers.*;
import com.vaticle.typedb.driver.api.answer.JSON;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.ZoneId;



public abstract class TypeQLDb extends BaseDb<TypeQLQueryStore> {

    @Override
    protected void onInit(Map<String, String> properties, LoggingService loggingService) throws DbException {
        try {
            dcs = new TypeQLDbConnectionState<TypeQLQueryStore>(properties, new TypeQLQueryStore(properties.get("queryDir")));
        } catch (ClassNotFoundException e) {
            throw new DbException(e);
        }
    }

    // Interactive Complex Reads
    
    // public static class InteractiveQuery1 extends TypeQLListOperationHandler<LdbcQuery1,LdbcQuery1Result>
    // {

    //     @Override
    //     public String getQueryString(TypeQLDbConnectionState state, LdbcQuery1 operation) {
    //         return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery1);
    //     }

    //     @Override
    //     public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery1 operation) {
    //         return state.getQueryStore().getQuery1Map(operation);
    //     }

    //     @Override
    //     public LdbcQuery1Result toResult(JSON result) throws ParseException {
    //         if (result != null) {
    //             Map<String, JSON> jsonMap = result.asObject();

    //             // Extracting individual attributes
    //             long friendId = (long)jsonMap.get("friendId").asObject().get("value").asNumber();
    //             String friendLastName = jsonMap.get("friendLastName").asObject().get("value").asString();
    //             int distanceFromPerson = (int)jsonMap.get("distance").asObject().get("value").asNumber();
    //             long friendBirthday = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(jsonMap.get("friendBirthday").asObject().get("value").asString()).getTime();
    //             long friendCreationDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(jsonMap.get("friendCreationDate").asObject().get("value").asString()).getTime();
    //             String friendGender = jsonMap.get("friendGender").asObject().get("value").asString();
    //             String friendBrowserUsed = jsonMap.get("friendBrowserUsed").asObject().get("value").asString();
    //             String friendLocationIp = jsonMap.get("friendLocationIP").asObject().get("value").asString();
    //             String friendCityName = jsonMap.get("friendCityName").asObject().get("value").asString();

    //             // Extracting lists
    //             Iterable<String> emails = Arrays.asList(jsonMap.get("friendEmail").asArray().get(0).asObject().get("value").asString().split(";"));
    //             Iterable<String> languages = jsonMap.get("friendLanguages").asArray().stream()
    //                                             .map(lang -> lang.asObject().get("value").asString())
    //                                             .collect(Collectors.toList());
    //             // Assuming JSON structure for universities and companies
    //             Iterable<LdbcQuery1Result.Organization> universities = jsonMap.get("friendUniversity").asArray().stream()
    //                 .map(uniJson -> {
    //                     String universityName = uniJson.asObject().get("universityName").asObject().get("value").asString();
    //                     int classYear = (int)uniJson.asObject().get("classYear").asObject().get("value").asNumber();
    //                     String uniCityName = uniJson.asObject().get("uniCityName").asObject().get("value").asString();
    //                     return new LdbcQuery1Result.Organization(universityName, classYear, uniCityName);
    //                 })
    //                 .collect(Collectors.toList());

    //             Iterable<LdbcQuery1Result.Organization> companies = jsonMap.get("friendCompany").asArray().stream()
    //                 .map(orgJson -> new LdbcQuery1Result.Organization(
    //                     orgJson.asObject().get("organizationName").asString(),
    //                     (int)orgJson.asObject().get("year").asNumber(),
    //                     orgJson.asObject().get("placeName").asString()
    //                 ))
    //                 .collect(Collectors.toList());

    //             // Constructing the result
    //             return new LdbcQuery1Result(
    //                     friendId,
    //                     friendLastName,
    //                     distanceFromPerson,
    //                     friendBirthday,
    //                     friendCreationDate,
    //                     friendGender,
    //                     friendBrowserUsed,
    //                     friendLocationIp,
    //                     emails,
    //                     languages,
    //                     friendCityName,
    //                     universities,
    //                     companies
    //             );
    //         } else {
    //             return null;
    //         }
    //     }

    // }

    public static class InteractiveQuery2 extends TypeQLListOperationHandler<LdbcQuery2,LdbcQuery2Result>
    {

        @Override
        public String getQueryString(TypeQLDbConnectionState state, LdbcQuery2 operation) {
            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery2);
        }

        @Override
        public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery2 operation) {
            return state.getQueryStore().getQuery2Map(operation);
        }

        @Override
        public LdbcQuery2Result toResult(JSON result) throws ParseException {
            if (result != null) {
                Map<String, JSON> jsonMap = result.asObject();
    
                // Extracting individual attributes
                long friendId = (long) jsonMap.get("friendId").asObject().get("value").asNumber();
                String name = jsonMap.get("friendFirstName").asObject().get("value").asString();
                String surname = jsonMap.get("friendLastName").asObject().get("value").asString();
                long messageId = (long) jsonMap.get("messageId").asObject().get("value").asNumber();
                String messageContent = jsonMap.get("messageContent").asObject().get("value").asString();
                long messageCreationDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
                        .parse(jsonMap.get("messageCreationDate").asObject().get("value").asString()).getTime();
    
                // Constructing the result
                return new LdbcQuery2Result(
                    friendId,
                    name,
                    surname,
                    messageId,
                    messageContent,
                    messageCreationDate
                );
            } else {
                return null;
            }
        }
    }

    // // public static class InteractiveQuery4 extends TypeQLListOperationHandler<LdbcQuery4,LdbcQuery4Result>
    // // {

    // //     @Override
    // //     public String getQueryString(TypeQLDbConnectionState state, LdbcQuery4 operation) {
    // //         return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery4);
    // //     }

    // //     @Override
    // //     public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery4 operation) {
    // //         return state.getQueryStore().getQuery4Map(operation);
    // //     }

    // //     @Override
    // //     public LdbcQuery4Result toResult(ConceptMap result) throws ParseException {
    // //         if (result != null) {
                

    // //             return new LdbcQuery4Result(
    // //                 friendId,
    // //                 name,
    // //                 surname,
    // //                 messageId,
    // //                 messageContent,
    // //                 messageCreationDate
    // //             );
    // //         } else {
    // //             return null;
    // //         }
    // //     }
    // // }

    // public static class InteractiveQuery7 extends TypeQLListOperationHandler<LdbcQuery7,LdbcQuery7Result>
    // {

    //     @Override
    //     public String getQueryString(TypeQLDbConnectionState state, LdbcQuery7 operation) {
    //         return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery7);
    //     }

    //     @Override
    //     public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery7 operation) {
    //         return state.getQueryStore().getQuery7Map(operation);
    //     }

    //     @Override
    //     public LdbcQuery7Result toResult(ConceptMap result) throws ParseException {
    //         if (result != null) {
                
    //             long likerId = result.get("likerId").asAttribute().asLong().getValue();
    //             String likerFirstName = result.get("likerFirstName").asAttribute().asString().getValue();
    //             String likerLastName = result.get("likerLastName").asAttribute().asString().getValue();
    //             long likesDate = result.get("likesDate").asAttribute().asDateTime().getValue().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    //             long messageId = result.get("messageId").asAttribute().asLong().getValue();
    //             String messageContent = result.get("messageContent").asValue().asString().getValue();
    //             int minutesLatency = (int)((likesDate - result.get("date").asAttribute().asDateTime().getValue().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())/60000);
    //             boolean isNew = result.get("isNew").asValue().asBoolean().getValue();
    //             return new LdbcQuery7Result(
    //                 likerId,
    //                 likerFirstName,
    //                 likerLastName,
    //                 likesDate,
    //                 messageId,
    //                 messageContent,
    //                 minutesLatency,
    //                 isNew
    //             );
    //         } else {
    //             return null;
    //         }
    //     }
    // }

    // public static class InteractiveQuery8 extends TypeQLListOperationHandler<LdbcQuery8,LdbcQuery8Result>
    // {

    //     @Override
    //     public String getQueryString(TypeQLDbConnectionState state, LdbcQuery8 operation) {
    //         return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery8);
    //     }

    //     @Override
    //     public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery8 operation) {
    //         return state.getQueryStore().getQuery8Map(operation);
    //     }

    //     @Override
    //     public LdbcQuery8Result toResult(ConceptMap result) throws ParseException {
    //         if (result != null) {
                
    //             long authorId = result.get("authorId").asAttribute().asLong().getValue();
    //             String firstname = result.get("firstname").asAttribute().asString().getValue();
    //             String lastname = result.get("lastname").asAttribute().asString().getValue();
    //             long date = result.get("date").asAttribute().asDateTime().getValue().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    //             long replyId = result.get("replyId").asAttribute().asLong().getValue();
    //             String content = result.get("content").asAttribute().asString().getValue();

    //             return new LdbcQuery8Result(
    //                 authorId,
    //                 firstname,
    //                 lastname,
    //                 date,
    //                 replyId,
    //                 content
    //             );
    //         } else {
    //             return null;
    //         }
    //     }
    // }

    // public static class InteractiveQuery9 extends TypeQLListOperationHandler<LdbcQuery9,LdbcQuery9Result>
    // {

    //     @Override
    //     public String getQueryString(TypeQLDbConnectionState state, LdbcQuery9 operation) {
    //         return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery9);
    //     }

    //     @Override
    //     public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery9 operation) {
    //         return state.getQueryStore().getQuery9Map(operation);
    //     }

    //     @Override
    //     public LdbcQuery9Result toResult(ConceptMap result) throws ParseException {
    //         if (result != null) {
                
    //             long otherId = result.get("other-id").asAttribute().asLong().getValue();
    //             String firstname = result.get("firstname").asAttribute().asString().getValue();
    //             String lastname = result.get("lastname").asAttribute().asString().getValue();
    //             long messageId = result.get("message-id").asAttribute().asLong().getValue();
    //             String messageContent = result.get("messageContent").asValue().asString().getValue();
    //             long date = result.get("date").asAttribute().asDateTime().getValue().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

    //             return new LdbcQuery9Result(
    //                 otherId,
    //                 firstname,
    //                 lastname,
    //                 messageId,
    //                 messageContent,
    //                 date
    //             );
    //         } else {
    //             return null;
    //         }
    //     }
    // }

    // Interactive short reads
    public static class ShortQuery1PersonProfile extends TypeQLSingletonOperationHandler<LdbcShortQuery1PersonProfile,LdbcShortQuery1PersonProfileResult>
    {
        @Override
        public String getQueryString(TypeQLDbConnectionState state, LdbcShortQuery1PersonProfile operation) {
            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveShortQuery1);
        }

        @Override
        public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcShortQuery1PersonProfile operation) {
            return state.getQueryStore().getShortQuery1PersonProfileMap(operation);
        }

        @Override
        public LdbcShortQuery1PersonProfileResult toResult(JSON result) throws ParseException {
            if (result != null) {
                Map<String, JSON> jsonMap = result.asObject();
        
                // Extracting and parsing each field
                String firstName = jsonMap.get("firstName").asObject().get("value").asString();
                String lastName = jsonMap.get("lastName").asObject().get("value").asString();
        
                // Parsing birthday and creationDate as long
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                long birthday = dateFormat.parse(jsonMap.get("birthday").asObject().get("value").asString()).getTime();
                long creationDate = dateFormat.parse(jsonMap.get("creationDate").asObject().get("value").asString()).getTime();
        
                String locationIP = jsonMap.get("locationIP").asObject().get("value").asString();
                String browserUsed = jsonMap.get("browserUsed").asObject().get("value").asString();
                String cityIdString = jsonMap.get("cityId").asObject().get("value").asString();
                long cityId = Long.parseLong(cityIdString);
                String gender = jsonMap.get("gender").asObject().get("value").asString();
        
                // Creating the result object with dates as long
                return new LdbcShortQuery1PersonProfileResult(
                        firstName,
                        lastName,
                        birthday,
                        locationIP,
                        browserUsed,
                        cityId,
                        gender,
                        creationDate );
            } else {
                return null;
            }
        }        
    }
}
