package statements;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
            Collection<TypedDependency> dependencies = graph.typedDependencies();

            // TODO: remove, for DEBUGGING
//            graph.prettyPrint();
//            System.out.println(graph);
//            logger.info("sentence with deps: " + dependencies);
            StatementFinder.find(sentence);

//
//            // find all dependants in nsubj relations and attach SubjectAnnotations to tokens based on them
//            for (TypedDependency dependency : dependencies) {
//                if (dependency.reln().getShortName().equals("nsubj")) {
//                    IndexedWord dependent = dependency.dep();
//                    CoreLabel subjectToken = tokens.get(dependent.index() - 1);  // since CoreNLP index starts at 1
////                    CompleteSubject subject = new CompleteSubject(dependent, graph);
////                    subjectToken.set(SubjectAnnotation.class, subject);
//                    logger.info("set subject to: " + subject);
//                }
//            }
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
