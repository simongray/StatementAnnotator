package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

/**
 * The complete verb of a natural language statement.
 */
public class CompleteVerb implements Resembling<CompleteVerb> {
    private final IndexedWord verb;

    public CompleteVerb(IndexedWord verb, SemanticGraph graph) {
        this.verb = verb;
    }

    /**
     * The name of the complete verb.
     * @return the longest subject possible
     */
    public String getName() {
        return verb.word();  // TODO: implement full version
    }

    /**
     * The lemmatised version of the verb.
     * @return
     */
    public String getLemma() {
        return verb.lemma();
    }

    /**
     * The resemblance of another verb to this verb.
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
