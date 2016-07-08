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
public class VerbFinder extends AbstractFinder<Verb> {
    private static final Logger logger = LoggerFactory.getLogger(VerbFinder.class);

    private static final Set<String> OUTGOING_RELATIONS = new HashSet<>();
    static {
        OUTGOING_RELATIONS.add(Relations.NSUBJ);
        OUTGOING_RELATIONS.add(Relations.NSUBJPASS);
        OUTGOING_RELATIONS.add(Relations.DOBJ);
    }

    /**
     * The verbs that are found in a sentence.
     *
     * @param graph the dependency graph of a sentence
     * @return verbs
     */
    @Override
    public Set<Verb> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Set<IndexedWord> simpleVerbs = new HashSet<>();
        Set<IndexedWord> copVerbs = new HashSet<>();
        Set<IndexedWord> adjectives = new HashSet<>();
        Set<IndexedWord> csubjVerbs = new HashSet<>();  // for verbs that act as subjects
        Set<IndexedWord> xcompVerbs = new HashSet<>();  // for verbs that act as subjects
        Set<IndexedWord> aclVerbs = new HashSet<>();  // for verbs that are used to describe nouns
        Map<IndexedWord, IndexedWord> conjunctions = new HashMap<>();  // dep-to-gov
        Set<Verb> verbs = new HashSet<>();

        Set<IndexedWord> ignoredWords = getIgnoredWords(graph);
        logger.info("ignored words: " + ignoredWords);

        // find candidate verbs and adjectives from relations
        for (TypedDependency dependency : dependencies) {
            if (OUTGOING_RELATIONS.contains(dependency.reln().getShortName())) {
                if (!ignoredWords.contains(dependency.dep())) simpleVerbs.add(dependency.gov());
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
            if (dependency.reln().getShortName().equals(Relations.CONJ)) {
                if (!ignoredWords.contains(dependency.gov())) {
                    conjunctions.put(dependency.dep(), dependency.gov());
                }
            }

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

        // remove adjectives from candidate verbs
        simpleVerbs.removeAll(adjectives);

        // remove verbs that are already in xcompverbs
        simpleVerbs.removeAll(xcompVerbs);

        // remove verbs that act as subjects
        simpleVerbs.removeAll(csubjVerbs); // TODO: safe to remove?

        // remove verbs that are used to describe nouns
        simpleVerbs.removeAll(aclVerbs);  // TODO: safe to remove?

        // add the cop relation verbs in
        simpleVerbs.addAll(copVerbs);

        for (IndexedWord simpleVerb : simpleVerbs) {
            Set<String> labels = new HashSet<>();

            if (conjunctions.keySet().contains(simpleVerb)) {
                labels.add(Labels.CONJCHILD);
            } else if (conjunctions.values().contains(simpleVerb)) {
                labels.add(Labels.CONJPARENT);
            }

            verbs.add(new Verb(simpleVerb, graph, labels));
        }

        // take care to label this type of verb
        // this is done in order to be able to label statements, i.e. a csubj verb statement
        // knowing a statement is a csubj verb statement, means it can be treated as a replacement for Subject
        for (IndexedWord csubjVerb : csubjVerbs) {
            Set<String> labels = new HashSet<>();
            labels.add(Labels.CSUBJVERB);

            if (conjunctions.keySet().contains(csubjVerb)) {
                labels.add(Labels.CONJCHILD);
            } else if (conjunctions.values().contains(csubjVerb)) {
                labels.add(Labels.CONJPARENT);
            }

            verbs.add(new Verb(csubjVerb, graph, labels));
        }

        // take care to label this type of verb
        // this is done in order to be able to label statements, i.e. a csubj verb statement
        // knowing a statement is a csubj verb statement, means it can be treated as a replacement for Subject
        for (IndexedWord xcompVerb : xcompVerbs) {
            Set<String> labels = new HashSet<>();
            labels.add(Labels.XCOMPVERB);

            if (conjunctions.keySet().contains(xcompVerb)) {
                labels.add(Labels.CONJCHILD);
            } else if (conjunctions.values().contains(xcompVerb)) {
                labels.add(Labels.CONJPARENT);
            }

            verbs.add(new Verb(xcompVerb, graph, labels));
        }
        logger.info("verbs found: " + verbs);
        logger.info("conjunction found: " + conjunctions);

        return verbs;
    }
}
