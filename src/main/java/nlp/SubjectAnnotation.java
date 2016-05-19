package nlp;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;

/**
 * Annotation for the subject(s) of a sentence.
 */
public class SubjectAnnotation implements CoreAnnotation<Subject> {
    public Class<Subject> getType() {
        return ErasureUtils.<Class<Subject>>uncheckedCast(Subject.class);
    }
}
