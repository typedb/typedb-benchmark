package org.ldbcouncil.snb.impls.workloads.typeql;

import com.vaticle.typedb.driver.api.TypeDBTransaction;
import com.vaticle.typedb.driver.api.answer.ConceptMap;
import com.vaticle.typedb.driver.api.answer.ValueGroup;
import com.vaticle.typedb.driver.api.concept.type.AttributeType;
import org.ldbcouncil.snb.driver.DbException;
import org.ldbcouncil.snb.driver.ResultReporter;
import org.ldbcouncil.snb.driver.control.LoggingService;
import org.ldbcouncil.snb.driver.workloads.interactive.queries.*;
import org.ldbcouncil.snb.impls.workloads.QueryType;
import org.ldbcouncil.snb.impls.workloads.db.BaseDb;
import org.ldbcouncil.snb.impls.workloads.typeql.operationhandlers.*;
import com.vaticle.typedb.driver.api.answer.JSON;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;


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
    
    public static class InteractiveQuery1 extends TypeQLListOperationHandler<LdbcQuery1,LdbcQuery1Result>
    {

         @Override
         public String getQueryString(TypeQLDbConnectionState state, LdbcQuery1 operation) {
             return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery1);
         }

         @Override
         public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery1 operation) {
             return state.getQueryStore().getQuery1Map(operation);
         }

         @Override
         public LdbcQuery1Result toResult(JSON result) throws ParseException {
             if (result != null) {
                 Map<String, JSON> jsonMap = result.asObject();

                 // Extracting individual attributes
                 long friendId = (long)jsonMap.get("friendId").asObject().get("value").asNumber();
                 String friendLastName = jsonMap.get("friendLastName").asObject().get("value").asString();
                 int distanceFromPerson = (int)jsonMap.get("distance").asObject().get("value").asNumber();
                 long friendBirthday = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(jsonMap.get("friendBirthday").asObject().get("value").asString()).getTime();
                 long friendCreationDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(jsonMap.get("friendCreationDate").asObject().get("value").asString()).getTime();
                 String friendGender = jsonMap.get("friendGender").asObject().get("value").asString();
                 String friendBrowserUsed = jsonMap.get("friendBrowserUsed").asObject().get("value").asString();
                 String friendLocationIp = jsonMap.get("friendLocationIP").asObject().get("value").asString();
                 String friendCityName = jsonMap.get("friendCityName").asObject().get("value").asString();

                 // Extracting lists
                 Iterable<String> emails = jsonMap.get("friendEmail").asArray().stream()
                         .map(emailJson -> emailJson.asObject().get("email").asObject().get("value").asString())
                         .collect(Collectors.toList());
                 Iterable<String> languages = jsonMap.get("friendLanguages").asArray().stream()
                                                 .map(lang -> lang.asObject().get("language").asObject().get("value").asString())
                                                 .collect(Collectors.toList());
                 // Assuming JSON structure for universities and companies
                 Iterable<LdbcQuery1Result.Organization> universities = jsonMap.get("friendUniversity").asArray().stream()
                     .map(uniJson -> new LdbcQuery1Result.Organization(
                             uniJson.asObject().get("universityName").asObject().get("value").asString(),
                             (int) uniJson.asObject().get("classYear").asObject().get("value").asNumber(),
                             uniJson.asObject().get("uniCityName").asObject().get("value").asString()
                     ))
                     .collect(Collectors.toList());

                 Iterable<LdbcQuery1Result.Organization> companies = jsonMap.get("friendCompany").asArray().stream()
                     .map(orgJson -> new LdbcQuery1Result.Organization(
                         orgJson.asObject().get("companyName").asObject().get("value").asString(),
                         (int) orgJson.asObject().get("workFrom").asObject().get("value").asNumber(),
                         orgJson.asObject().get("companyCityName").asObject().get("value").asString()
                     ))
                     .collect(Collectors.toList());

                 // Constructing the result
                 return new LdbcQuery1Result(
                         friendId,
                         friendLastName,
                         distanceFromPerson,
                         friendBirthday,
                         friendCreationDate,
                         friendGender,
                         friendBrowserUsed,
                         friendLocationIp,
                         emails,
                         languages,
                         friendCityName,
                         universities,
                         companies
                 );
             } else {
                 return null;
             }
         }
     }

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

    public static class InteractiveQuery3a extends TypeQLListOperationHandler<LdbcQuery3a, LdbcQuery3Result> {

        @Override
        public String getQueryString(TypeQLDbConnectionState state, LdbcQuery3a operation) {
            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery3);
        }

        @Override
        public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery3a operation) {
            Map<String, Object> parameters = new HashMap(state.getQueryStore().getQuery3Map(operation));
            LocalDate start_date = LocalDate.parse(parameters.get("startDate").toString().replace("\'",""), DateTimeFormatter.ISO_LOCAL_DATE);
            String daysToAddStr = parameters.get("durationDays").toString();
            int daysToAdd = Integer.parseInt(daysToAddStr);
            LocalDate end_date = start_date.plusDays(daysToAdd);
            parameters.remove("durationDays");
            parameters.put("endDate", end_date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            return parameters;
        }

        @Override
        public LdbcQuery3Result toResult(JSON result) throws ParseException {
            if (result != null) {
                Map<String, JSON> jsonMap = result.asObject();

                int xCount = (int) result.asObject().get("xCount").asObject().get("value").asNumber();
                int yCount = (int) result.asObject().get("yCount").asObject().get("value").asNumber();
                // Constructing the result
                return new LdbcQuery3Result(
                        (int) result.asObject().get("otherPerson_id").asObject().get("value").asNumber(),
                        result.asObject().get("otherPerson_firstName").asObject().get("value").asString(),
                        result.asObject().get("otherPerson_lastName").asObject().get("value").asString(),
                        xCount,
                        yCount,
                        xCount + yCount
                );
            } else {
                return null;
            }
        }
    }

    public static class InteractiveQuery3b extends TypeQLListOperationHandler<LdbcQuery3b, LdbcQuery3Result> {

        @Override
        public String getQueryString(TypeQLDbConnectionState state, LdbcQuery3b operation) {
            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery3);
        }

        @Override
        public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery3b operation) {
            Map<String, Object> parameters = new HashMap(state.getQueryStore().getQuery3Map(operation));
            LocalDate start_date = LocalDate.parse(parameters.get("startDate").toString().replace("\'",""), DateTimeFormatter.ISO_LOCAL_DATE);
            String daysToAddStr = parameters.get("durationDays").toString();
            int daysToAdd = Integer.parseInt(daysToAddStr);
            LocalDate end_date = start_date.plusDays(daysToAdd);
            parameters.remove("durationDays");
            parameters.put("endDate", end_date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            return parameters;
        }

        @Override
        public LdbcQuery3Result toResult(JSON result) throws ParseException {
            if (result != null) {
                Map<String, JSON> jsonMap = result.asObject();

                int xCount = (int) result.asObject().get("xCount").asObject().get("value").asNumber();
                int yCount = (int) result.asObject().get("yCount").asObject().get("value").asNumber();
                // Constructing the result
                return new LdbcQuery3Result(
                        (int) result.asObject().get("otherPerson_id").asObject().get("value").asNumber(),
                        result.asObject().get("otherPerson_firstName").asObject().get("value").asString(),
                        result.asObject().get("otherPerson_lastName").asObject().get("value").asString(),
                        xCount,
                        yCount,
                        xCount + yCount
                );
            } else {
                return null;
            }
        }
    }
    public static class InteractiveQuery4 extends TypeQLListOperationHandler<LdbcQuery4, LdbcQuery4Result> {

        final int LIMIT = 10;

        @Override
        public String getQueryString(TypeQLDbConnectionState state, LdbcQuery4 operation) {
            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery4);
        }

        @Override
        public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery4 operation) {
            Map<String, Object> parameters = new HashMap<String, Object>(state.getQueryStore().getQuery4Map(operation));
            LocalDate start_date = LocalDate.parse(parameters.get("startDate").toString().replace("\'",""), DateTimeFormatter.ISO_LOCAL_DATE);
            String daysToAddStr = parameters.get("durationDays").toString();
            int daysToAdd = Integer.parseInt(daysToAddStr);
            LocalDate end_date = start_date.plusDays(daysToAdd);
            parameters.remove("durationDays");
            parameters.put("endDate", end_date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            return parameters;
        }

        public LdbcQuery4Result toLdbcResult(List<Comparable> result) {
            return new LdbcQuery4Result(
                    (String) result.get(0),
                    (int) result.get(1)
            );
        }

        public List<Comparable> toListResult(JSON result) throws ParseException {
            return Arrays.asList(
                    result.asObject().get("tag_name").asObject().get("value").asString(),
                    (int) result.asObject().get("postCount").asObject().get("value").asNumber()
            );
        }

        class Query4Comparator implements Comparator<List<Comparable>> {
            @Override
            public int compare(List<Comparable> o1, List<Comparable> o2) {
                int comparison = -o1.get(1).compareTo(o2.get(1)); // Sort by postCount in descending order
                if (comparison != 0) {
                    return comparison;
                }
                return o1.get(0).compareTo(o2.get(0)); // Otherwise sort by tag_name
            }
        }

        @Override
        public void executeOperation(LdbcQuery4 operation, TypeQLDbConnectionState state,
                                     ResultReporter resultReporter) throws DbException
        {
            String query = getQueryString(state, operation);
            final Map<String, Object> parameters = getParameters(state, operation);
            // Replace parameters in query
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String valueString = entry.getValue().toString().replace("\"", "").replace("\'","");
                query = query.replace(":" + entry.getKey(), valueString);
            }
            final List<List<Comparable>> sortable_results = new ArrayList<>();

            try(TypeDBTransaction transaction = state.getTransaction()){
                final Stream<JSON> result = transaction.query().fetch(query);

                // Convert and collect results
                result.forEach(concept -> {
                    try {
                        sortable_results.add(toListResult(concept));
                    } catch (ParseException e) {
                        System.err.println("[ERR] Error parsing concept: " + e.getMessage());
                    }
                });
                transaction.close();
                Collections.sort(sortable_results, new Query4Comparator());
                final List<LdbcQuery4Result> results = sortable_results
                        .stream()
                        .limit(LIMIT)
                        .map(this::toLdbcResult)
                        .collect(Collectors.toList());
                resultReporter.report(results.size(), results, operation);
            } catch (Exception e) {
                System.err.println("[ERR] Error executing operation: " + operation.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }

    public static class InteractiveQuery5 extends TypeQLListOperationHandler<LdbcQuery5, LdbcQuery5Result> {

        final int LIMIT = 10;

        @Override
        public String getQueryString(TypeQLDbConnectionState state, LdbcQuery5 operation) {
            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery5);
        }

        @Override
        public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery5 operation) {
            return state.getQueryStore().getQuery5Map(operation);
        }

        public LdbcQuery5Result toLdbcResult(List<Comparable> result) {
            return new LdbcQuery5Result(
                (String) result.get(1),
                ((Long) result.get(2)).intValue()
            );
        }

        public List<Comparable> toListResult(JSON result) throws ParseException {
            return Arrays.asList(
                    (int) result.asObject().get("forum_id").asObject().get("value").asNumber(),
                    result.asObject().get("forum_title").asObject().get("value").asString(),
                    (int) result.asObject().get("postCount").asObject().get("value").asNumber()
            );
        }

        class Query5PreComparator implements Comparator<List<Comparable>> {
            @Override
            public int compare(List<Comparable> o1, List<Comparable> o2) {
                return -o1.get(1).compareTo(o2.get(1)); // Sort by postCount in descending order
            }
        }

        class Query5Comparator implements Comparator<List<Comparable>> {
            @Override
            public int compare(List<Comparable> o1, List<Comparable> o2) {
                int comparison = -o1.get(2).compareTo(o2.get(2)); // Sort by postCount in descending order
                if (comparison != 0) {
                    return comparison;
                }
                return o1.get(0).compareTo(o2.get(0)); // Otherwise sort by forum_id
            }
        }

        @Override
        public void executeOperation(LdbcQuery5 operation, TypeQLDbConnectionState state,
                                     ResultReporter resultReporter) throws DbException
        {
            String query = getQueryString(state, operation);
            final Map<String, Object> parameters = getParameters(state, operation);
            // Replace parameters in query
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String valueString = entry.getValue().toString().replace("\"", "").replace("\'","");
                query = query.replace(":" + entry.getKey(), valueString);
            }
            String[] queryParts = query.split("#split#");
            String firstQuery = queryParts[0];
            String secondQuery = queryParts[1];

            // Pre-results are computed using a get group aggregate
            final List<List<Comparable>> preResults = new ArrayList<>();
            // First results are adding forum id and title to pre-results
            final List<List<Comparable>> firstResults = new ArrayList<>();
            // Second results pad results with forums without any friend's posts
            final List<List<Comparable>> secondResults = new ArrayList<>();

            try(TypeDBTransaction transaction = state.getTransaction()){
                // Generate pre-results
                final Stream<ValueGroup> answers = transaction.query().getGroupAggregate(firstQuery);
                answers.forEach(valueGroup -> {
                    preResults.add(Arrays.asList(
                            valueGroup.owner().asThing().getIID(),
                            valueGroup.value().get().asLong()
                    ));
                });
                Collections.sort(preResults, new Query5PreComparator());
                final List<List<Comparable>> truncatedPreResult = new ArrayList<>();
                truncatedPreResult.addAll(preResults.subList(0, Math.min(LIMIT, preResults.size())));
                if (preResults.size() > LIMIT) {
                    int extendedLimit = LIMIT;
                    long lastCount = (long) preResults.get(extendedLimit-1).get(1);
                    long nextCount = (long) preResults.get(extendedLimit).get(1);
                    while (lastCount == nextCount) {
                        truncatedPreResult.add(preResults.get(extendedLimit));
                        if (preResults.size() > extendedLimit + 1) {
                            extendedLimit++;
                            nextCount = (long) preResults.get(extendedLimit).get(1);
                        } else {
                            break;
                        }
                    }
                }

                // Generate first results
                truncatedPreResult.forEach(list -> {
                    final Stream<ConceptMap> answers1 = transaction.query().get(
                            String.format("match $x iid %s, has id $id, has title $title; get $id, $title;", list.get(0))
                    );
                    answers1.forEach(conceptMap -> {
                        firstResults.add(Arrays.asList(
                                conceptMap.get("id").asAttribute().getValue().asLong(),
                                conceptMap.get("title").asAttribute().getValue().asString(),
                                list.get(1)
                        ));
                    });
                });
                Collections.sort(firstResults, new Query5Comparator());

                final List<LdbcQuery5Result> results = firstResults
                        .stream()
                        .limit(LIMIT)
                        .map(this::toLdbcResult)
                        .collect(Collectors.toList());

                // Check if we have all results needed
                // If not, still need to consider second query
                if (results.size() == LIMIT) {
                    resultReporter.report(results.size(), results, operation);
                } else {
                    secondQuery = secondQuery.replace(":LIMIT", String.valueOf(LIMIT - results.size()));
                    final Stream<ConceptMap> answers2 = transaction.query().get(secondQuery);
                    answers2.forEach(conceptMap -> {
                        secondResults.add(Arrays.asList(
                                conceptMap.get("forum_id").asAttribute().getValue().asLong(),
                                conceptMap.get("forum_title").asAttribute().getValue().asString(),
                                0L
                        ));
                    });
                    Collections.sort(secondResults, new Query5Comparator());
                    // Append LIMIT - results.size() elements from second query
                    results.addAll(secondResults.stream()
                            .limit(LIMIT - results.size())
                            .map(this::toLdbcResult)
                            .collect(Collectors.toList()));
                    resultReporter.report(results.size(), results, operation);
                }
                transaction.close();
            } catch (Exception e) {
                System.err.println("[ERR] Error executing operation: " + operation.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }

    public static class InteractiveQuery6 extends TypeQLListOperationHandler<LdbcQuery6, LdbcQuery6Result> {

        final int LIMIT = 10;

        @Override
        public String getQueryString(TypeQLDbConnectionState state, LdbcQuery6 operation) {
            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery6);
        }

        @Override
        public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery6 operation) {
            return state.getQueryStore().getQuery6Map(operation);
        }

        public LdbcQuery6Result toLdbcResult(List<Comparable> result) {
            return new LdbcQuery6Result(
                    (String) result.get(0),
                    ((Long) result.get(1)).intValue()
            );
        }

        public List<Comparable> toListResult(JSON result) throws ParseException {
            return Arrays.asList(
                    result.asObject().get("tag_name").asObject().get("value").asString(),
                    (int) result.asObject().get("postCount").asObject().get("value").asNumber()
            );
        }

        class Query6Comparator implements Comparator<List<Comparable>> {
            @Override
            public int compare(List<Comparable> o1, List<Comparable> o2) {
                int comparison = -o1.get(1).compareTo(o2.get(1)); // Sort by postCount in descending order
                if (comparison != 0) {
                    return comparison;
                }
                return o1.get(0).compareTo(o2.get(0)); // Otherwise sort by tag_name
            }
        }

        @Override
        public void executeOperation(LdbcQuery6 operation, TypeQLDbConnectionState state,
                                     ResultReporter resultReporter) throws DbException
        {
            String query = getQueryString(state, operation);
            final Map<String, Object> parameters = getParameters(state, operation);
            // Replace parameters in query
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String valueString = entry.getValue().toString().replace("\"", "").replace("\'","");
                query = query.replace(":" + entry.getKey(), valueString);
            }
            final List<List<Comparable>> sortable_results = new ArrayList<>();

            try(TypeDBTransaction transaction = state.getTransaction()){
                final Stream<ValueGroup> result = transaction.query().getGroupAggregate(query);
                final AttributeType name = transaction
                        .concepts()
                        .getAttributeType("name")
                        .resolve();

                // Convert and collect results
                result.forEach(valueGroup -> {
                    sortable_results.add(Arrays.asList(
                            valueGroup
                                    .owner()
                                    .asThing()
                                    .getHas(transaction, name)
                                    .limit(1)
                                    .map(concept -> concept.asAttribute().getValue().asString())
                                    .collect(Collectors.toList())
                                    .get(0),
                            valueGroup.value().get().asLong()
                    ));
                });
                transaction.close();
                Collections.sort(sortable_results, new Query6Comparator());
                final List<LdbcQuery6Result> results = sortable_results
                        .stream()
                        .limit(LIMIT)
                        .map(this::toLdbcResult)
                        .collect(Collectors.toList());
                resultReporter.report(results.size(), results, operation);
            } catch (Exception e) {
                System.err.println("[ERR] Error executing operation: " + operation.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }

     public static class InteractiveQuery7 extends TypeQLListOperationHandler<LdbcQuery7,LdbcQuery7Result>
     {

         @Override
         public String getQueryString(TypeQLDbConnectionState state, LdbcQuery7 operation) {
             return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery7);
         }

         @Override
         public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery7 operation) {
             return state.getQueryStore().getQuery7Map(operation);
         }

         public LdbcQuery7Result toResult(ConceptMap result) throws ParseException {
             if (result != null) {
                 long friend_id = result.get("friend_id").asAttribute().getValue().asLong();
                 String friend_firstName = result.get("friend_firstName").asAttribute().getValue().asString();
                 String friend_lastName = result.get("friend_lastName").asAttribute().getValue().asString();
                 long likes_creationDate = result.get("likes_creationDate").asAttribute().getValue().asDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                 long message_id = result.get("message_id").asAttribute().getValue().asLong();
                 String message_ContOrImg = result.get("message_content_or_imageFile").asValue().asString();
                 int minutesLatency = (int)((likes_creationDate - result.get("message_creationDate").asAttribute().getValue().asDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())/60000);
                 boolean isNew = result.get("isNew").asValue().asBoolean();
                 return new LdbcQuery7Result(
                     friend_id,
                     friend_firstName,
                     friend_lastName,
                     likes_creationDate,
                     message_id,
                     message_ContOrImg,
                     minutesLatency,
                     isNew
                 );
             } else {
                 return null;
             }
         }

         @Override
         public void executeOperation(LdbcQuery7 operation, TypeQLDbConnectionState state,
                                      ResultReporter resultReporter) throws DbException
         {
             String query = getQueryString(state, operation);
             final Map<String, Object> parameters = getParameters(state, operation);
             // Replace parameters in query
             for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                 String valueString = entry.getValue().toString().replace("\"", "").replace("\'","");
                 query = query.replace(":" + entry.getKey(), valueString);
             }
             final List<LdbcQuery7Result> results = new ArrayList<>();

             try(TypeDBTransaction transaction = state.getTransaction()){
                 final Stream<ConceptMap> result = transaction.query().get(query);

                 // Convert and collect results
                 result.forEach(concept -> {
                     try {
                         results.add(toResult(concept));
                     } catch (ParseException e) {
                         System.err.println("[ERR] Error parsing concept: " + e.getMessage());
                     }
                 });
                 transaction.close();
                 resultReporter.report(results.size(), results, operation);
             } catch (Exception e) {
                 System.err.println("[ERR] Error executing operation: " + operation.getClass().getSimpleName());
                 e.printStackTrace();
             }
         }
     }

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
