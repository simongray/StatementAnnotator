package semantic;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;

/**
 * Annotation for the subject(s) of a sentence.
 */
public class SubjectAnnotation implements CoreAnnotation<StatementSubject> {
    public Class<StatementSubject> getType() {
        return ErasureUtils.<Class<StatementSubject>>uncheckedCast(StatementSubject.class);
    }
}
