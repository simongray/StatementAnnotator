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

    private Set<IndexedWord> simpleSubjects;
    private Set<IndexedWord> simplePassiveSubjects;
    private Set<Subject> subjects;

    @Override
    protected void init() {
        simpleSubjects = new HashSet<>();
        simplePassiveSubjects = new HashSet<>();
        subjects = new HashSet<>();

        logger.info("ignored words: " + ignoredWords);
    }

    @Override
    protected void check(TypedDependency dependency) {
        if (dependency.reln().getShortName().equals(Relations.NSUBJ)) {
            if (!ignoredWords.contains(dependency.dep())) simpleSubjects.add(dependency.dep());
        }
        if (dependency.reln().getShortName().equals(Relations.NSUBJPASS)) {
            if (!ignoredWords.contains(dependency.dep())) simplePassiveSubjects.add(dependency.dep());
        }
    }

    @Override
    protected Set<Subject> get() {
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
