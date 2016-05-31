package statements.annotations;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import statements.core.Statement;

import java.util.Set;

/**
 * Annotation for the statements of a sentence.
 */
public class StatementsAnnotation implements CoreAnnotation<Set<Statement>> {
    public Class<Set<Statement>> getType() {
        return ErasureUtils.<Class<Set<Statement>>>uncheckedCast(Statement.class);
    }
}
