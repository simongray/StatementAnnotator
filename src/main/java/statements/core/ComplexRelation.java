package statements.core;

import edu.stanford.nlp.trees.GrammaticalRelation;

import java.util.HashSet;
import java.util.Set;

/**
 * Useful for defining complex relations such as "NMOD excluding certain specific cases"
 */
public class ComplexRelation {
    private final String shortName;
    Set<String> specifics = new HashSet<>();
    boolean excluding = false;

    private ComplexRelation(String shortName, boolean excluding, String... specifics) {
        this.shortName = shortName;
        this.excluding = excluding;
        for (String specific : specifics) {
            this.specifics.add(specific);
        }
    }

    public static ComplexRelation getRelationExcludingSpecifics(String relation, String... excludedSpecifics) {
        return new ComplexRelation(relation, true, excludedSpecifics);
    }

    public static ComplexRelation getRelationIncludingSpecifics(String relation, String... excludedSpecifics) {
        return new ComplexRelation(relation, false, excludedSpecifics);
    }

    public boolean evaluate(GrammaticalRelation grammaticalRelation) {
        return evaluateShortName(grammaticalRelation.getShortName()) && evaluateSpecific(grammaticalRelation.getSpecific());
    }

    public boolean evaluateShortName(String shortName) {
        return this.shortName.equals(shortName);
    }

    public boolean evaluateSpecific(String specific) {
        if (excluding) {
            return !specifics.contains(specific);
        } else {
            return specifics.contains(specific);
        }
    }
}
