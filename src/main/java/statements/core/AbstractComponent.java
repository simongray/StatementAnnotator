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
    protected final SemanticGraph graph;

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
    protected final Set<IndexedWord> remaining;

    /**
     * The incoming connections of the primary word, i.e. its governors.
     * Used to find connections between different components.
     */
    protected final Set<IndexedWord> governors;

    /**
     * Conjunctions (= sibling components).
     */
    protected final Set<IndexedWord> conjunction;

    /**
     * Labels are useful to document certain special behaviour of a component.
     */
    protected final Set<String> labels;

    protected final Set<IndexedWord> negations;
    protected final Set<IndexedWord> punctuation;
    protected final Set<IndexedWord> markers;
    protected final Set<IndexedWord> cc;
    protected final Set<IndexedWord> adverbialClauses;
    protected final Set<IndexedWord> nounClauses;

    public AbstractComponent(IndexedWord primary, SemanticGraph graph) {
       this(primary, graph, new HashSet<>());
    }

    public AbstractComponent(IndexedWord primary, SemanticGraph graph, Set<String> labels) {
        this.primary = primary;
        this.graph = graph;
        compound = StatementUtils.findCompound(primary, graph, getIgnoredRelations(), null);

        // relevant parts of the component that have been separated out from the compound
        negations = StatementUtils.findSpecificChildren(Relations.NEG, primary, graph);
        punctuation = StatementUtils.findSpecificChildren(Relations.PUNCT, primary, graph);
        markers = StatementUtils.findSpecificChildren(Relations.MARK, primary, graph);
        cc = StatementUtils.findSpecificChildren(Relations.CC, primary, graph);
        adverbialClauses = StatementUtils.findSpecificDescendants(Relations.ADVCL, primary, graph);
        nounClauses = StatementUtils.findSpecificDescendants(Relations.ACL, primary, graph);
        nounClauses.addAll(StatementUtils.findSpecificDescendants(Relations.ACL_RELCL, primary, graph));

        // conjunction are used to loosely "link" separate statements
        conjunction = StatementUtils.findSpecificChildren(Relations.CONJ, primary, graph);
        conjunction.addAll(StatementUtils.findSpecificParents(Relations.CONJ, primary, graph));

        // the stuff that doesn't go into the compound
        // used by containing statements to reproduce the statement text
        remaining = new HashSet<>();
        remaining.addAll(negations);
        remaining.addAll(markers);
        remaining.addAll(adverbialClauses);
        remaining.addAll(nounClauses);

        // the governors/parents of the component
        // some relations are ignored, e.g. the conj relation which is not treated as governor since it defines siblings
        governors = new HashSet<>();
        for (SemanticGraphEdge edge : graph.incomingEdgeList(primary)) {
            // it is important to leave out certain governor relations
            // (e.g. the conj relation, since multiple of the same component type should not be connecting)
            if (!Relations.IGNORED_CONNECTING_RELATIONS.contains(edge.getRelation().getShortName())) {
                governors.add(edge.getGovernor());
            }
        }

        this.labels = labels;
    }

    /**
     * Describes which relations are ignored when producing the compound.
     */
    protected Set<String> getIgnoredRelations() {
        return Relations.IGNORED_OUTGOING_RELATIONS;
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
     * Every main word of the component.
     *
     * @return compound
     */
    public Set<IndexedWord> getCompound() {
        return compound;
    }

    /**
     * The remaining words of the component.
     *
     * @return compound
     */
    public Set<IndexedWord> getRemaining() {  // TODO: rename
        return remaining;
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
     * All conjunction words.
     *
     * @return markers
     */
    public Set<IndexedWord> getCc() {
        return cc;
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
     * Conjunctions.
     *
     * @return conjunction
     */
    public Set<IndexedWord> getConjunction() {
        return conjunction;
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
        Set<IndexedWord> expandedCompound = getCompound();
        expandedCompound.addAll(getNegations());
        return StatementUtils.join(expandedCompound);
    }

    /**
     * The labels of component.
     * In most cases, components don't have a label, but in some cases they can be useful.
     * Component labels can be used to label their containing statements.
     * Labeling containing statements can provide information
     * on, for example, how an embedded statement fits into its parent statement.
     *
     * @return labels
     */
    public Set<String> getLabels() {
        return labels;
    }

    public SemanticGraph getGraph() {
        return graph;
    }

    /**
     * Attach a label to this component.  // TODO: is this needed?
     */
    public void addLabel(String label) {
        labels.add(label);
    }

    @Override
    public boolean contains(StatementComponent otherComponent) {
        return false;
    }

    @Override
    public String toString() {
        Set<String> conjunctions = new HashSet<>();
        for (IndexedWord word : getConjunction()) {
            conjunctions.add(word.word());
        }

        return "{" +
            getClass().getSimpleName() + ": \"" + getString() + "\"" +
            (!getClauses().isEmpty()? ", clause: \"" + StatementUtils.join(getClauses()) + "\"" : "") +
            (!getLabels().isEmpty()? ", labels: \"" + String.join(", ", getLabels()) + "\"" : "") +
            (!conjunctions.isEmpty()? ", conjunction: \"" + String.join(", ", conjunctions) + "\"" : "") +
        "}";
    }
}
