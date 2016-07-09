package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Finds verbs in sentences.
 */
public class VerbFinder extends AbstractFinder2<Verb> {
    private static final Logger logger = LoggerFactory.getLogger(VerbFinder.class);

    Set<IndexedWord> dobjVerbs;
    Set<IndexedWord> copVerbs;
    Set<IndexedWord> adjectives;
    Set<IndexedWord> csubjVerbs;  // for verbs that act as subjects
    Set<IndexedWord> xcompVerbs;  // for verbs that act as subjects
    Set<IndexedWord> aclVerbs;// for verbs that are used to describe nouns
    Set<Verb> verbs;

    private static final Set<String> OUTGOING_RELATIONS = new HashSet<>();
    static {
        OUTGOING_RELATIONS.add(Relations.NSUBJ);
        OUTGOING_RELATIONS.add(Relations.NSUBJPASS);
        OUTGOING_RELATIONS.add(Relations.DOBJ);
    }

    @Override
    protected void init(SemanticGraph graph) {
        super.init(graph);  // always start with call to super class method

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
        super.check(dependency);  // always start with call to super class method

        if (OUTGOING_RELATIONS.contains(dependency.reln().getShortName())) {
            if (!ignoredWords.contains(dependency.dep())) dobjVerbs.add(dependency.gov());
        }

        // find verbs acting as subjects in a sentence through a clause
        if (dependency.reln().getShortName().equals(Relations.CSUBJ)) {
            if (!ignoredWords.contains(dependency.gov())) csubjVerbs.add(dependency.dep());  // TODO: contains(gov or dep)?
        }

        // find verbs acting as direct objects in a sentence through a clause
        if (dependency.reln().getShortName().equals(Relations.XCOMP)) {
            if (!ignoredWords.contains(dependency.gov())) xcompVerbs.add(dependency.dep());  // TODO: contains(gov or dep)?
        }

        // find conjunction
        findConjunctions(dependency);

        // make sure that adjectives are removed from the list of verbs
        // in the very same move, cop relation verbs (is, be, 's, 'm, etc.) are found
        if (dependency.reln().getShortName().equals(Relations.COP)) {
            if (!ignoredWords.contains(dependency.dep())) {
                adjectives.add(dependency.gov());
                copVerbs.add(dependency.dep());
            }
        }

        // TODO: safe to remove?
        if (dependency.reln().getShortName().equals(Relations.ACL)) {
            if (!ignoredWords.contains(dependency.dep())) aclVerbs.add(dependency.dep());
        }
    }

    @Override
    protected Set<Verb> get(SemanticGraph graph) {
        // remove adjectives from candidate verbs
        dobjVerbs.removeAll(adjectives);

        // remove verbs that are already in xcompverbs
        dobjVerbs.removeAll(xcompVerbs);

        // remove verbs that act as subjects
        // these are added later with the correct label
        dobjVerbs.removeAll(csubjVerbs);

        // remove verbs that are used to describe nouns
        dobjVerbs.removeAll(aclVerbs);  // TODO: safe to remove?

        for (IndexedWord simpleVerb : dobjVerbs) {
            Set<String> labels = new HashSet<>();

            if (conjunctions.keySet().contains(simpleVerb)) {
                labels.add(Labels.CONJ_CHILD_VERB);
            } else if (conjunctions.values().contains(simpleVerb)) {
                labels.add(Labels.CONJ_PARENT_VERB);
            }

            verbs.add(new Verb(simpleVerb, graph, labels));
        }

        // take care to label this type of verb
        // this is done in order to be able to label statements, i.e. a csubj verb statement
        // knowing a statement is a csubj verb statement, means it can be treated as a replacement for Subject
        for (IndexedWord copVerb : copVerbs) {
            Set<String> labels = new HashSet<>();
            labels.add(Labels.COP_VERB);

            if (conjunctions.keySet().contains(copVerb)) {
                labels.add(Labels.CONJ_CHILD_VERB);
            } else if (conjunctions.values().contains(copVerb)) {
                labels.add(Labels.CONJ_PARENT_VERB);
            }

            verbs.add(new Verb(copVerb, graph, labels));
        }

        // take care to label this type of verb
        // this is done in order to be able to label statements, i.e. a csubj verb statement
        // knowing a statement is a csubj verb statement, means it can be treated as a replacement for Subject
        for (IndexedWord csubjVerb : csubjVerbs) {
            Set<String> labels = new HashSet<>();
            labels.add(Labels.CSUBJ_VERB);

            if (conjunctions.keySet().contains(csubjVerb)) {
                labels.add(Labels.CONJ_CHILD_VERB);
            } else if (conjunctions.values().contains(csubjVerb)) {
                labels.add(Labels.CONJ_PARENT_VERB);
            }

            verbs.add(new Verb(csubjVerb, graph, labels));
        }

        // take care to label this type of verb
        // this is done in order to be able to label statements, i.e. a csubj verb statement
        // knowing a statement is a csubj verb statement, means it can be treated as a replacement for Subject
        for (IndexedWord xcompVerb : xcompVerbs) {
            Set<String> labels = new HashSet<>();
            labels.add(Labels.XCOMP_VERB);

            if (conjunctions.keySet().contains(xcompVerb)) {
                labels.add(Labels.CONJ_CHILD_VERB);
            } else if (conjunctions.values().contains(xcompVerb)) {
                labels.add(Labels.CONJ_PARENT_VERB);
            }

            verbs.add(new Verb(xcompVerb, graph, labels));
        }
        logger.info("verbs found: " + verbs);
        logger.info("conjunction found: " + conjunctions);

        return verbs;
    }
}
