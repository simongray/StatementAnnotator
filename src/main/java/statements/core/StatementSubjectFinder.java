package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This class handles the construction of subjects.
 */
public class StatementSubjectFinder {
    private static final Logger logger = LoggerFactory.getLogger(StatementSubjectFinder.class);
    private static final String NSUBJ_RELATION = "nsubj";

    /**
     * Returns the StatementSubjects that are found in a sentence.
     * @param graph the dependency graph of a sentence
     * @return
     */
    public static Set<StatementSubject> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Set<IndexedWord> simpleSubjects = new HashSet<>();
        Map<IndexedWord, Set<IndexedWord>> subjectMapping = new HashMap<>();
        Set<StatementSubject> subjects = new HashSet<>();

        // find simple subjects from relations
        for (TypedDependency dependency : dependencies) {
            if (dependency.reln().getShortName().equals(NSUBJ_RELATION)) {
                simpleSubjects.add(dependency.dep());
            }
        }

        // create mapping based on relations
        for (IndexedWord simpleSubject : simpleSubjects) {
            IndexedWord parent = graph.getParent(simpleSubject);
            if (simpleSubjects.contains(parent)) {
                Set<IndexedWord> secondarySubjects = subjectMapping.getOrDefault(parent, new HashSet<>());
                secondarySubjects.add(simpleSubject);
                subjectMapping.put(parent, secondarySubjects);
            }
        }

        // build complete subjects from mapping
        for (IndexedWord primarySubject : subjectMapping.keySet()) {
            subjects.add(new StatementSubject(primarySubject, subjectMapping.get(primarySubject), graph));
        }

        return subjects;
    }
}
