package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Set;

/**
 * The complete indirect object of a natural language statement.
 */
public class IndirectObject extends AbstractComponent {

    public IndirectObject(IndexedWord primary, SemanticGraph graph, Set<String> labels, Set<IndexedWord> conjunction) {
        super(primary, graph, labels);
        this.conjunction.addAll(conjunction);  // often are not available through conj relation
    }

    public IndirectObject(IndexedWord primary, SemanticGraph graph, Set<String> labels) {
        super(primary, graph, labels);
    }
}
