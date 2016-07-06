package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Finds subjects in sentences.
 */
public class SubjectFinder extends AbstractFinder<Subject> {
    private static final Logger logger = LoggerFactory.getLogger(SubjectFinder.class);

    /**
     * The subjects that are found in a sentence.
     *
     * @param graph the dependency graph of a sentence
     * @return subjects
     */
    @Override
    public Set<Subject> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Set<IndexedWord> simpleSubjects = new HashSet<>();
        Set<IndexedWord> simplePassiveSubjects = new HashSet<>();
        Set<Subject> subjects = new HashSet<>();

        Set<IndexedWord> ignoredWords = getIgnoredWords(graph);
        logger.info("ignored words: " + ignoredWords);

        // find simple subjects from relations
        for (TypedDependency dependency : dependencies) {
            if (dependency.reln().getShortName().equals(Relations.NSUBJ)) {
                if (!ignoredWords.contains(dependency.dep())) simpleSubjects.add(dependency.dep());
            }
            if (dependency.reln().getShortName().equals(Relations.NSUBJPASS)) {
                if (!ignoredWords.contains(dependency.dep())) simplePassiveSubjects.add(dependency.dep());
            }
        }

        Set<IndexedWord> entries = new HashSet<>();
        entries.addAll(simpleSubjects);
        entries.addAll(simplePassiveSubjects);

        // build complete subjects
        for (IndexedWord entry : entries) {
            subjects.add(new Subject(entry, graph));
        }

        logger.info("subjects found: " + subjects);

        return subjects;
    }
}
