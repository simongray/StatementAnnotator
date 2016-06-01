package statements.core;


import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * A component of a natural language statement.
 */
public abstract class AbstractComponent implements StatementComponent {
    /**
     * Describes which relations are ignored when producing the complete subject.
     */
    protected Set<String> getIgnoredRelations() {
        return null;
    }

    /**
     * Describes which relations are ignored when producing compound subjects.
     */
    protected Set<String> getIgnoredCompoundRelations() {
        return null;
    }

    protected final IndexedWord primary;
    protected final Set<IndexedWord> secondary;
    protected final Set<IndexedWord> entries;
    protected final Set<IndexedWord> complete;
    protected final Set<Set<IndexedWord>> compounds;

    // TODO: remove secondary requirement from constructor, find dynamically instead
    public AbstractComponent(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        this.primary = primary;
        this.secondary = secondary == null? new HashSet<>() : secondary;
        this.complete = StatementUtils.findCompoundComponents(primary, graph, getIgnoredRelations());

        // store primary word + secondary word(s) together for simple retrieval
        this.entries = new HashSet<>();
        entries.add(primary);
        for (IndexedWord word : this.secondary) {
            entries.add(word);
        }

        // recursively discover all compounds
        compounds = new HashSet<>();
        for (IndexedWord word : entries) {
            compounds.add(StatementUtils.findCompoundComponents(word, graph, getIgnoredCompoundRelations()));
        }
    }

    /**
     * The primary word contained within the complete component.
     *
     * @return primary word
     */
    public IndexedWord getPrimary() {
        return primary;
    }

    /**
     * The entries of the complete component.
     *
     * @return compounds
     */
    public Set<IndexedWord> getEntries() {
        return entries;
    }

    /**
     * Every word of the complete component.
     *
     * @return complete component
     */
    public Set<IndexedWord> getComplete() {
        return complete;
    }

    /**
     * The compounds of the complete component.
     *
     * @return compounds
     */
    public Set<Set<IndexedWord>> getCompounds() {
        return compounds;
    }

    /**
     * The string of the complete component.
     *
     * @return the longest string possible
     */
    protected String getString() {
        return StatementUtils.join(getComplete());
    }

    /**
     * The amount of individual compounds contained within the complete component.
     *
     * @return compound count
     */
    public int count() {
        return getCompounds().size();
    }

    /**
     * The amount of words contained within the complete component.
     *
     * @return word count
     */
    public int size() {
        return getComplete().size();
    }

    @Override
    public String toString() {
        return "{" + getClass().getSimpleName() + ": " + getString() + "}";
    }
}
