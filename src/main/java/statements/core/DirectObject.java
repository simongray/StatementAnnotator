package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import statements.core.exceptions.MissingEntryException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The complete direct object of a natural language statement.
 */
public class DirectObject extends AbstractComponent implements Resembling<DirectObject> {
    public DirectObject(IndexedWord primary, SemanticGraph graph) {
        super(primary, null, graph);
    }

    /**
     * Describes which relations are ignored when producing the complete direct object.
     */
    protected Set<String> getIgnoredRelations() {
        return Relations.IGNORED_DIRECT_OBJECT_RELATIONS;
    }

    /**
     * Describes which relations are ignored when producing compound direct objects.
     */
    protected Set<String> getIgnoredCompoundRelations() {
        return Relations.IGNORED_DIRECT_OBJECT_COMPOUND_RELATIONS;
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
