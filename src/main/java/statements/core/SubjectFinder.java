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
        Set<IndexedWord> simpleClausalSubjects = new HashSet<>();
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
            if (dependency.reln().getShortName().equals(Relations.CSUBJ)) {
                if (!ignoredWords.contains(dependency.dep())) simpleClausalSubjects.add(dependency.dep());
            }
        }

        // create normal subject mapping based on relations
        Map<IndexedWord, Set<IndexedWord>> subjectMapping = StatementUtils.makeChildMap(simpleSubjects, Relations.CONJ, graph);

        // create passive subject mapping based on relations
        Map<IndexedWord, Set<IndexedWord>> passiveSubjectMapping = StatementUtils.makeChildMap(simplePassiveSubjects, Relations.CONJ, graph);

        // create clausal subject mapping based on relations
        Map<IndexedWord, Set<IndexedWord>> clausalSubjectMapping = StatementUtils.makeChildMap(simpleClausalSubjects, Relations.CONJ, graph);

        // build complete subjects from mappings
        for (IndexedWord primarySubject : subjectMapping.keySet()) {
            subjects.add(new Subject(primarySubject, subjectMapping.get(primarySubject), graph));
        }
        for (IndexedWord primarySubject : passiveSubjectMapping.keySet()) {
            subjects.add(new Subject(primarySubject, passiveSubjectMapping.get(primarySubject), graph));
        }
        for (IndexedWord primarySubject : clausalSubjectMapping.keySet()) {
            subjects.add(new Subject(primarySubject, passiveSubjectMapping.get(primarySubject), graph));
        }

        logger.info("subjects found: " + subjects);

        return subjects;
    }
}
