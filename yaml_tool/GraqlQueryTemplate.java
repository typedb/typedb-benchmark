package grakn.simulation.yaml_tool;

import graql.lang.util.StringUtil;
import org.apache.commons.csv.CSVRecord;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to perform simple Graql query templating.
 */
public class GraqlQueryTemplate {
    private static final Pattern TEMPLATE_VAR_PATTERN = Pattern.compile("<(\\d+)>");

    private String[] templateComponents;
    private List<Integer> vars;
    private Set<Integer> columns;

    public GraqlQueryTemplate(String template) {
        Matcher matcher = TEMPLATE_VAR_PATTERN.matcher(template);

        columns = new HashSet<>();
        vars = new ArrayList<>();

        while(matcher.find()) {
            int varNum = Integer.parseInt(matcher.group(1));
            columns.add(varNum);
            vars.add(varNum);
        }

        // Store the strings around the matched patterns so that a StringBuilder can be used to build up the template.
        templateComponents = TEMPLATE_VAR_PATTERN.split(template);
    }

    /**
     * Interpolate a given object that maps Integer indices to String values.
     *
     * @param getter A function to map the indices to values.
     * @return The interpolated query.
     */
    public String interpolate(Function<Integer, String> getter) {
        StringBuilder builder = new StringBuilder(templateComponents[0]);

        for (int i = 1; i < templateComponents.length; ++i) {
            builder.append(StringUtil.quoteString(getter.apply(vars.get(i - 1))));
            builder.append(templateComponents[i]);
        }

        return builder.toString();
    }
}
