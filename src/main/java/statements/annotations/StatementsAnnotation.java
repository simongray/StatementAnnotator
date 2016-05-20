package statements.annotations;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;
import statements.core.Statement;

import java.util.List;

/**
 * Annotation for the statements of a sentence.
 */
public class StatementsAnnotation implements CoreAnnotation<List<Statement>> {
    public Class<List<Statement>> getType() {
        return ErasureUtils.<Class<List<Statement>>>uncheckedCast(Statement.class);
    }
}
