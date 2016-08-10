package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;


/**
 * The complete subject of a natural language statement.
 */
public class Subject extends AbstractComponent {
    public Subject(IndexedWord primary, SemanticGraph graph) {
        super(primary, graph);

        // nmod relations from nouns are typically descriptive in nature
        otherDescriptives.addAll(StatementUtils.findSpecificDescendants(Relations.NMOD, primary, graph));
        remaining.addAll(otherDescriptives);
    }
}
