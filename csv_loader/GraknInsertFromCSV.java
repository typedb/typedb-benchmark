package grakn.simulation.csv_loader;

import graql.lang.Graql;
import graql.lang.pattern.Pattern;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.StatementInstance;
import org.apache.commons.csv.CSVRecord;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class GraknInsertFromCSV {
    private String insertedEntityType;
    private List<HasRelation> hasRelations = new ArrayList<>();
    private List<HasAttribute> hasAttributes = new ArrayList<>();

    GraknInsertFromCSV(String insertedEntityType, List<String> headerNames) {
        this.insertedEntityType = insertedEntityType;

        // Interpret all headers
        for (int i = 0; i < headerNames.size(); ++i) {
            interpretHeader(headerNames.get(i), i);
        }
    }

    private void interpretHeader(String name, int position) {
        if (name.startsWith(":")) {
            String[] splitName = name.split(":", 6);
            if (splitName.length < 6) {
                throw new IllegalArgumentException("Format Error: Header " + position + ": " + name);
            }

            hasRelations.add(new HasRelation(
                    splitName[1],
                    splitName[2],
                    splitName[3],
                    splitName[4],
                    splitName[5],
                    position
            ));
        } else {
            hasAttributes.add(new HasAttribute(name, position));
        }
    }

    InsertQueryBuilder builder() {
        return new InsertQueryBuilder();
    }

    class InsertQueryBuilder {
        private int currentRecord = 1;
        Set<String> alreadyMatchedKeys = new HashSet<>();
        List<Pattern> matchPatterns = new ArrayList<>();
        List<StatementInstance> insertStatements = new ArrayList<>();

        private InsertQueryBuilder() {}

        void add(CSVRecord record) {

            String recordKey = "__" + currentRecord;

            StatementInstance insert = Graql.var(recordKey).isa(insertedEntityType);
            for (HasAttribute hasAttribute : hasAttributes) {
                insert = hasAttribute.has(insert, record);
            }

            insertStatements.add(insert);

            for (HasRelation hasRelation : hasRelations) {
                HasRelation.HasRelationInstance instance = hasRelation.of(record);

                if (!alreadyMatchedKeys.contains(instance.getMatchKey())) {
                    matchPatterns.add(instance.getPattern());
                }

                insertStatements.add(instance.getInsert(recordKey));
            }

            currentRecord++;
        }

        GraqlInsert build() {
            if (matchPatterns.size() > 0) {
                return Graql.match(matchPatterns).insert(insertStatements);
            } else {
                return Graql.insert(insertStatements);
            }
        }
    }

    private static class HasRelation {
        private String relationType;
        private String insertedEntityRole;
        private String otherEntityRole;
        private String otherEntityType;
        private String otherEntityAttribute;
        private int position;

        private HasRelation(String relationType,
                            String insertedEntityRole,
                            String otherEntityRole,
                            String otherEntityType,
                            String otherEntityAttribute,
                            int position) {
            this.relationType = relationType;
            this.insertedEntityRole = insertedEntityRole;
            this.otherEntityRole = otherEntityRole;
            this.otherEntityType = otherEntityType;
            this.otherEntityAttribute = otherEntityAttribute;
            this.position = position;
        }

        HasRelationInstance of(CSVRecord record) {
            return new HasRelationInstance(record);
        }

        private class HasRelationInstance {
            private String matchKey;
            private String value;

            private HasRelationInstance(CSVRecord csvRecord) {
                value = csvRecord.get(position);
                matchKey = otherEntityType + "__" + value.replace(' ', '_');
            }

            String getMatchKey() {
                return matchKey;
            }

            Pattern getPattern() {
                return Graql.var(matchKey).isa(otherEntityType).has(otherEntityAttribute, value);
            }

            StatementInstance getInsert(String insertedEntityKey) {
                return Graql.var()
                        .isa(relationType)
                        .rel(insertedEntityRole, insertedEntityKey)
                        .rel(otherEntityRole, matchKey);
            }
        }
    }

    private static class HasAttribute {
        private String label;
        private int position;

        private HasAttribute(String label, int position) {
            this.label = label;
            this.position = position;
        }

        StatementInstance has(StatementInstance original, CSVRecord record) {
            return original.has(label, record.get(position));
        }
    }
}
