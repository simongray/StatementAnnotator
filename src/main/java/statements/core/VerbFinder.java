package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Finds verbs in sentences.
 */
public class VerbFinder extends AbstractFinder<Verb> {
    private static final Logger logger = LoggerFactory.getLogger(VerbFinder.class);

    private final Set<String> OUTGOING_RELATIONS;

    private Set<IndexedWord> dobjVerbs;
    private Set<IndexedWord> copVerbs;
    private Set<IndexedWord> adjectives;
    private Set<IndexedWord> csubjVerbs;  // act as subjects
    private Set<IndexedWord> xcompVerbs;
    private Set<IndexedWord> aclVerbs;  // for verbs that are used to describe nouns
    private Set<Verb> verbs;

    public VerbFinder() {
        OUTGOING_RELATIONS = new HashSet<>();
        OUTGOING_RELATIONS.add(Relations.NSUBJ);
        OUTGOING_RELATIONS.add(Relations.NSUBJPASS);
        OUTGOING_RELATIONS.add(Relations.DOBJ);
    }

    @Override
    protected void init() {
        dobjVerbs = new HashSet<>();
        copVerbs = new HashSet<>();
        adjectives = new HashSet<>();
        csubjVerbs = new HashSet<>();
        xcompVerbs = new HashSet<>();
        aclVerbs = new HashSet<>();
        verbs = new HashSet<>();

        logger.info("ignored words: " + ignoredWords);
    }

    @Override
    protected void check(TypedDependency dependency) {
        if (OUTGOING_RELATIONS.contains(dependency.reln().getShortName())) {
            if (!ignoredWords.contains(dependency.dep())) dobjVerbs.add(dependency.gov());
        }

        // find verbs acting as subjects in a sentence through a clause
        addDependent(csubjVerbs, dependency, Relations.CSUBJ);

        // find verbs acting as direct objects in a sentence through a clause
        addDependent(xcompVerbs, dependency, Relations.XCOMP);

        // TODO: safe to remove?
        addDependent(aclVerbs, dependency, Relations.ACL);

        // make sure that adjectives are removed from the list of verbs
        // and find cop relation verbs (is, be, 's, 'm, etc.) in the same relation
        addDependent(copVerbs, dependency, Relations.COP);
        addGovernor(adjectives, dependency, Relations.COP);
    }

    @Override
    protected Set<Verb> get() {
        // remove adjectives from candidate verbs
        dobjVerbs.removeAll(adjectives);

        // remove verbs that are already in xcompverbs
        dobjVerbs.removeAll(xcompVerbs);

        // remove verbs that act as subjects
        // these are added later with the correct label
        dobjVerbs.removeAll(csubjVerbs);

        // remove verbs that are used to describe nouns
        dobjVerbs.removeAll(aclVerbs);  // TODO: is this needed anymore?

        for (IndexedWord dobjVerb : dobjVerbs) {
            logger.info("added new verb from dobj relation: " + dobjVerb);
            verbs.add(new Verb(dobjVerb, graph, getLabels(dobjVerb)));
        }
        for (IndexedWord copVerb : copVerbs) {
            logger.info("added new verb from cop relation: " + copVerb);
            verbs.add(new Verb(copVerb, graph, getLabels(copVerb, Labels.COP_VERB)));
        }
        for (IndexedWord csubjVerb : csubjVerbs) {
            // knowing a statement is a csubj verb statement, means it can be treated as a replacement for Subject
            logger.info("added new verb from csubj relation: " + csubjVerb);
            verbs.add(new Verb(csubjVerb, graph, getLabels(csubjVerb, Labels.CSUBJ_VERB)));
        }
        for (IndexedWord xcompVerb : xcompVerbs) {
            logger.info("added new verb from xcomp relation: " + xcompVerb);
            verbs.add(new Verb(xcompVerb, graph, getLabels(xcompVerb, Labels.XCOMP_VERB)));
        }
        logger.info("verbs found: " + verbs);

        return verbs;
    }
}
