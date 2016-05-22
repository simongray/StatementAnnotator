package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * The complete simpleVerb of a natural language statement.
 */
public class CompleteVerb implements Resembling<CompleteVerb> {
    /**
     * Describes which relations are ignored when producing compound subjects.
     */
    private static final Set<String> IGNORED_RELATIONS = new HashSet<>();
    static {
        IGNORED_RELATIONS.add("nsubj");
        IGNORED_RELATIONS.add("dobj");  // "<verb>s <verb>ing"
        IGNORED_RELATIONS.add("nmod");  // "<verb> to/from/etc. <object>"
        IGNORED_RELATIONS.add("xcomp");  // "<verb>s to <verb>"
    }
    private static final String NEG = "neg";
    private final IndexedWord simpleVerb;
    private final Set<IndexedWord> compoundVerb;
    private final boolean negated;

    public CompleteVerb(IndexedWord simpleVerb, SemanticGraph graph) {
        this.simpleVerb = simpleVerb;

        // recursively discover all compound subjects
        this.compoundVerb = StatementUtils.findCompoundComponents(simpleVerb, graph, IGNORED_RELATIONS);
        this.negated = StatementUtils.isNegated(simpleVerb, graph);
    }

    /**
     * The name of the complete verb.
     * @return the longest verb possible
     */
    public String getName() {
        return StatementUtils.join(compoundVerb);
    }

    /**
     * The name of the primary simple verb.
     * @return the shortest verb possible
     */
    public String getShortName() {
        return simpleVerb.word();
    }

    /**
     * The lemmatised version of the simpleVerb.
     * @return
     */
    public String getLemma() {
        return simpleVerb.lemma();
    }

    /**
     * Whether the verb is negated.
     * @return
     */
    public boolean isNegated() {
        return negated;
    }

    /**
     * The resemblance of another simpleVerb to this simpleVerb.
     * @param otherVerb subject to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(CompleteVerb otherVerb) {  // TODO: implement fully
        if (getName().equals(otherVerb.getName())) {
            return Resemblance.FULL;
        }

        if (isNegated() == otherVerb.isNegated()) {
            if (getLemma().equals(otherVerb.getLemma())) {
                return Resemblance.CLOSE;
            }
            // TODO: perform resemblance comparison using synonyms
        } else {
            // TODO: perform resemblance comparison using antonyms (can never be full, but can be close)
        }

        return Resemblance.NONE;
    }

    @Override
    public String toString() {
        return getName() + " (" + (isNegated()? "!": "") + getLemma() + ")";
    }
}
