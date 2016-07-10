package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Set;

/**
 * The complete indirect object of a natural language statement.
 */
public class IndirectObject extends AbstractComponent implements Resembling<IndirectObject> {

    public IndirectObject(IndexedWord primary, SemanticGraph graph, Set<String> labels) {
        super(primary, graph, labels);
    }

    /**
     * Describes which relations are ignored when producing the complete indirect object.
     */
    protected Set<String> getIgnoredRelations() {
        return Relations.IGNORED_INDIRECT_OBJECT_RELATIONS;
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
