package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * The complete primary of a natural language statement.
 */
public class Verb implements StatementComponent, Resembling<Verb> {
    /**
     * Describes which relations are ignored when producing compound subjects.
     */
    private static final Set<String> IGNORED_RELATIONS = new HashSet<>();
    static {
        IGNORED_RELATIONS.add("nsubj");
        IGNORED_RELATIONS.add("nsubjpass");
        IGNORED_RELATIONS.add("dobj");  // "<primary>s <primary>ing"
        IGNORED_RELATIONS.add("nmod");  // "<primary> to/from/etc. <object>"
        IGNORED_RELATIONS.add("xcomp");  // "<primary>s to <primary>"
        IGNORED_RELATIONS.add("ccomp");  // <primary> that <statement>, e.g. "we have <found> that <it is great>"
    }
    private static final Set<String> IGNORED_COMPOUND_RELATIONS = new HashSet<>();
    static {
        IGNORED_COMPOUND_RELATIONS.addAll(IGNORED_RELATIONS);
        IGNORED_COMPOUND_RELATIONS.add("cc");  // "and", "or", etc.
        IGNORED_COMPOUND_RELATIONS.add("conj");  // connections to other verbs
    }

    private static final String NEG_RELATION = "neg";
    private static final String CC_RELATION = "cc";
    private final IndexedWord primary;
    private Set<IndexedWord> secondary;
    private final Set<IndexedWord> complete;
    private final Set<Set<IndexedWord>> compounds;
    private final Set<IndexedWord> negations;
    private final Set<IndexedWord> conjuctions;

    public Verb(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        this.primary = primary;
        this.secondary = secondary;
        this.complete = StatementUtils.findCompoundComponents(primary, graph, IGNORED_RELATIONS);

        // recursively discover all compound subjects
        // recursively discover all compound subjects
        compounds = new HashSet<>();
        compounds.add(StatementUtils.findCompoundComponents(primary, graph, IGNORED_COMPOUND_RELATIONS));
        for (IndexedWord subject : secondary) {
            compounds.add(StatementUtils.findCompoundComponents(subject, graph, IGNORED_COMPOUND_RELATIONS));
        }

        // find specific words
        this.negations = StatementUtils.findSpecificDescendants(NEG_RELATION, primary, graph);
        this.conjuctions = StatementUtils.findSpecificDescendants(CC_RELATION, primary, graph);
    }

    /**
     * The name of the complete verb.
     * @return the longest verb possible
     */
    public String getName() {
        return StatementUtils.join(complete);
    }

    /**
     * The primary verb.
     * @return the shortest verb possible
     */
    public IndexedWord getPrimary() {
        return primary;
    }

    /**
     * The verb compound.
     * @return compound
     */
    public Set<Set<IndexedWord>> getCompounds() {
        return new HashSet<>(compounds);
    }

    /**
     * The negations for the verb.
     * @return negations
     */
    public Set<IndexedWord> getNegations() {
        return new HashSet<>(negations);
    }

    /**
     * The conjunctions for the verb.
     * @return negations
     */
    public Set<IndexedWord> getConjuctions() {
        return new HashSet<>(conjuctions);
    }

    /**
     * Whether the verb is negated.
     * @return
     */
    public boolean isNegated() {
        return StatementUtils.isNegated(negations);
    }

    /**
     * The amount of individual verbs contained within the complete verb.
     * @return verbs count
     */
    public int size() {
        return secondary.size() + 1;
    }

    /**
     * The resemblance of another verb to this verb.
     * @param otherVerb subject to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(Verb otherVerb) {  // TODO: implement fully
        if (otherVerb == null) {
            return Resemblance.NONE;
        }

        if (getName().equals(otherVerb.getName())) {
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

    @Override
    public String toString() {
        return "V: " + getName() + " (" + (isNegated()? "not ": "") + getPrimary().word() + ", " + size() +")";
    }
}
