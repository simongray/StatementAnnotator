package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.Set;

/**
 * Allows for linking of Subject, Verb, DirectObject, and IndirectObject.
 */
public interface StatementComponent {
    Set<IndexedWord> getComplete();
    boolean connectedTo(StatementComponent otherComponent);
}
