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
public class Subject implements StatementComponent, Resembling<Subject> {
    /**
     * Describes which relations are ignored when producing compound subjects.
     */
    private static final Set<String> IGNORED_RELATIONS = new HashSet<>();
    static {
        IGNORED_RELATIONS.add("conj");   // other simple subjects
        IGNORED_RELATIONS.add("cc");     // words like "and"
        IGNORED_RELATIONS.add("punct");  // punctuation like ","
    }
    private final IndexedWord primary;
    private final Set<IndexedWord> secondary;
    private final Set<IndexedWord> complete;
    private final Set<Set<IndexedWord>> compounds;

    // TODO: remove secondary requirement from constructor, find dynamically instead
    public Subject(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        this.primary = primary;
        this.secondary = secondary;
        this.complete = graph.descendants(primary);

        // recursively discover all compound subjects
        compounds = new HashSet<>();
        compounds.add(StatementUtils.findCompoundComponents(primary, graph, IGNORED_RELATIONS));
        for (IndexedWord subject : secondary) {
            compounds.add(StatementUtils.findCompoundComponents(subject, graph, IGNORED_RELATIONS));
        }
    }

    /**
     * The name of the complete subject.
     * @return the longest subject possible
     */
    public String getName() {
        return StatementUtils.join(complete);
    }

    /**
     * The primary single subject contained within the complete subject.
     * @return primary subject
     */
    public IndexedWord getPrimary() {
        return primary;
    }

    /**
     * The compound subjects.
     * @return compounds
     */
    public Set<Set<IndexedWord>> getCompounds() {
        return new HashSet<>(compounds);
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
     * The amount of individual subjects contained within the complete subject.
     * @return subjects count
     */
    public int size() {
        return secondary.size() + 1;
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

        if (getName().equals(otherSubject.getName())) {
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

    @Override
    public String toString() {
        return getName() + " (" + size() + ")";
    }
}
