package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * Allows for linking of Subject, Verb, DirectObject, and IndirectObject.
 */
public interface StatementComponent {
    Set<IndexedWord> getCompound();
    Set<IndexedWord> getRemaining();  // TODO: rename and find a stronger purpose than just getting clauses into the statement text
    Set<IndexedWord> getGovernors();
    String getLabel();

    /**
     * Is this component the parent of another component?
     *
     * @param otherComponent the other component
     * @return true if parent
     */
    default boolean parentOf(StatementComponent otherComponent) {
        Logger logger = LoggerFactory.getLogger(StatementComponent.class);
        return StatementUtils.intersects(getCompound(), otherComponent.getGovernors());
    }
}
