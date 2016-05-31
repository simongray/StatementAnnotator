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
    private static final String NSUBJ_RELATION = "nsubj";
    private static final String NSUBJPASS_RELATION = "nsubjpass";  // for passive voice

    /**
     * The subjects that are found in a sentence.
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
            if (dependency.reln().getShortName().equals(NSUBJ_RELATION)) {
                simpleSubjects.add(dependency.dep());
            }
            if (dependency.reln().getShortName().equals(NSUBJPASS_RELATION)) {
                simplePassiveSubjects.add(dependency.dep());
            }
        }

        // create normal subject mapping based on relations
        for (IndexedWord simpleSubject : simpleSubjects) {
            IndexedWord parent = graph.getParent(simpleSubject);
            if (simpleSubjects.contains(parent)) {
                Set<IndexedWord> secondarySubjects = subjectMapping.getOrDefault(parent, new HashSet<>());
                secondarySubjects.add(simpleSubject);
                subjectMapping.put(parent, secondarySubjects);
            } else {
                subjectMapping.putIfAbsent(simpleSubject, new HashSet<>());
            }
        }

        // create passive subject mapping based on relations
        for (IndexedWord simplePassiveSubject : simplePassiveSubjects) {
            IndexedWord parent = graph.getParent(simplePassiveSubject);
            if (simplePassiveSubjects.contains(parent)) {
                Set<IndexedWord> secondarySubjects = passiveSubjectMapping.getOrDefault(parent, new HashSet<>());
                secondarySubjects.add(simplePassiveSubject);
                passiveSubjectMapping.put(parent, secondarySubjects);
            } else {
                passiveSubjectMapping.putIfAbsent(simplePassiveSubject, new HashSet<>());
            }
        }

        // build complete subjects from mappings
        for (IndexedWord primarySubject : subjectMapping.keySet()) {
            subjects.add(new Subject(primarySubject, subjectMapping.get(primarySubject), graph));
        }
        for (IndexedWord primarySubject : passiveSubjectMapping.keySet()) {
            subjects.add(new Subject(primarySubject, passiveSubjectMapping.get(primarySubject), graph));
        }

        return subjects;
    }
}
