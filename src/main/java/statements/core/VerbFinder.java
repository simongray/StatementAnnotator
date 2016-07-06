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
    private static final Set<String> INCOMING_RELATIONS = new HashSet<>();
    static {
        OUTGOING_RELATIONS.add(Relations.NSUBJ);
        OUTGOING_RELATIONS.add(Relations.NSUBJPASS);
        OUTGOING_RELATIONS.add(Relations.DOBJ);

        INCOMING_RELATIONS.add(Relations.XCOMP);
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
        Set<IndexedWord> aclVerbs = new HashSet<>();  // for verbs that are used to describe nouns
        Set<Verb> verbs = new HashSet<>();

        Set<IndexedWord> ignoredWords = getIgnoredWords(graph);
        logger.info("ignored words: " + ignoredWords);

        // find candidate verbs and adjectives from relations
        for (TypedDependency dependency : dependencies) {
            if (OUTGOING_RELATIONS.contains(dependency.reln().getShortName())) {
                if (!ignoredWords.contains(dependency.dep())) simpleVerbs.add(dependency.gov());
            }
            if (INCOMING_RELATIONS.contains(dependency.reln().getShortName())) {
                if (!ignoredWords.contains(dependency.gov())) simpleVerbs.add(dependency.dep());
            }

            // find verbs acting as subjects in a sentence through a clause
            if (dependency.reln().getShortName().equals(Relations.CSUBJ)) {
                if (!ignoredWords.contains(dependency.gov())) csubjVerbs.add(dependency.dep());  // TODO: contains(gov or dep)?
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

        // remove verbs that act as subjects
        simpleVerbs.removeAll(csubjVerbs); // TODO: safe to remove?

        // remove verbs that are used to describe nouns
        simpleVerbs.removeAll(aclVerbs);  // TODO: safe to remove?

        // add the cop relation verbs in
        simpleVerbs.addAll(copVerbs);

        for (IndexedWord simpleVerb : simpleVerbs) {
            verbs.add(new Verb(simpleVerb, graph));
        }

        // take care to label this type of verb
        // this is done in order to be able to label statements, i.e. a csubj verb statement
        // knowing a statement is a csubj verb statement, means it can be treated as a replacement for Subject
        for (IndexedWord csubjVerb : csubjVerbs) {
            verbs.add(new Verb(csubjVerb, graph, Labels.CSUBJVERB));
        }
        logger.info("verbs found: " + verbs);

        return verbs;
    }
}
