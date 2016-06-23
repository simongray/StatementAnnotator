package statements.core;


import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import statements.core.exceptions.MissingEntryException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A component of a natural language statement.
 */
public abstract class AbstractComponent implements StatementComponent {
    protected final IndexedWord primary;
    protected final Set<IndexedWord> secondary;
    protected final Set<IndexedWord> entries;
    protected final Set<IndexedWord> complete;
    protected final Set<Set<IndexedWord>> compounds;
    protected final Map<IndexedWord, Set<IndexedWord>> negationMapping;
    private final Map<IndexedWord, Set<IndexedWord>> punctuationMapping;

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

        // find negations for each entry
        negationMapping = StatementUtils.makeRelationsMap(entries, Relations.NEG, graph);

        // find punctuation for each entry
        punctuationMapping = StatementUtils.makeRelationsMap(entries, Relations.PUNCT, graph);
    }

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
     * The negations for a specific entry
     *
     * @param entry the entry to get the negations for
     * @return negations for the entry
     */
    public Set<IndexedWord> getNegations(IndexedWord entry) throws MissingEntryException {
        if (!negationMapping.containsKey(entry)) throw new MissingEntryException(entry + " is not a part of this component");
        return new HashSet<>(negationMapping.get(entry));
    }

    /**
     * The punctuation for a specific entry
     *
     * @param entry the entry to get the punctuation for
     * @return punctuation for the entry
     */
    public Set<IndexedWord> getPunctuation(IndexedWord entry) throws MissingEntryException {
        if (!punctuationMapping.containsKey(entry)) throw new MissingEntryException(entry + " is not a part of this component");
        return new HashSet<>(punctuationMapping.get(entry));
    }

    /**
     * Whether specific entry is negated.
     *
     * @param entry the entry to get the negation status for
     * @return true if negated
     */
    public boolean isNegated(IndexedWord entry) throws MissingEntryException {
        Set<IndexedWord> negations = getNegations(entry);
        return negations.size() % 2 != 0;
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
        return "{" + getClass().getSimpleName() + ": " + getString() + (count() > 1? ", count: " + count() : "") + "}";
    }
}
