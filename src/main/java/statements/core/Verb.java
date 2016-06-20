package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The complete primary of a natural language statement.
 */
public class Verb extends AbstractComponent implements Resembling<Verb> {

    private static final Set<String> IGNORED_RELATIONS = new HashSet<>();
    private static final Set<String> IGNORED_COMPOUND_RELATIONS = new HashSet<>();

    static {
        IGNORED_RELATIONS.add(Relations.NSUBJ);
        IGNORED_RELATIONS.add(Relations.NSUBJPASS);
        IGNORED_RELATIONS.add(Relations.DOBJ);  // "<primary>s <primary>ing"
        IGNORED_RELATIONS.add(Relations.NMOD);  // "<primary> to/from/etc. <object>"
        IGNORED_RELATIONS.add(Relations.XCOMP);  // "<primary>s to <primary>"
        IGNORED_RELATIONS.add(Relations.CCOMP);  // <primary> that <statement>, e.g. "we have <found> that <it is great>"

        IGNORED_COMPOUND_RELATIONS.addAll(IGNORED_RELATIONS);
        IGNORED_COMPOUND_RELATIONS.add(Relations.CONJ); // connections to other verbs
        IGNORED_COMPOUND_RELATIONS.add(Relations.CC);  // "and", "or", etc.
    }

    private final Map<IndexedWord, Set<IndexedWord>> markerMap;
    private final Set<IndexedWord> negations;
    private final Set<IndexedWord> conjunctions;

    public Verb(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        super(primary, secondary, graph);

        // find specific words
        this.negations = StatementUtils.findSpecificDescendants(Relations.NEG, primary, graph);
        this.conjunctions = StatementUtils.findSpecificDescendants(Relations.CC, primary, graph);

        // find markers (only relevant for COP)
        markerMap = StatementUtils.makeRelationsMap(entries, Relations.MARK, graph);
    }

    /**
     * Describes which relations are ignored when producing the complete subject.
     */
    protected Set<String> getIgnoredRelations() {
        return IGNORED_RELATIONS;
    }

    /**
     * Describes which relations are ignored when producing compound subjects.
     */
    protected Set<String> getIgnoredCompoundRelations() {
        return IGNORED_COMPOUND_RELATIONS;
    }

    /**
     * The string of the complete verb.
     *
     * @return the longest string possible
     */
    public String getString() {
        return StatementUtils.join(StatementUtils.without(getMarkers(), complete));
    }

    /**
     * The negations for the verb.
     *
     * @return negations
     */
    public Set<IndexedWord> getNegations() {
        return new HashSet<>(negations);
    }

    /**
     * The conjunctions for the verb.
     *
     * @return negations
     */
    public Set<IndexedWord> getConjunctions() {
        return new HashSet<>(conjunctions);
    }

    /**
     * Whether the verb is negated.
     *
     * @return negation status
     */
    public boolean isNegated() {
        return StatementUtils.isNegated(negations);
    }

    /**
     * Markers for all contained verbs.
     *
     * @return copulas
     */
    public Set<IndexedWord> getMarkers() {
        Set<IndexedWord> markers = new HashSet<>();
        for (Set<IndexedWord> markerSet : markerMap.values()) {
            markers.addAll(markerSet);  // TODO: needs to search though descendants too
        }
        return markers;
    }

    /**
     * The resemblance of this object to another object.
     *
     * @param otherObject subject to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(Verb otherObject) {
        return null;  // TODO
    }
}
