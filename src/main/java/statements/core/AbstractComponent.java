package statements.core;


import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * A component of a natural language statement.
 */
public abstract class AbstractComponent implements StatementComponent {
    protected final IndexedWord primary;
    protected final Set<IndexedWord> complete;
    protected final Set<IndexedWord> words;  // used to make sure re-composed statements have access to all words
    protected final Set<IndexedWord> negations;
    protected final Set<IndexedWord> punctuation;
    protected final Set<IndexedWord> markers;
    protected final Set<IndexedWord> adverbialClauses;
    protected final Set<IndexedWord> nounClauses;

    public AbstractComponent(IndexedWord primary, SemanticGraph graph) {
        this.primary = primary;
        complete = StatementUtils.findCompound(primary, graph, getIgnoredRelations(), getOwnedScopes());

        // find negations
        negations = StatementUtils.findSpecificChildren(Relations.NEG, primary, graph);;

        // find punctuation
        punctuation = StatementUtils.findSpecificChildren(Relations.PUNCT, primary, graph);;

        // find markers
        markers = StatementUtils.findSpecificChildren(Relations.MARK, primary, graph);;

        // find clauses
        adverbialClauses = StatementUtils.findSpecificDescendants(Relations.ADVCL, primary, graph);
        nounClauses = StatementUtils.findSpecificDescendants(Relations.ACL, primary, graph);
        nounClauses.addAll(StatementUtils.findSpecificDescendants(Relations.ACL_RELCL, primary, graph));

        // TODO: find simpler and less resource intensive way of doing this
        // find ALL of the words of this component, to be used for recomposing into statement without missing words
        // only follows non-component specific ignored relations (e.g. dep, punct, mark)
        // this is done in order to not cross into the relations of other components
        Set<String> componentSpecificIgnoredRelations = new HashSet<>(getIgnoredRelations());
        componentSpecificIgnoredRelations.removeAll(Relations.IGNORED_RELATIONS);
        componentSpecificIgnoredRelations.addAll(Relations.IGNORED_INTER_COMPONENT_RELATIONS); // TODO: this is a retarded way of unignoring stuff
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
     * The primary word contained within the complete component.
     *
     * @return primary word
     */
    public IndexedWord getPrimary() {
        return primary;
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
     * The negations.
     *
     * @return negations
     */
    public Set<IndexedWord> getNegations() {
        return negations;
    }

    /**
     * The punctuation.
     *
     * @return punctuation
     */
    public Set<IndexedWord> getPunctuation() {
        return punctuation;
    }

    /**
     * All markers.
     *
     * @return markers
     */
    public Set<IndexedWord> getMarkers() {
        return markers;
    }

    /**
     * All adverbial clauses for every entry.
     *
     * @return adverbial clauses
     */
    public Set<IndexedWord> getAdverbialClauses() {
        return adverbialClauses;
    }

    /**
     * All noun clauses for every entry.
     *
     * @return noun clauses
     */
    public Set<IndexedWord> getNounClauses() {
        return nounClauses;
    }

    /**
     * All clauses (of all kinds) for every entry.
     *
     * @return clauses
     */
    public Set<IndexedWord> getClauses() {
        Set<IndexedWord> clauses = new HashSet<>();
        clauses.addAll(getAdverbialClauses());
        clauses.addAll(getNounClauses());
        return clauses;
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
     * Whether the component is negated.
     *
     * @return true if negated
     */
    public boolean isNegated() {
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
     * The amount of words contained within the complete component.
     *
     * @return word count
     */
    public int size() {
        return getComplete().size();
    }

    public boolean connectedTo(StatementComponent otherComponent) {
        return false;
    }

    @Override
    public String toString() {
        return "{" +
            getClass().getSimpleName() + ": \"" + getString() + "\"" +
            (!getClauses().isEmpty()? ", clause: \"" + StatementUtils.join(getClauses()) + "\"" : "")
        + "}";
    }
}
