package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.*;

/**
 * This class represents the subject of a natural language statement.
 */
public class StatementSubject {
    private final IndexedWord primarySubject;
    private final Set<IndexedWord> completeSubject;
    private final Map<IndexedWord, Set<IndexedWord>> compounds;

    public StatementSubject(IndexedWord primarySubject, Set<IndexedWord> secondarySubjects, SemanticGraph graph) {
        this.primarySubject = primarySubject;
        this.completeSubject = graph.descendants(primarySubject);

        compounds = new HashMap<>();
        compounds.put(primarySubject, graph.getChildren(primarySubject));
        for (IndexedWord subject : secondarySubjects) {
            compounds.put(subject, graph.getChildren(subject));
        }
    }

    /**
     * Returns the name of the simple subject.
     * @return the shortest subject possible
     */
    public String getSimpleName() {
        return primarySubject.word();
    }

    /**
     * Returns the name of the complete subject.
     * @return the longest subject possible
     */
    public String getCompleteName() {
        return StatementUtils.join(new ArrayList<>(completeSubject));
    }

    /**
     * Returns the simple subjects contained in the complete subject.
     * @return
     */
    public Set<IndexedWord> getSimpleSubjects() {
        return compounds.keySet();
    }

    /**
     * Returns the compound subjects contained in the complete subject.
     * @return
     */
    public Set<Set<IndexedWord>> getCompoundSubjects() {
        Set<Set<IndexedWord>> completeCompoundParts = new HashSet<>();
        for (IndexedWord simpleCompoundPart : getSimpleSubjects()) {
            Set<IndexedWord> compoundParts = new HashSet<>(compounds.get(simpleCompoundPart));  // TODO: is copying necessary?
            compoundParts.add(simpleCompoundPart);
            completeCompoundParts.add(compoundParts);
        }

        return completeCompoundParts;
    }

    @Override
    public String toString() {
        return getSimpleName() + " (" + getCompleteName() + ")";
    }
}
