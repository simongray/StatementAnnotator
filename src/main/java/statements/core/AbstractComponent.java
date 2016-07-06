package statements.core;


import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

import java.util.HashSet;
import java.util.Set;

/**
 * A component of a natural language statement.
 */
public abstract class AbstractComponent implements StatementComponent {
    /**
     * The primary word of the component.
     * Used as the entry point for the rest of the compound.
     */
    protected final IndexedWord primary;

    /**
     * The main compound derived from the outgoing connections of the primary word.
     * Represents the entire component, although it does not include specific parts
     * - such as negations - that are found separately.
     */
    protected final Set<IndexedWord> compound;

    /**
     * The incoming connections of the primary word, i.e. its governors.
     * Used to find connections between different components.
     */
    protected final Set<IndexedWord> governors;

    protected final String label;

    protected final Set<IndexedWord> negations;
    protected final Set<IndexedWord> punctuation;
    protected final Set<IndexedWord> markers;
    protected final Set<IndexedWord> adverbialClauses;
    protected final Set<IndexedWord> nounClauses;

    public AbstractComponent(IndexedWord primary, SemanticGraph graph) {
       this(primary, graph, null);
    }

    public AbstractComponent(IndexedWord primary, SemanticGraph graph, String label) {
        this.primary = primary;
        compound = StatementUtils.findCompound(primary, graph, getIgnoredRelations(), getOwnedScopes());

        // relevant parts of the component that have been separated out from the compound
        negations = StatementUtils.findSpecificChildren(Relations.NEG, primary, graph);
        punctuation = StatementUtils.findSpecificChildren(Relations.PUNCT, primary, graph);
        markers = StatementUtils.findSpecificChildren(Relations.MARK, primary, graph);
        adverbialClauses = StatementUtils.findSpecificDescendants(Relations.ADVCL, primary, graph);
        nounClauses = StatementUtils.findSpecificDescendants(Relations.ACL, primary, graph);
        nounClauses.addAll(StatementUtils.findSpecificDescendants(Relations.ACL_RELCL, primary, graph));

        // the governors/parents of the component
        governors = new HashSet<>();
        for (SemanticGraphEdge edge : graph.incomingEdgeList(primary)) {
            // it is important to leave out certain governor relations
            // (e.g. the conj relation, since multiple of the same component type should not be connecting)
            if (!Relations.IGNORED_OUTGOING_RELATIONS.contains(edge.getRelation().getShortName())) {
                governors.add(edge.getGovernor());
            }
        }

        this.label = label;
    }

    /**
     * Describes which relations are ignored when producing the compound.
     */
    protected Set<String> getIgnoredRelations() {
        return Relations.IGNORED_RELATIONS;
    }

    /**
     * Describes relations whose entire set of descendants is to be accepted, regardless of ignored relations.
     */
    protected Set<String> getOwnedScopes() {
        return Relations.IGNORED_SCOPES;
    }

    /**
     * The primary word contained within the compound component.
     *
     * @return primary word
     */
    public IndexedWord getPrimary() {
        return primary;
    }

    /**
     * Every word of the component.
     *
     * @return compound
     */
    public Set<IndexedWord> getCompound() {
        return compound;
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
     * Outgoing (= governing) compound.
     * Useful for establishing whether this statement component is connected to another component.
     *
     * @return governors
     */
    public Set<IndexedWord> getGovernors() {
        return governors;
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
     * The compound as a string.
     *
     * @return the longest string possible
     */
    protected String getString() {
        return StatementUtils.join(getCompound());
    }

    /**
     * The label of component.
     * In most cases, components don't have a label, but in some cases they can be useful.
     * Component labels can be used to label their containing statements.
     * Labeling containing statements can provide information
     * on, for example, how an embedded statement fits into its parent statement.
     *
     * @return label
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "{" +
            getClass().getSimpleName() + ": \"" + getString() + "\"" +
            (!getClauses().isEmpty()? ", clause: \"" + StatementUtils.join(getClauses()) + "\"" : "") +
            ((getLabel() != null)? ", label: \"" + getLabel() + "\"" : "") +
        "}";
    }
}
