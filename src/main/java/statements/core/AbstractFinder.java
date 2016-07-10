package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;

import java.util.*;


public abstract class AbstractFinder<T extends AbstractComponent> {
    protected SemanticGraph graph;
    protected Collection<TypedDependency> dependencies;
    protected Map<IndexedWord, IndexedWord> conjunctions = new HashMap<>();  // dep-to-gov
    protected Set<IndexedWord> ignoredWords;
    protected Set<T> components;

    /**
     * Find statement components based on the dependency graph of a sentence.
     * Note: cannot be overridden in subclasses. Implement methods init(...), check(...), and get(...) instead.
     *
     * @param graph the dependency graph of a sentence
     * @return statement components
     */
    public final Set<T> find(SemanticGraph graph) {
        // initialise the fields to a neutral state
        this.graph = graph;
        ignoredWords = getIgnoredWords(graph);
        dependencies = graph.typedDependencies();
        init();

        // find relevant connections
        for (TypedDependency dependency : dependencies) {
            findConjunctions(dependency);
            check(dependency);
        }

        // produce components based on the connections
        components = get();

        return components;
    }

    /**
     * Initialise the fields of the finder prior to starting the finding process.
     * Needs to be implemented by subclasses.
     */
    protected abstract void init();

    /**
     * Check dependency and store relevant information.
     * Needs to be implemented by subclasses.
     *
     * @param dependency
     */
    protected abstract void check(TypedDependency dependency);

    /**
     * The components produced by this finder.
     * Needs to be implemented by subclasses.
     *
     * @return
     */
    protected abstract Set<T> get();

    /**
     * Generalised way to get labels for a new component.
     *
     * @param primary
     * @param optionalLabels
     * @return labels
     */
    protected final Set<String> getLabels(IndexedWord primary, String... optionalLabels) {
        Set<String> labels = new HashSet<>();

        // assign conjunction label if applicable
        if (conjunctions.keySet().contains(primary)) {
            labels.add(Labels.CONJ_CHILD_VERB);
        } else if (conjunctions.values().contains(primary)) {
            labels.add(Labels.CONJ_PARENT_VERB);
        }

        // assign the optional labels
        for (String optionalLabel : optionalLabels) {
            labels.add(optionalLabel);
        }

        return labels;
    }

    /**
     * Put both dependent and governor from a typed dependency into a map.
     *
     * @param mapping
     * @param dependency
     * @param relation
     */
    protected final void updateMapping(Map<IndexedWord, IndexedWord> mapping, TypedDependency dependency, String relation) {
        if (dependency.reln().getShortName().equals(relation)) {
            if (!ignoredWords.contains(dependency.dep())) mapping.put(dependency.dep(), dependency.gov());
        }
    }

    /**
     * Attach the dependent from a typed dependency with a given relation.
     *
     * @param words
     * @param dependency
     * @param relation
     * @return dependent
     */
    protected final void addDependent(Set<IndexedWord> words, TypedDependency dependency, String relation) {
        if (dependency.reln().getShortName().equals(relation)) {
            if (!ignoredWords.contains(dependency.dep())) words.add(dependency.dep());
        }
    }

    /**
     * Attach the governor from a typed dependency with a given relation.
     *
     * @param words
     * @param dependency
     * @param relation
     * @return dependent
     */
    protected final void addGovernor(Set<IndexedWord> words, TypedDependency dependency, String relation) {
        if (dependency.reln().getShortName().equals(relation)) {
            if (!ignoredWords.contains(dependency.gov())) words.add(dependency.gov());
        }
    }

    /**
     * Retrieves the conjunctions (child-parent) of a dependency.
     *
     * @param dependency
     */
    protected final void findConjunctions(TypedDependency dependency) {
        if (dependency.reln().getShortName().equals(Relations.CONJ)) {
            if (!ignoredWords.contains(dependency.gov())) {
                conjunctions.put(dependency.dep(), dependency.gov());
            }
        }
    }

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
    protected final Set<IndexedWord> getIgnoredWords(SemanticGraph graph) {
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
            ignoredWords.addAll(StatementUtils.findCompound(scopeEntry, graph));
        }

        return ignoredWords;
    }
}
