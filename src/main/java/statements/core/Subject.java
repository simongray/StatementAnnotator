package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;


/**
 * The complete subject of a natural language statement.
 */
public class Subject extends AbstractComponent {
    Set<IndexedWord> nmod = new HashSet<>();

    public Subject(IndexedWord primary, SemanticGraph graph) {
        super(primary, graph);

        nmod = StatementUtils.findSpecificDescendants(Relations.NMOD, primary, graph);

        // TODO: put in separate place
        compound.addAll(nmod);
    }

    /**
     * Allow components to define what makes them interesting.
     *
     * @return true if interesting
     */
    @Override
    public boolean isInteresting() {
        if (SpecialWords.uninterestingNouns.contains(getBasicCompound())) {
            return false;
        }

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
