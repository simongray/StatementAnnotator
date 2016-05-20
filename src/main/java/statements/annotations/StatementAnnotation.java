package statements.annotations;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import statements.core.Statement;

/**
 * Annotation for the statements of a sentence.
 */
public class StatementAnnotation implements CoreAnnotation<Statement> {
    public Class<Statement> getType() {
        return ErasureUtils.<Class<Statement>>uncheckedCast(Statement.class);
    }
}
