package nlp;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Find the subjects in sentences and annotates them.
 */
public class SubjectObjectAnnotator implements Annotator {
    public final static String SUBJECT = "subjectobject";
    final Logger logger = LoggerFactory.getLogger(SubjectObjectAnnotator.class);

    /**
     * This constructor allows for the annotator to accept different properties to alter its behaviour.
     *
     * It doesn't seem to be documented anywhere, but a method in AnnotatorImplementations.java with signature
     *      public Annotator custom(Properties properties, String property) { ... }
     * allows for various constructor signatures to be implemented for a custom annotator.
     * @param properties
     */
    public SubjectObjectAnnotator(String name, Properties properties) {
        String prefix = (name != null && !name.isEmpty())? name + ".":"";
    }

    @Override
    public void annotate(Annotation annotation)  {
    }

    @Override
    public Set<Requirement> requirementsSatisfied() {
        Set<Requirement> requirementsSatisfied = new HashSet<>();
        requirementsSatisfied.add(new Requirement(SUBJECT));
        return requirementsSatisfied;
    }

    @Override
    public Set<Requirement> requires() {
        Set<Requirement> requirements = new HashSet<>();
        // TODO: find out why it fails when requirements are set
//        requirements.add(new Requirement(Annotator.STANFORD_PARSE));
        return requirements;
    }
}
