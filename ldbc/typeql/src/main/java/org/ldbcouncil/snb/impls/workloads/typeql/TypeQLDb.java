package org.ldbcouncil.snb.impls.workloads.typeql;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import java.time.Instant;

public abstract class TypeQLDb extends BaseDb<TypeQLQueryStore> {

    @Override
    protected void onInit(Map<String, String> properties, LoggingService loggingService) throws DbException {
        try {
            dcs = new TypeQLDbConnectionState<TypeQLQueryStore>(properties, new TypeQLQueryStore(properties.get("queryDir")));
        } catch (ClassNotFoundException e) {
            throw new DbException(e);
        }
    }

    // =============
    // Complex Reads
    // =============

    public static class InteractiveQuery1 extends TypeQLFetchListOperationHandler<LdbcQuery1,LdbcQuery1Result>
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

    public static class InteractiveQuery2 extends TypeQLFetchListOperationHandler<LdbcQuery2,LdbcQuery2Result>
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

    public static class InteractiveQuery3a extends TypeQLFetchListOperationHandler<LdbcQuery3a, LdbcQuery3Result> {

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

    public static class InteractiveQuery3b extends TypeQLFetchListOperationHandler<LdbcQuery3b, LdbcQuery3Result> {

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

    public static class InteractiveQuery4 extends TypeQLSpecialListOperationHandler<LdbcQuery4, LdbcQuery4Result> {

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

            try(TypeDBTransaction transaction = state.getReadTransaction()){
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

    public static class InteractiveQuery5 extends TypeQLFetchListOperationHandler<LdbcQuery5, LdbcQuery5Result> {

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

            try(TypeDBTransaction transaction = state.getReadTransaction()){
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

    public static class InteractiveQuery6 extends TypeQLSpecialListOperationHandler<LdbcQuery6, LdbcQuery6Result> {

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

            try(TypeDBTransaction transaction = state.getReadTransaction()){
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

     public static class InteractiveQuery7 extends TypeQLGetListOperationHandler<LdbcQuery7,LdbcQuery7Result> {

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
                 int minutesLatency = (int) ((likes_creationDate - result.get("message_creationDate").asAttribute().getValue().asDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()) / 60000);
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
     }

     public static class InteractiveQuery8 extends TypeQLGetListOperationHandler<LdbcQuery8,LdbcQuery8Result> {

         @Override
         public String getQueryString(TypeQLDbConnectionState state, LdbcQuery8 operation) {
             return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery8);
         }

         @Override
         public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery8 operation) {
             return state.getQueryStore().getQuery8Map(operation);
         }

         public LdbcQuery8Result toResult(ConceptMap result) throws ParseException {
             if (result != null) {

                 long authorId = result.get("commenAuthor_id").asAttribute().getValue().asLong();
                 String firstname = result.get("commenAuthor_firstName").asAttribute().getValue().asString();
                 String lastname = result.get("commenAuthor_lastName").asAttribute().getValue().asString();
                 long date = result.get("comment_creationDate").asAttribute().getValue().asDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                 long replyId = result.get("comment_id").asAttribute().getValue().asLong();
                 String content = result.get("comment_content").asAttribute().getValue().asString();

                 return new LdbcQuery8Result(
                         authorId,
                         firstname,
                         lastname,
                         date,
                         replyId,
                         content
                 );
             } else {
                 return null;
             }
         }
     }

     public static class InteractiveQuery9 extends TypeQLGetListOperationHandler<LdbcQuery9,LdbcQuery9Result>
     {

         @Override
         public String getQueryString(TypeQLDbConnectionState state, LdbcQuery9 operation) {
             return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery9);
         }

         @Override
         public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery9 operation) {
             return state.getQueryStore().getQuery9Map(operation);
         }

         public LdbcQuery9Result toResult(ConceptMap result) throws ParseException {
             if (result != null) {
                
                 long otherId = result.get("otherPerson_id").asAttribute().getValue().asLong();
                 String firstname = result.get("otherPerson_firstName").asAttribute().getValue().asString();
                 String lastname = result.get("otherPerson_lastName").asAttribute().getValue().asString();
                 long messageId = result.get("message_id").asAttribute().getValue().asLong();
                 String messageContent = result.get("message_content_or_imageFile").asValue().asString();
                 long date = result.get("message_creationDate").asAttribute().getValue().asDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

                 return new LdbcQuery9Result(
                     otherId,
                     firstname,
                     lastname,
                     messageId,
                     messageContent,
                     date
                 );
             } else {
                 return null;
             }
         }
     }
    public static class InteractiveQuery10 extends TypeQLSpecialListOperationHandler<LdbcQuery10, LdbcQuery10Result> {

        final int LIMIT = 10;

        @Override
        public String getQueryString(TypeQLDbConnectionState state, LdbcQuery10 operation) {
            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveComplexQuery10);
        }

        @Override
        public Map<String, Object> getParameters(TypeQLDbConnectionState state, LdbcQuery10 operation) {
            return state.getQueryStore().getQuery10Map(operation);
        }

        public LdbcQuery10Result toLdbcResult(List<Comparable> result) {
            return new LdbcQuery10Result(
                    (long) result.get(1),
                    (String) result.get(2),
                    (String) result.get(3),
                    ((Integer) result.get(0)).intValue(),
                    (String) result.get(4),
                    (String) result.get(5)
            );
        }

        private List<Comparable> toFoafResult(ConceptMap map) {
            return Arrays.asList(
                    map.get("foaf").asThing().getIID(),
                    map.get("foaf_birthday").asAttribute().getValue().asDateTime().atZone(ZoneId.systemDefault()).toLocalDate()
            );
        }

        class Query10Comparator implements Comparator<List<Comparable>> {
            @Override
            public int compare(List<Comparable> o1, List<Comparable> o2) {
                int comparison = -o1.get(0).compareTo(o2.get(0)); // Sort by commonality score
                if (comparison != 0) {
                    return comparison;
                }
                return o1.get(1).compareTo(o2.get(1)); // Otherwise sort by friend_id
            }
        }

        @Override
        public void executeOperation(LdbcQuery10 operation, TypeQLDbConnectionState state,
                                     ResultReporter resultReporter) throws DbException
        {
            String query = getQueryString(state, operation);
            final Map<String, Object> parameters = getParameters(state, operation);
            query = query.replace(":personId", parameters.get("personId").toString());
            String[] queries = query.split("#split#");
            String foaf_query = queries[0];
            final String[] common_query = {queries[1]};

            try(TypeDBTransaction transaction = state.getReadTransaction()){
                final Stream<ConceptMap> result = transaction.query().get(foaf_query);
                final List<List<Comparable>> foaf_results = new ArrayList<>();
                final List<List<Comparable>> sortable_common_results = new ArrayList<>();
                result.forEach(map -> {
                    foaf_results.add(toFoafResult(map));
                });
                // Filter results to only include those with a birthday
                // between 21-month and 22-(month+1) where
                // month = parameters.get("month")
                foaf_results.stream().filter(list -> {
                    LocalDate birthday = (LocalDate) list.get(1);
                    int month = Integer.parseInt((String) parameters.get("month"));
                    LocalDate startDate = LocalDate.of(birthday.getYear(), month, 21);
                    LocalDate endDate = startDate.plusMonths(1).withDayOfMonth(22);
                    return !birthday.isBefore(startDate) && birthday.isBefore(endDate);
                }).forEach(list -> {
                    String foaf_id = (String) list.get(0);
                    String common_query_foaf = common_query[0].replace(":foafId", foaf_id);
                    final Stream<JSON> common_result = transaction.query().fetch(common_query_foaf);
                    common_result.forEach(json -> {
                        Map<String, JSON> jsonMap = json.asObject();
                        sortable_common_results.add(Arrays.asList(
                                (int) jsonMap.get("common").asObject().get("value").asNumber()
                                       - (int) jsonMap.get("uncommon").asObject().get("value").asNumber(),
                                (long) jsonMap.get("foaf_id").asObject().get("value").asNumber(),
                                jsonMap.get("foaf_firstName").asObject().get("value").asString(),
                                jsonMap.get("foaf_lastName").asObject().get("value").asString(),
                                jsonMap.get("foaf_gender").asObject().get("value").asString(),
                                jsonMap.get("city_name").asObject().get("value").asString()
                        ));
                    });
                });

                transaction.close();
                Collections.sort(sortable_common_results, new Query10Comparator());
                final List<LdbcQuery10Result> results = sortable_common_results
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

    // ===========
    // SHORT READS
    // ===========

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
                long cityId = (long) jsonMap.get("cityId").asObject().get("value").asNumber();
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

    // ===========
    // INSERTS
    // ===========

    public static class Insert1AddPerson extends TypeQLUpdateOperationHandler<LdbcInsert1AddPerson>
    {
        @Override
        public String getQueryString(TypeQLDbConnectionState state, LdbcInsert1AddPerson operation) {
            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveInsert1);
        }

        @Override
        public Map<String, Object> getParameters( LdbcInsert1AddPerson operation )
        {
            final List<List<Long>> universities =
                    operation.getStudyAt().stream().map( u -> Arrays.asList( u.getOrganizationId(), (long) u.getYear() ) ).collect( Collectors.toList() );
            final List<List<Long>> companies =
                    operation.getWorkAt().stream().map( c -> Arrays.asList( c.getOrganizationId(), (long) c.getYear() ) ).collect( Collectors.toList() );

            return ImmutableMap.<String, Object>builder()
                    .put( LdbcInsert1AddPerson.PERSON_ID, operation.getPersonId() )
                    .put( LdbcInsert1AddPerson.PERSON_FIRST_NAME, operation.getPersonFirstName() )
                    .put( LdbcInsert1AddPerson.PERSON_LAST_NAME, operation.getPersonLastName() )
                    .put( LdbcInsert1AddPerson.GENDER, operation.getGender() )
                    .put( LdbcInsert1AddPerson.BIRTHDAY,
                            Instant.ofEpochMilli(operation.getBirthday().getTime())
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                    .format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .put(LdbcInsert1AddPerson.CREATION_DATE,
                            Instant.ofEpochMilli(operation.getCreationDate().getTime())
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                    .format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .put( LdbcInsert1AddPerson.LOCATION_IP, operation.getLocationIp() )
                    .put( LdbcInsert1AddPerson.BROWSER_USED, operation.getBrowserUsed() )
                    .put( LdbcInsert1AddPerson.CITY_ID, operation.getCityId() )
                    .put( LdbcInsert1AddPerson.LANGUAGES, operation.getLanguages() )
                    .put( LdbcInsert1AddPerson.EMAILS, operation.getEmails() )
                    .put( LdbcInsert1AddPerson.TAG_IDS, operation.getTagIds() )
                    .put( LdbcInsert1AddPerson.STUDY_AT, universities )
                    .put( LdbcInsert1AddPerson.WORK_AT, companies )
                    .build();
        }

        @Override
        public void executeOperation(LdbcInsert1AddPerson operation, TypeQLDbConnectionState state,
                                     ResultReporter resultReporter) throws DbException
        {
            String query = getQueryString(state, operation);
            final Map<String, Object> parameters = getParameters(operation);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String valueString = entry.getValue().toString().replace("\"", "").replace("\'","");
                query = query.replace(":" + entry.getKey(), valueString);
            }
            String[] queries = query.split("#split#");

            try(TypeDBTransaction transaction = state.getWriteTransaction()){
                transaction.query().insert(queries[0]);
                // Languages
                ((ImmutableList) parameters.get("languages")).forEach(language -> {
                    transaction.query().insert(queries[1].replace(":language", language.toString()));
                });
                // Emails
                ((ImmutableList) parameters.get("emails")).forEach(email -> {
                    transaction.query().insert(queries[2].replace(":email", email.toString()));
                });
                // TagIDs
                ((ImmutableList) parameters.get("tagIds")).forEach(tagid -> {
                    transaction.query().insert(queries[3].replace(":tagId", tagid.toString()));
                });
                // University
                ((ArrayList) parameters.get("studyAt")).forEach(studyTuple -> {
                    transaction.query().insert(queries[4]
                            .replace(":orgId", ((List) studyTuple).get(0).toString())
                            .replace(":studyYear", ((List) studyTuple).get(1).toString()));
                });
                // Work
                ((ArrayList) parameters.get("workAt")).forEach(workTuple -> {
                    transaction.query().insert(queries[4]
                            .replace(":orgId", ((List) workTuple).get(0).toString())
                            .replace(":workFrom", ((List) workTuple).get(1).toString()));
                });
                transaction.commit();
            } catch (Exception e) {
                System.err.println("[ERR] Error executing operation: " + operation.getClass().getSimpleName());
                e.printStackTrace();
            }

        }
    }

//    public static class Insert2AddPostLike extends TypeQLUpdateOperationHandler<LdbcInsert2AddPostLike>
//    {
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcInsert2AddPostLike operation) {
//            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveInsert2);
//        }
//
//        @Override
//        public Map<String, Object> getParameters( LdbcInsert2AddPostLike operation )
//        {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcInsert2AddPostLike.PERSON_ID, operation.getPersonId() )
//                    .put( LdbcInsert2AddPostLike.POST_ID, operation.getPostId() )
//                    .put( LdbcInsert2AddPostLike.CREATION_DATE, operation.getCreationDate().getTime() )
//                    .build();
//        }
//    }
//
//    public static class Insert3AddCommentLike extends TypeQLUpdateOperationHandler<LdbcInsert3AddCommentLike>
//    {
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcInsert3AddCommentLike operation) {
//            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveInsert3);
//        }
//
//        @Override
//        public Map<String, Object> getParameters( LdbcInsert3AddCommentLike operation )
//        {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcInsert3AddCommentLike.PERSON_ID, operation.getPersonId() )
//                    .put( LdbcInsert3AddCommentLike.COMMENT_ID, operation.getCommentId() )
//                    .put( LdbcInsert3AddCommentLike.CREATION_DATE, operation.getCreationDate().getTime() )
//                    .build();
//        }
//    }
//
//    public static class Insert4AddForum extends TypeQLUpdateOperationHandler<LdbcInsert4AddForum>
//    {
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcInsert4AddForum operation) {
//            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveInsert4);
//        }
//
//        @Override
//        public Map<String, Object> getParameters( LdbcInsert4AddForum operation )
//        {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcInsert4AddForum.FORUM_ID, operation.getForumId() )
//                    .put( LdbcInsert4AddForum.FORUM_TITLE, operation.getForumTitle() )
//                    .put( LdbcInsert4AddForum.CREATION_DATE, operation.getCreationDate().getTime() )
//                    .put( LdbcInsert4AddForum.MODERATOR_PERSON_ID, operation.getModeratorPersonId() )
//                    .put( LdbcInsert4AddForum.TAG_IDS, operation.getTagIds() )
//                    .build();
//        }
//    }
//
//    public static class Insert5AddForumMembership extends TypeQLUpdateOperationHandler<LdbcInsert5AddForumMembership>
//    {
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcInsert5AddForumMembership operation) {
//            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveInsert5);
//        }
//
//        @Override
//        public Map<String, Object> getParameters( LdbcInsert5AddForumMembership operation )
//        {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcInsert5AddForumMembership.FORUM_ID, operation.getForumId() )
//                    .put( LdbcInsert5AddForumMembership.PERSON_ID, operation.getPersonId() )
//                    .put( LdbcInsert5AddForumMembership.CREATION_DATE, operation.getCreationDate().getTime() )
//                    .build();
//        }
//    }
//
//    public static class Insert6AddPost extends TypeQLUpdateOperationHandler<LdbcInsert6AddPost>
//    {
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcInsert6AddPost operation) {
//            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveInsert6);
//        }
//
//        @Override
//        public Map<String, Object> getParameters( LdbcInsert6AddPost operation )
//        {
//            final HashMap<String, Object> parameterMap = new HashMap<>();
//            parameterMap.put(LdbcInsert6AddPost.POST_ID, operation.getPostId());
//            parameterMap.put(LdbcInsert6AddPost.IMAGE_FILE, operation.getImageFile());
//            parameterMap.put(LdbcInsert6AddPost.CREATION_DATE, operation.getCreationDate().getTime());
//            parameterMap.put(LdbcInsert6AddPost.LOCATION_IP, operation.getLocationIp());
//            parameterMap.put(LdbcInsert6AddPost.BROWSER_USED, operation.getBrowserUsed());
//            parameterMap.put(LdbcInsert6AddPost.LANGUAGE, operation.getLanguage());
//            parameterMap.put(LdbcInsert6AddPost.CONTENT, operation.getContent());
//            parameterMap.put(LdbcInsert6AddPost.LENGTH, operation.getLength());
//            parameterMap.put(LdbcInsert6AddPost.AUTHOR_PERSON_ID, operation.getAuthorPersonId());
//            parameterMap.put(LdbcInsert6AddPost.FORUM_ID, operation.getForumId());
//            parameterMap.put(LdbcInsert6AddPost.COUNTRY_ID, operation.getCountryId());
//            parameterMap.put(LdbcInsert6AddPost.TAG_IDS, operation.getTagIds());
//            return parameterMap;
//        }
//    }
//
//    public static class Insert7AddComment extends TypeQLUpdateOperationHandler<LdbcInsert7AddComment>
//    {
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcInsert7AddComment operation) {
//            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveInsert7);
//        }
//
//        @Override
//        public Map<String, Object> getParameters( LdbcInsert7AddComment operation )
//        {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcInsert7AddComment.COMMENT_ID, operation.getCommentId() )
//                    .put( LdbcInsert7AddComment.CREATION_DATE, operation.getCreationDate().getTime() )
//                    .put( LdbcInsert7AddComment.LOCATION_IP, operation.getLocationIp() )
//                    .put( LdbcInsert7AddComment.BROWSER_USED, operation.getBrowserUsed() )
//                    .put( LdbcInsert7AddComment.CONTENT, operation.getContent() )
//                    .put( LdbcInsert7AddComment.LENGTH, operation.getLength() )
//                    .put( LdbcInsert7AddComment.AUTHOR_PERSON_ID, operation.getAuthorPersonId() )
//                    .put( LdbcInsert7AddComment.COUNTRY_ID, operation.getCountryId() )
//                    .put( LdbcInsert7AddComment.REPLY_TO_POST_ID, operation.getReplyToPostId() )
//                    .put( LdbcInsert7AddComment.REPLY_TO_COMMENT_ID, operation.getReplyToCommentId() )
//                    .put( LdbcInsert7AddComment.TAG_IDS, operation.getTagIds() )
//                    .build();
//        }
//    }
//
//    public static class Insert8AddFriendship extends TypeQLUpdateOperationHandler<LdbcInsert8AddFriendship>
//    {
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcInsert8AddFriendship operation) {
//            return state.getQueryStore().getParameterizedQuery(QueryType.InteractiveInsert8);
//        }
//
//        @Override
//        public Map<String, Object> getParameters( LdbcInsert8AddFriendship operation )
//        {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcInsert8AddFriendship.PERSON1_ID, operation.getPerson1Id() )
//                    .put( LdbcInsert8AddFriendship.PERSON2_ID, operation.getPerson2Id() )
//                    .put( LdbcInsert8AddFriendship.CREATION_DATE, operation.getCreationDate().getTime() )
//                    .build();
//        }
//    }
//

    // ===========
    // DELETIONS
    // ===========

    public static class Delete1RemovePerson extends TypeQLUpdateOperationHandler<LdbcDelete1RemovePerson> {

        @Override
        public String getQueryString(TypeQLDbConnectionState state, LdbcDelete1RemovePerson operation) {
            return state.getQueryStore().getDelete1(operation);
        }

        @Override
        public Map<String, Object> getParameters(LdbcDelete1RemovePerson operation) {
            return ImmutableMap.<String, Object>builder()
                    .put( LdbcDelete1RemovePerson.PERSON_ID, operation.getremovePersonIdD1() )
                    .build();
        }

        @Override
        public void executeOperation(LdbcDelete1RemovePerson operation, TypeQLDbConnectionState state,
                                     ResultReporter resultReporter) throws DbException
        {
            String query = getQueryString(state, operation);
            final Map<String, Object> parameters = getParameters(operation);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                String valueString = entry.getValue().toString().replace("\"", "").replace("\'","");
                query = query.replace(":" + entry.getKey(), valueString);
            }
            String[] queries = query.split("#split#");

            try(TypeDBTransaction transaction = state.getWriteTransaction()){
                transaction.query().delete(queries[0]);
                transaction.query().delete(queries[1]);
                transaction.query().delete(queries[2]);
                transaction.query().delete(queries[3]);
//                transaction.commit();
            } catch (Exception e) {
                System.err.println("[ERR] Error executing operation: " + operation.getClass().getSimpleName());
                e.printStackTrace();
            }
        }
    }
//
//    public static class Delete2RemovePostLike extends TypeQLUpdateOperationHandler<LdbcDelete2RemovePostLike> {
//
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcDelete2RemovePostLike operation) {
//            return state.getQueryStore().getDelete2(operation);
//        }
//
//        @Override
//        public Map<String, Object> getParameters(LdbcDelete2RemovePostLike operation) {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcDelete2RemovePostLike.PERSON_ID, operation.getremovePersonIdD2() )
//                    .put( LdbcDelete2RemovePostLike.POST_ID, operation.getremovePostIdD2() )
//                    .build();
//        }
//    }
//
//    public static class Delete3RemoveCommentLike extends TypeQLUpdateOperationHandler<LdbcDelete3RemoveCommentLike> {
//
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcDelete3RemoveCommentLike operation) {
//            return state.getQueryStore().getDelete3(operation);
//        }
//
//        @Override
//        public Map<String, Object> getParameters(LdbcDelete3RemoveCommentLike operation) {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcDelete3RemoveCommentLike.PERSON_ID, operation.getremovePersonIdD3() )
//                    .put( LdbcDelete3RemoveCommentLike.COMMENT_ID, operation.getremoveCommentIdD3() )
//                    .build();
//        }
//    }
//
//    public static class Delete4RemoveForum extends TypeQLUpdateOperationHandler<LdbcDelete4RemoveForum> {
//
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcDelete4RemoveForum operation) {
//            return state.getQueryStore().getDelete4(operation);
//        }
//
//        @Override
//        public Map<String, Object> getParameters(LdbcDelete4RemoveForum operation) {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcDelete4RemoveForum.FORUM_ID, operation.getremoveForumIdD4() )
//                    .build();
//        }
//    }
//
//    public static class Delete5RemoveForumMembership extends TypeQLUpdateOperationHandler<LdbcDelete5RemoveForumMembership> {
//
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcDelete5RemoveForumMembership operation) {
//            return state.getQueryStore().getDelete5(operation);
//        }
//
//        @Override
//        public Map<String, Object> getParameters(LdbcDelete5RemoveForumMembership operation) {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcDelete5RemoveForumMembership.PERSON_ID, operation.getremovePersonIdD5() )
//                    .put( LdbcDelete5RemoveForumMembership.FORUM_ID, operation.getremoveForumIdD5() )
//                    .build();
//        }
//    }
//
//    public static class Delete6RemovePostThread extends TypeQLUpdateOperationHandler<LdbcDelete6RemovePostThread> {
//
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcDelete6RemovePostThread operation) {
//            return state.getQueryStore().getDelete6(operation);
//        }
//
//        @Override
//        public Map<String, Object> getParameters(LdbcDelete6RemovePostThread operation) {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcDelete6RemovePostThread.POST_ID, operation.getremovePostIdD6() )
//                    .build();
//        }
//    }
//
//    public static class Delete7RemoveCommentSubthread extends TypeQLUpdateOperationHandler<LdbcDelete7RemoveCommentSubthread> {
//
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcDelete7RemoveCommentSubthread operation) {
//            return state.getQueryStore().getDelete7(operation);
//        }
//
//        @Override
//        public Map<String, Object> getParameters(LdbcDelete7RemoveCommentSubthread operation) {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcDelete7RemoveCommentSubthread.COMMENT_ID, operation.getremoveCommentIdD7() )
//                    .build();
//        }
//    }
//
//    public static class Delete8RemoveFriendship extends TypeQLUpdateOperationHandler<LdbcDelete8RemoveFriendship> {
//
//        @Override
//        public String getQueryString(TypeQLDbConnectionState state, LdbcDelete8RemoveFriendship operation) {
//            return state.getQueryStore().getDelete8(operation);
//        }
//
//        @Override
//        public Map<String, Object> getParameters(LdbcDelete8RemoveFriendship operation) {
//            return ImmutableMap.<String, Object>builder()
//                    .put( LdbcDelete8RemoveFriendship.PERSON1_ID, operation.getremovePerson1Id() )
//                    .put( LdbcDelete8RemoveFriendship.PERSON2_ID, operation.getremovePerson2Id() )
//                    .build();
//        }
//    }
}
