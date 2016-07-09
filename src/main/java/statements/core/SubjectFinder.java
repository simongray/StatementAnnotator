package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Finds subjects in sentences.
 */
public class SubjectFinder extends AbstractFinder<Subject> {
    private static final Logger logger = LoggerFactory.getLogger(SubjectFinder.class);

    private Set<IndexedWord> nsubjSubjects;
    private Set<IndexedWord> nsubjpassSubjects;
    private Set<Subject> subjects;

    @Override
    protected void init() {
        nsubjSubjects = new HashSet<>();
        nsubjpassSubjects = new HashSet<>();
        subjects = new HashSet<>();

        logger.info("ignored words: " + ignoredWords);
    }

    @Override
    protected void check(TypedDependency dependency) {
        addDependent(nsubjSubjects, dependency, Relations.NSUBJ);
        addDependent(nsubjpassSubjects, dependency, Relations.NSUBJPASS);
    }

    @Override
    protected Set<Subject> get() {
        for (IndexedWord nsubjSubject : nsubjSubjects) {
            subjects.add(new Subject(nsubjSubject, graph));
        }
        for (IndexedWord nsubjpassSubject : nsubjpassSubjects) {
            subjects.add(new Subject(nsubjpassSubject, graph));
        }

        logger.info("subjects found: " + subjects);

        return subjects;
    }
}
