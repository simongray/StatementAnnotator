package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;

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
public class CompleteSubject implements Resembling<CompleteSubject> {
    /**
     * Describes which relations are ignored when producing compound subjects.
     */
    private static final Set<String> IGNORED_RELATIONS = new HashSet<>();
    {
        IGNORED_RELATIONS.add("conj");   // other simple subjects
        IGNORED_RELATIONS.add("cc");     // words like "and"
        IGNORED_RELATIONS.add("punct");  // punctuation like ","
    }
    private final IndexedWord primarySubject;
    private final Set<IndexedWord> secondarySubjects;
    private final Set<IndexedWord> completeSubject;
    private final Set<Set<IndexedWord>> compoundSubjects;

    // TODO: remove secondarySubjects requirement from constructor, find dynamically instead
    public CompleteSubject(IndexedWord primarySubject, Set<IndexedWord> secondarySubjects, SemanticGraph graph) {
        this.primarySubject = primarySubject;
        this.secondarySubjects = secondarySubjects;
        this.completeSubject = graph.descendants(primarySubject);

        // recursively discover all compound subjects
        compoundSubjects = new HashSet<>();
        compoundSubjects.add(findCompoundComponents(primarySubject, graph));
        for (IndexedWord secondarySubject : secondarySubjects) {
            compoundSubjects.add(findCompoundComponents(secondarySubject, graph));
        }
    }

    /**
     * The name of the complete subject.
     * @return the longest subject possible
     */
    public String getName() {
        return StatementUtils.join(completeSubject);
    }

    /**
     * The name of the primary simple subject.
     * @return the shortest subject possible
     */
    public String getShortName() {
        return primarySubject.word();
    }

    /**
     * The simple subjects contained in the complete subject.
     * @return simple subjects
     */
    public Set<IndexedWord> getSimpleSubjects() {
        Set<IndexedWord> simpleSubjects = new HashSet<>(secondarySubjects);
        simpleSubjects.add(primarySubject);
        return simpleSubjects;
    }

    /**
     * The compound subjects contained in the complete subject.
     * @return compound subject components
     */
    public Set<Set<IndexedWord>> getCompoundSubjects() {
        return new HashSet<>(compoundSubjects);
    }

    /**
     * The names of all of the compound subjects.
     * @return compound subject names
     */
    public Set<String> getCompoundSubjectNames() {
        Set<String> compoundSubjectNames = new HashSet<>();
        for (Set<IndexedWord> compoundSubject : compoundSubjects) {
            compoundSubjectNames.add(StatementUtils.join(compoundSubject));
        }
        return compoundSubjectNames;
    }

    /**
     * Recursively finds the components of a compound subject.
     * @param simpleSubject the simple subject that serves as an entry point
     * @param graph the graph of the sentence
     * @return compound components
     */
    private Set<IndexedWord> findCompoundComponents(IndexedWord simpleSubject, SemanticGraph graph) {
        return StatementUtils.findCompoundComponents(simpleSubject, graph, IGNORED_RELATIONS);
    }

    /**
     * The amount of individual subjects contained within the complete subject.
     * @return subjects count
     */
    public int size() {
        return secondarySubjects.size() + 1;
    }

    /**
     * The resemblance of another subject to this subject.
     * @param otherSubject subject to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(CompleteSubject otherSubject) {
        if (getName().equals(otherSubject.getName())) {
            return Resemblance.FULL;  // identical complete subject
        }

        int resemblanceCount = 0;
        Set<String> otherSubjectCompoundNames = otherSubject.getCompoundSubjectNames();
        for (String name : getCompoundSubjectNames()) {
            if (otherSubjectCompoundNames.contains(name)) {
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
