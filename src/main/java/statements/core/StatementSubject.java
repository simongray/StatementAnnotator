package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;

import java.util.*;

/**
 * This class represents the subject of a natural language statement.
 *
 * Explanation of terms used:
 *     (simple) subject = single word subject (a direct dependent of a verb)
 *     compound (subject) = multiple word subject (single word subject + relevant dependent words)
 *     complete subject = the complete subject, including all compound subjects
 *
 *     primary subject = the simple subject that is the primary dependent of the verb
 *     secondary subject = any other simple subjects contained in the complete subject
 */
public class StatementSubject {
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

    public StatementSubject(IndexedWord primarySubject, Set<IndexedWord> secondarySubjects, SemanticGraph graph) {
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
     * Returns the name of the complete subject.
     * @return the longest subject possible
     */
    public String getName() {
        return StatementUtils.join(new ArrayList<>(completeSubject));
    }

    /**
     * Returns the name of the primary simple subject.
     * @return the shortest subject possible
     */
    public String getShortName() {
        return primarySubject.word();
    }

    /**
     * Returns the simple subjects contained in the complete subject.
     * @return
     */
    public Set<IndexedWord> getSimpleSubjects() {
        Set<IndexedWord> simpleSubjects = new HashSet<>(secondarySubjects);
        simpleSubjects.add(primarySubject);
        return simpleSubjects;
    }

    /**
     * Returns the compound subjects contained in the complete subject.
     * @return
     */
    public Set<Set<IndexedWord>> getCompoundSubjects() {
        return new HashSet<>(compoundSubjects);
    }

    /**
     * Recursively finds the components of a compound subject.
     * @param simpleSubject the simple subject that serves as an entry point
     * @param graph the graph of the sentence
     * @return
     */
    private Set<IndexedWord> findCompoundComponents(IndexedWord simpleSubject, SemanticGraph graph) {
        Set<IndexedWord> compoundComponents = new HashSet<>();
        compoundComponents.add(simpleSubject);

        for (IndexedWord child : graph.getChildren(simpleSubject)) {
            GrammaticalRelation relation = graph.reln(simpleSubject, child);
            if (!IGNORED_RELATIONS.contains(relation.getShortName())) {
                compoundComponents.addAll(findCompoundComponents(child, graph));
            }
        }

        return compoundComponents;
    }

    /**
     * Returns the amount of individual subjects contained within the complete subject.
     * @return
     */
    public int size() {
        return secondarySubjects.size() + 1;
    }

    @Override
    public String toString() {
        return getName() + " (" + size() + ")";
    }
}
