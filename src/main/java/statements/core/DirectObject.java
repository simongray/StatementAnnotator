package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * The complete direct object of a natural language statement.
 */
public class DirectObject extends AbstractComponent {
    Set<IndexedWord> nmod = new HashSet<>();

    public DirectObject(IndexedWord primary, SemanticGraph graph) {
        this(primary, graph, false);
    }

    public DirectObject(IndexedWord primary, SemanticGraph graph, boolean copObject) {
        super(primary, graph);

        // in case this object was constructed from a COP relation, it needs to ignore certain relations
        // Note: important to also remove negations from $remaining and $all, otherwise components will overlap!
        if (copObject) {
            remaining.removeAll(negations);
            all.removeAll(negations);
            negations.clear();
        }

        // nmod relations from nouns are typically descriptive in nature
        otherDescriptives.addAll(StatementUtils.findSpecificDescendants(Relations.NMOD, primary, graph));
        remaining.addAll(otherDescriptives);
    }

    /**
     * Allow components to define what makes them interesting.
     *
     * @return true if interesting
     */
    @Override
    public boolean isInteresting() {
        if (Lexicon.uninterestingNouns.contains(getBasicCompound())) return false;
        if (isLocal()) return false;

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
