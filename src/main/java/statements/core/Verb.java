package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Set;

/**
 * The complete primary of a natural language statement.
 */
public class Verb extends AbstractComponent {

    protected final Set<IndexedWord> aux;

    public Verb(IndexedWord primary, SemanticGraph graph, Set<String> labels) {
        super(primary, graph, labels);

        // AUX can be both directly connected to be verbs and to governing words in COP relation
        aux = StatementUtils.findSpecificChildren(Relations.AUX, primary, graph);
        Set<IndexedWord> copGovernors = StatementUtils.findSpecificParents(Relations.COP, primary, graph);
        for (IndexedWord copGovernor : copGovernors) {
            aux.addAll(StatementUtils.findSpecificChildren(Relations.AUX, copGovernor, graph));
        }

        compound.addAll(aux);
    }

    /**
     * Allow components to define what makes them interesting.
     *
     * @return true if interesting
     */
    @Override
    public boolean isInteresting() {
        return true;
    }

    /**
     * Allow components to define what makes them well formed.
     *
     * @return true if well formed
     */
    @Override
    public boolean isWellFormed() {
        return gaps() == 0;
    }
}
