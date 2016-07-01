package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Set;

/**
 * The complete indirect object of a natural language statement.
 */
public class IndirectObject extends AbstractComponent implements Resembling<IndirectObject> {

    public IndirectObject(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        super(primary, secondary, graph);
    }

    /**
     * Describes which relations are ignored when producing the complete indirect object.
     */
    protected Set<String> getIgnoredRelations() {
        return Relations.IGNORED_INDIRECT_OBJECT_RELATIONS;
    }

    /**
     * Describes which relations are ignored when producing compound indirect objects.
     */
    protected Set<String> getIgnoredCompoundRelations() {
        return Relations.IGNORED_INDIRECT_OBJECT_COMPOUND_RELATIONS;
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
