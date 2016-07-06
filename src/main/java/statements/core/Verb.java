package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Set;

/**
 * The complete primary of a natural language statement.
 */
public class Verb extends AbstractComponent implements Resembling<Verb> {

    public Verb(IndexedWord primary, SemanticGraph graph) {
        super(primary, graph);

        // TODO: remove?
        words.addAll(complete);  // needed since verb conjunctions are different from other components
    }

    /**
     * Describes which relations are ignored when producing the complete verb.
     */
    protected Set<String> getIgnoredRelations() {
        return Relations.IGNORED_VERB_RELATIONS;
    }

    /**
     * The string of the complete verb.
     *
     * @return the longest string possible
     */
    public String getString() {
        return StatementUtils.join(StatementUtils.without(getMarkers(), complete));
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
