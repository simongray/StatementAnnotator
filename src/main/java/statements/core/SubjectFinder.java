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
    private Set<IndexedWord> nsubjSubjects;
    private Set<IndexedWord> nsubjpassSubjects;
    private Set<Subject> subjects;

    @Override
    protected void init() {
        nsubjSubjects = new HashSet<>();
        nsubjpassSubjects = new HashSet<>();
        subjects = new HashSet<>();
    }

    @Override
    protected void check(TypedDependency dependency) {
        addDependent(nsubjSubjects, dependency, Relations.NSUBJ);
        addDependent(nsubjpassSubjects, dependency, Relations.NSUBJPASS);
    }

    @Override
    protected Set<Subject> get() {
        // remove duplicates (this happens sometimes, e.g. "It's once a week,  3 hours and is run by two psychologists.")
        nsubjSubjects.removeAll(nsubjpassSubjects);

        for (IndexedWord nsubjSubject : nsubjSubjects) {
            subjects.add(new Subject(nsubjSubject, graph));
        }
        for (IndexedWord nsubjpassSubject : nsubjpassSubjects) {
            subjects.add(new Subject(nsubjpassSubject, graph));
        }

        return subjects;
    }
}
