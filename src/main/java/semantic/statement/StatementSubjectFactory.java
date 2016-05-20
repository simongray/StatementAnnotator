package semantic.statement;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.List;
import java.util.Set;

/**
 * This class handles the construction of subjects.
 */
public class StatementSubjectFactory {
    public static List<StatementSubject> getSubjects(SemanticGraph graph) {
        return null;
    }

    /**
     * Retrieves the words making up the complete subject based on the simple subject and its relations in the graph.
     * @param simpleSubject
     * @param graph
     * @return
     */
    private static Set<IndexedWord> getCompleteSubject(IndexedWord simpleSubject, SemanticGraph graph) {
        // TODO: create full name using relations such as compound and det (or perhaps by ignoring relations such as cc)
        return null;
    }

    /**
     * Retrieves simple subjects for the compound subject if applicable.
     * Useful for keeping track of the entire compound subject from any of its parts.
     * @param simpleSubject
     * @param graph
     * @return
     */
    private static Set<IndexedWord> getSubjectCompoundParts(IndexedWord simpleSubject, SemanticGraph graph) {
        // TODO: create links to other StatementSubject parts from conj:and and conj:or
        return null;
    }
}
