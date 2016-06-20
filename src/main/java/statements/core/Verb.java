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
        IGNORED_RELATIONS.add("nsubj");
        IGNORED_RELATIONS.add("nsubjpass");
        IGNORED_RELATIONS.add("dobj");  // "<primary>s <primary>ing"
        IGNORED_RELATIONS.add("nmod");  // "<primary> to/from/etc. <object>"
        IGNORED_RELATIONS.add("xcomp");  // "<primary>s to <primary>"
        IGNORED_RELATIONS.add("ccomp");  // <primary> that <statement>, e.g. "we have <found> that <it is great>"
        IGNORED_RELATIONS.add("conj");  // connections to other verbs
        IGNORED_RELATIONS.add("cc");  // "and", "or", etc.

        IGNORED_COMPOUND_RELATIONS.addAll(IGNORED_RELATIONS);
    }

    private static final String NEG_RELATION = "neg";
    private static final String CC_RELATION = "cc";
    private static final String MARK_RELATION = "mark";  // for markers, e.g. "whether"
    private final Map<IndexedWord, Set<IndexedWord>> markerMap;
    private final Set<IndexedWord> negations;
    private final Set<IndexedWord> conjunctions;

    public Verb(IndexedWord primary, SemanticGraph graph) {
        super(primary, null, graph);

        // find specific words
        this.negations = StatementUtils.findSpecificDescendants(NEG_RELATION, primary, graph);
        this.conjunctions = StatementUtils.findSpecificDescendants(CC_RELATION, primary, graph);

        // find markers (only relevant for COP)
        markerMap = StatementUtils.makeRelationsMap(entries, MARK_RELATION, graph);
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
     * The resemblance of another verb to this verb.
     *
     * @param otherVerb subject to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(Verb otherVerb) {  // TODO: implement fully
        if (otherVerb == null) {
            return Resemblance.NONE;
        }

        if (getString().equals(otherVerb.getString())) {
            return Resemblance.FULL;
        }

        if (isNegated() == otherVerb.isNegated()) {
//            if (getLemma().equals(otherVerb.getLemma())) {
//                return Resemblance.CLOSE;
//            }
            // TODO: perform resemblance comparison using synonyms
        } else {
            // TODO: perform resemblance comparison using antonyms (can never be full, but can be close)
        }

        return Resemblance.NONE;
    }
}
