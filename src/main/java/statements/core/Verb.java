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

        if (isCopula()) {
            Set<IndexedWord> copGovernors = StatementUtils.findSpecificParents(Relations.COP, primary, graph);
            for (IndexedWord copGovernor : copGovernors) {
                aux.addAll(StatementUtils.findSpecificChildren(Relations.AUX, copGovernor, graph));

                // negations are also wrongly applied in case of COP verbs so they need to be added
                // (conversely, negations are removed from the DirectObject made from the COP relation)
                negations.addAll(StatementUtils.findSpecificChildren(Relations.NEG, copGovernor, graph));
                remaining.addAll(negations);
                all.addAll(negations);

            }
        }

        compound.addAll(aux);
        all.addAll(aux);
    }

    /**
     * Whether or not this verb is a copula (= some form of "to be").
     *
     * @return true if verb is copula
     */
    public boolean isCopula() {
        return labels.contains(Labels.COP_VERB);
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
