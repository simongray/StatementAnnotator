package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * The complete verb of a natural language statement.
 */
public class Verb implements StatementComponent, Resembling<Verb> {
    /**
     * Describes which relations are ignored when producing compound subjects.
     */
    private static final Set<String> IGNORED_RELATIONS = new HashSet<>();
    static {
        IGNORED_RELATIONS.add("nsubj");
        IGNORED_RELATIONS.add("nsubjpass");
        IGNORED_RELATIONS.add("dobj");  // "<verb>s <verb>ing"
        IGNORED_RELATIONS.add("nmod");  // "<verb> to/from/etc. <object>"
        IGNORED_RELATIONS.add("xcomp");  // "<verb>s to <verb>"
        IGNORED_RELATIONS.add("cc");  // "and", "or", etc.
        IGNORED_RELATIONS.add("conj");  // connections to other verbs
    }
    private static final String NEG_RELATION = "neg";
    private final IndexedWord verb;
    private final Set<IndexedWord> compound;
    private final Set<IndexedWord> negations;

    public Verb(IndexedWord verb, SemanticGraph graph) {
        this.verb = verb;

        // recursively discover all compound subjects
        this.compound = StatementUtils.findCompoundComponents(verb, graph, IGNORED_RELATIONS);

        // find negations
        this.negations = StatementUtils.findSpecificDescendants(NEG_RELATION, verb, graph);
    }

    /**
     * The name of the complete verb.
     * @return the longest verb possible
     */
    public String getName() {
        return StatementUtils.join(compound);
    }

    /**
     * The primary verb.
     * @return the shortest verb possible
     */
    public IndexedWord getPrimary() {
        return verb;
    }

    /**
     * The verb compound.
     * @return compound
     */
    public Set<IndexedWord> getCompound() {
        return new HashSet<>(compound);
    }

    /**
     * The lemmatised version of the verb.
     * @return
     */
    public String getLemma() {
        return verb.lemma();
    }

    /**
     * The negations for the verb.
     * @return negations
     */
    public Set<IndexedWord> getNegations() {
        return new HashSet<>(negations);
    }

    /**
     * Whether the verb is negated.
     * @return
     */
    public boolean isNegated() {
        return StatementUtils.isNegated(negations);
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
        return "V: " + getName() + " (" + (isNegated()? "!": "") + getLemma() + ")";
    }
}
