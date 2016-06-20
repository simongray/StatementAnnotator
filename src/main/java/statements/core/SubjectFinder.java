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
public class SubjectFinder {
    private static final Logger logger = LoggerFactory.getLogger(SubjectFinder.class);

    /**
     * The subjects that are found in a sentence.
     *
     * @param graph the dependency graph of a sentence
     * @return subjects
     */
    public static Set<Subject> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Set<IndexedWord> simpleSubjects = new HashSet<>();
        Set<IndexedWord> simplePassiveSubjects = new HashSet<>();
        Map<IndexedWord, Set<IndexedWord>> subjectMapping = new HashMap<>();
        Map<IndexedWord, Set<IndexedWord>> passiveSubjectMapping = new HashMap<>();
        Set<Subject> subjects = new HashSet<>();

        // find simple subjects from relations
        for (TypedDependency dependency : dependencies) {
            if (dependency.reln().getShortName().equals(Relations.NSUBJ)) {
                simpleSubjects.add(dependency.dep());
            }
            if (dependency.reln().getShortName().equals(Relations.NSUBJPASS)) {
                simplePassiveSubjects.add(dependency.dep());
            }
        }

        // create normal subject mapping based on relations
        subjectMapping = StatementUtils.makeRelationsMap(simpleSubjects, Relations.CONJ, graph);

        // create passive subject mapping based on relations
        passiveSubjectMapping = StatementUtils.makeRelationsMap(simplePassiveSubjects, Relations.CONJ, graph);

        // build complete subjects from mappings
        for (IndexedWord primarySubject : subjectMapping.keySet()) {
            subjects.add(new Subject(primarySubject, subjectMapping.get(primarySubject), graph));
        }
        for (IndexedWord primarySubject : passiveSubjectMapping.keySet()) {
            subjects.add(new Subject(primarySubject, passiveSubjectMapping.get(primarySubject), graph));
        }

        logger.info("subjects found: " + subjects);

        return subjects;
    }
}
