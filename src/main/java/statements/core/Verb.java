package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Set;

/**
 * The complete verb of a natural language statement.
 */
public class Verb extends AbstractComponent {

    protected final Set<IndexedWord> aux;

    public Verb(IndexedWord head, SemanticGraph graph, Set<String> labels) {
        super(head, graph, labels);

        // AUX can be both directly connected to be verbs and to governing words in COP relation
        aux = StatementUtils.findSpecificChildren(Relations.AUX, head, graph);

        if (isCopula()) {
            Set<IndexedWord> copGovernors = StatementUtils.findSpecificParents(Relations.COP, head, graph);
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

    public Set<IndexedWord> getAuxiliaryVerbs() {
        return aux;
    }

    /**
     * Whether or not this verb is a copula (= some form of "to be").
     *
     * @return true if verb is copula
     */
    public boolean isCopula() {
        return labels.contains(Labels.COP_VERB);
    }
}
