package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.Set;

/**
 * Allows for linking of Subject, Verb, DirectObject, and IndirectObject.
 */
public interface StatementComponent {
    Set<IndexedWord> getComplete();
    Set<IndexedWord> getOutgoing();

    /**
     * Is this component connected to another component?
     *
     * @param otherComponent the other component
     * @return true if connected
     */
    default boolean connectedTo(StatementComponent otherComponent) {
        if (this.equals(otherComponent)) {
            return false;
        } else {
            return StatementUtils.intersects(getComplete(), otherComponent.getOutgoing()) || StatementUtils.intersects(getOutgoing(), otherComponent.getComplete());
        }
    }
}
