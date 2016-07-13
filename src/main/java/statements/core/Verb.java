package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Set;

/**
 * The complete primary of a natural language statement.
 */
public class Verb extends AbstractComponent implements Resembling<Verb> {

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
     * The resemblance of this object to another object.
     *
     * @param otherObject subject to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(Verb otherObject) {
        return null;  // TODO
    }
}
