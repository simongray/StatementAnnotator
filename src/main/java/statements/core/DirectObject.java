package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Set;

/**
 * The complete direct object of a natural language statement.
 */
public class DirectObject extends AbstractComponent implements Resembling<DirectObject> {
    public DirectObject(IndexedWord primary, SemanticGraph graph) {
        super(primary, graph);
    }

    /**
     * Describes which relations are ignored when producing the complete direct object.
     */
    protected Set<String> getIgnoredRelations() {
        return Relations.IGNORED_DIRECT_OBJECT_RELATIONS;
    }

    /**
     * The resemblance of another direct object to this direct object.
     *
     * @param otherObject direct object to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(DirectObject otherObject) {
        return null;  // TODO
    }
}
