package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Allows for linking of Subject, Verb, DirectObject, and IndirectObject.
 */
public interface StatementComponent {
    Set<IndexedWord> getCompound();
    Set<IndexedWord> getRemaining();  // TODO: rename and find a stronger purpose than just getting clauses into the statement text
    Set<IndexedWord> getAll();
    Set<IndexedWord> getGovernors();
    Set<IndexedWord> getEmbeddingGovernors();  // TODO: rename, sounds like crap
    Set<String> getLabels();
    boolean contains(StatementComponent otherComponent);  // TODO: do or do not?
    int getLowestIndex();
    int getHighestIndex();
    boolean matches(StatementComponent otherComponent);

    /**
     * Is this component the parent of another component?
     * It will evaluate to false in case part of the other component
     *
     * @param otherComponent the other component
     * @return true if parent of
     */
    default boolean parentOf(StatementComponent otherComponent) {
        return StatementUtils.intersects(getCompound(), otherComponent.getGovernors()) && !StatementUtils.intersects(getCompound(), otherComponent.getCompound());
    }

    /**
     * Is this component the parent of another component?
     * It will evaluate to false in case part of the other component
     *
     * @param otherComponent the other component
     * @return true if parent of
     */
    default boolean embeddingParentOf(StatementComponent otherComponent) {
        return StatementUtils.intersects(getCompound(), otherComponent.getEmbeddingGovernors()) && !StatementUtils.intersects(getCompound(), otherComponent.getCompound());
    }

    /**
     * Count the gaps between all of the component words (compound + remaining).
     * It is useful for determining if a component is likely to be malformed.
     *
     * @return number of gaps
     */
    default int gaps() {
        int highestIndex = getHighestIndex();
        int lowestIndex = getLowestIndex();

        // populate an array so that words are true and missing words at some index are false
        // this is done to find the gaps (= missing word at some index between the highest and lowest index)
        boolean[] filledIndexes = new boolean[highestIndex - lowestIndex + 1];
        // TODO: sometimes this randomly fails
        try {
            for (IndexedWord word : getAll()) {
                filledIndexes[word.index() - lowestIndex] = true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.error("error finding gaps! lowestIndex= " + lowestIndex + ", highestIndex=" + highestIndex + ", getAll()=" + getAll(), e);
        }
        // count the gaps in the resulting array
        // note: multi-word gaps in a row count as a single gap, not as multiple gaps!
        int gaps = 0;
        boolean inGap = false;
        for (boolean filledIndex : filledIndexes) {
            if (!filledIndex) {
                if (!inGap) {
                    inGap = true;
                    gaps++;
                }
            } else {
                inGap = false;
            }
        }

        return gaps;
    }
}
