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
     * The resemblance of another verb to this verb.
     * @param otherVerb subject to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(CompleteVerb otherVerb) {
        return null;
    }

    @Override
    public String toString() {
        return verb.word() + " (" + verb.lemma() + ")";
    }
}
