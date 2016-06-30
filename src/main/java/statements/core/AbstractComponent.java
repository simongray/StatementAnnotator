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
    protected final Set<IndexedWord> words;
    protected final Set<Set<IndexedWord>> compounds;
    protected final Map<IndexedWord, Set<IndexedWord>> negationMapping;
    private final Map<IndexedWord, Set<IndexedWord>> punctuationMapping;
    private final Map<IndexedWord, Set<IndexedWord>> markerMapping;

    // TODO: remove secondary requirement from constructor, find dynamically instead
    public AbstractComponent(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        this.primary = primary;
        this.secondary = secondary == null? new HashSet<>() : secondary;
        complete = StatementUtils.findCompound(primary, graph, getIgnoredRelations(), getOwnedScopes());

        // store primary word + secondary word(s) together for simple retrieval
        entries = new HashSet<>();
        entries.add(primary);
        for (IndexedWord word : this.secondary) {
            entries.add(word);
        }

        // recursively discover all compounds
        compounds = new HashSet<>();
        for (IndexedWord word : entries) {
            compounds.add(StatementUtils.findCompound(word, graph, getIgnoredCompoundRelations(), getOwnedScopes()));
        }

        // find negations for each entry
        negationMapping = StatementUtils.makeChildMap(entries, Relations.NEG, graph);

        // find punctuation for each entry
        punctuationMapping = StatementUtils.makeChildMap(entries, Relations.PUNCT, graph);

        // find markers for each entry
        markerMapping = StatementUtils.makeDescendantMap(entries, Relations.MARK, graph);

        // TODO: find simpler and less resource intensive way of doing this
        // find ALL of the words of this component, to be used for recomposing into statement without missing words
        // only follows non-component specific ignored relations (e.g. dep, punct, mark)
        // this is done in order to not cross into the relations of other components
        Set<String> componentSpecificIgnoredRelations = new HashSet<>(getIgnoredRelations());
        componentSpecificIgnoredRelations.removeAll(Relations.IGNORED_RELATIONS);
        words = StatementUtils.findCompound(primary, graph, componentSpecificIgnoredRelations, getOwnedScopes());
        words.addAll(getNegations());
        words.addAll(getMarkers());
        words.addAll(getPunctuation());
    }

    /**
     * Describes relations whose entire set of descendants is to be accepted, regardless of ignored relations.
     */
    protected Set<String> getOwnedScopes() {
        return Relations.IGNORED_SCOPES;
    }

    /**
     * Describes which relations are ignored when producing the complete subject.
     */
    protected Set<String> getIgnoredRelations() {
        return Relations.IGNORED_RELATIONS;
    }

    /**
     * Describes which relations are ignored when producing compound subjects.
     */
    protected Set<String> getIgnoredCompoundRelations() {
        return Relations.IGNORED_COMPOUND_RELATIONS;
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
     * The negations for all entries.
     *
     * @return negations
     */
    public Set<IndexedWord> getNegations() {
        Set<IndexedWord> negations = new HashSet<>();
        for (Set<IndexedWord> negationSet : negationMapping.values()) negations.addAll(negationSet);
        return negations;
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
     * The punctuation for all entries.
     *
     * @return punctuation
     */
    public Set<IndexedWord> getPunctuation() {
        Set<IndexedWord> punctuation = new HashSet<>();
        for (Set<IndexedWord> punctuationSet : punctuationMapping.values()) punctuation.addAll(punctuationSet);
        return punctuation;
    }

    /**
     * The markers for a specific entry
     *
     * @param entry the entry to get the markers for
     * @return markers for the entry
     */
    public Set<IndexedWord> getMarkers(IndexedWord entry) throws MissingEntryException {
        if (!markerMapping.containsKey(entry)) throw new MissingEntryException(entry + " is not a part of this component");
        return new HashSet<>(markerMapping.get(entry));
    }

    /**
     * All markers for every entry.
     *
     * @return markers
     */
    public Set<IndexedWord> getMarkers() {
        Set<IndexedWord> markers = new HashSet<>();
        for (Set<IndexedWord> markerSet : markerMapping.values()) markers.addAll(markerSet);
        return markers;
    }

    /**
     * All of the words (including ignored ones) for this component.
     * Useful for recomposing into full statements.
     *
     * @return
     */
    public Set<IndexedWord> getWords() {
        return words;
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
