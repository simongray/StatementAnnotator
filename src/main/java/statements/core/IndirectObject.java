package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Set;

/**
 * The complete indirect object of a natural language statement.
 */
public class IndirectObject extends AbstractComponent implements Resembling<IndirectObject> {

    public IndirectObject(IndexedWord primary, SemanticGraph graph, Set<String> labels, Set<IndexedWord> conjunction) {
        super(primary, graph, labels);
        this.conjunction.addAll(conjunction);  // often are not available through conj relation
    }

    public IndirectObject(IndexedWord primary, SemanticGraph graph, Set<String> labels) {
        super(primary, graph, labels);
    }

    /**
     * The resemblance of another indirect object to this indirect object.
     *
     * @param otherObject object to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(IndirectObject otherObject) {
        return null;  // TODO
    }
}
