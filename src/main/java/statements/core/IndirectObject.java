package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * The complete indirect object of a natural language statement.
 */
public class IndirectObject extends AbstractComponent implements Resembling<IndirectObject> {

    public IndirectObject(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        super(primary, secondary, graph);
    }

    /**
     * The resemblance of another indirect object to this indirect object.
     * @param otherObject object to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(IndirectObject otherObject) {
        return null;  // TODO
    }

    /**
     * The identifier for this component.
     *
     * @return identifier
     */
    @Override
    protected String getIdentifier() {
        return "IO";
    }
}
