package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;

import java.util.HashSet;
import java.util.Set;


public abstract class AbstractFinder<T extends AbstractComponent> {
    /**
     * Find statement components based on the dependency graph of a sentence.
     *
     * @param graph the dependency graph of a sentence
     * @return statement components
     */
    public abstract Set<T> find(SemanticGraph graph);

    /**
     * The specific scopes that will not be considered when finding statement components.
     * Scopes are defined as "the compound based on the dependant of a relation + its descendants",
     * i.e. the scope of the acl relation would be the dependant in an acl relation and its descendants.
     *
     * @return ignored scopes
     */
    protected Set<String> getIgnoredScopes() {
        return Relations.IGNORED_SCOPES;
    }

    /**
     * The words that should not be used for component entries.
     * These words are found based on ignored scopes for specific relations.
     *
     * @param graph the dependency graph of a sentence
     * @return ignored words
     */
    protected Set<IndexedWord> getIgnoredWords(SemanticGraph graph) {
        Set<String> ignoredScopes = getIgnoredScopes();
        Set<IndexedWord> scopeEntries = new HashSet<>();
        Set<IndexedWord> ignoredWords = new HashSet<>();

        // discover all entry words for the ignored scopes
        for (TypedDependency dependency : graph.typedDependencies()) {
            if (ignoredScopes.contains(dependency.reln().getShortName())) {
                scopeEntries.add(dependency.dep());
            }
        }

        // use the entry words to find the scope compounds
        for (IndexedWord scopeEntry : scopeEntries) {
            ignoredWords.addAll(StatementUtils.findCompoundComponents(scopeEntry, graph, null));
        }

        return ignoredWords;
    }
}
