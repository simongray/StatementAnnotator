package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.*;

/**
 * The complete subject of a natural language statement.
 *
 * It encapsulates and facilitates retrieval of the different components of the complete subject.
 * It can be compared to other CompleteSubjects through it implementation of the Resembling interface.
 *
 * Explanation of terms used:
 *     simple subject = a single word subject (a direct dependent of a verb)
 *     compound subject = a multiple word subject (single word subject + relevant dependent words)
 *     complete subject = the complete subject, including all compound subjects and syntactical glue
 *     primary subject = the simple subject that is the primary dependent of the verb
 *     secondary subject = any other simple subjects contained in the complete subject
 */
public class Subject extends AbstractComponent implements Resembling<Subject> {
    /**
     * Describes which relations are ignored when producing compound subjects.
     */
    static {
        IGNORED_COMPOUND_RELATIONS.add("conj");   // other simple subjects
        IGNORED_COMPOUND_RELATIONS.add("cc");     // words like "and"
        IGNORED_COMPOUND_RELATIONS.add("punct");  // punctuation like ","
    }

    public Subject(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        super(primary, secondary, graph);
    }

    /**
     * The names of all of the compound subjects.
     * @return compound subject names
     */
    public Set<String> getCompoundStrings() {
        Set<String> compoundSubjectNames = new HashSet<>();
        for (Set<IndexedWord> compound : compounds) {
            compoundSubjectNames.add(StatementUtils.join(compound));
        }
        return compoundSubjectNames;
    }

    /**
     * The resemblance of another subject to this subject.
     * @param otherSubject subject to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(Subject otherSubject) {
        if (otherSubject == null) {
            return Resemblance.NONE;
        }

        if (getString().equals(otherSubject.getString())) {
            return Resemblance.FULL;  // identical complete subject
        }

        int resemblanceCount = 0;
        Set<String> otherSubjectCompounds = otherSubject.getCompoundStrings();
        for (String compound : getCompoundStrings()) {
            if (otherSubjectCompounds.contains(compound)) {
                resemblanceCount++;
            }
        }

        if (resemblanceCount == size()) {
            return Resemblance.CLOSE;  // identical subject compounds
        }

        if (resemblanceCount > 0) {
            return Resemblance.SLIGHT;  // at least 1 identical subject compound
        }

        return Resemblance.NONE;
    }

    /**
     * The identifier for this component.
     *
     * @return identifier
     */
    @Override
    protected String getIdentifier() {
        return "S";
    }
}
