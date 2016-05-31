package statements;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import statements.annotations.StatementsAnnotation;
import statements.core.Statement;
import statements.core.StatementFinder;

import java.util.*;

/**
 * Annotates sentences with statements as represented by the Statement class.
 */
public class StatementAnnotator implements Annotator {
    public final static String STATEMENT = "statement";
    final Logger logger = LoggerFactory.getLogger(StatementAnnotator.class);

    /**
     * This constructor allows for the annotator to accept different properties to alter its behaviour.
     * It doesn't seem to be documented anywhere, but a method in AnnotatorImplementations.java with signature
     *      public Annotator custom(Properties properties, String property) { ... }
     * allows for various constructor signatures to be implemented for a custom annotator.
     * @param properties
     */
    public StatementAnnotator(String name, Properties properties) {
        String prefix = (name != null && !name.isEmpty())? name + ".":"";
    }

    @Override
    public void annotate(Annotation annotation)  {
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            logger.info("checking sentence for statements: " + sentence);
            Set<Statement> statements = StatementFinder.find(sentence);
            if (statements != null && statements.size() > 0) {
                logger.info("statements found: " + statements);
                sentence.set(StatementsAnnotation.class, statements);
            }
        }
    }

    @Override
    public Set<Requirement> requirementsSatisfied() {
        Set<Requirement> requirementsSatisfied = new HashSet<>();
        requirementsSatisfied.add(new Requirement(STATEMENT));
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
