package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

/**
 * Allows for linking of Subject, Verb, DirectObject, and IndirectObject.
 */
public interface StatementComponent {
    IndexedWord getPrimary();
}
