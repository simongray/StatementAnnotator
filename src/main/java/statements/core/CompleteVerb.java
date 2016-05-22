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
        IGNORED_RELATIONS.add("aux");  // "<subject>'s <verb>ing"
    }
    private final IndexedWord simpleVerb;
    private final Set<IndexedWord> compoundVerb;

    public CompleteVerb(IndexedWord simpleVerb, SemanticGraph graph) {
        this.simpleVerb = simpleVerb;
        // TODO: implement compound creation, needs to be smart enough to find negatives etc.

        // recursively discover all compound subjects
        this.compoundVerb = StatementUtils.findCompoundComponents(simpleVerb, graph, IGNORED_RELATIONS);
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
     * The resemblance of another simpleVerb to this simpleVerb.
     * @param otherVerb subject to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(CompleteVerb otherVerb) {  // TODO: implement fully
        if (getName().equals(otherVerb.getName())) {
            return Resemblance.FULL;
        }

        if (getLemma().equals(otherVerb.getLemma())) {  // TODO: needs to be a bit more precise, also compare negative
            return Resemblance.CLOSE;
        }

        return Resemblance.NONE;
    }

    @Override
    public String toString() {
        return getName() + " (" + getLemma() + ")";
    }
}
