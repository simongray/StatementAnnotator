package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;

import java.util.*;

/**
 * Utilities for Statement and related classes.
 */
public class StatementUtils {
    /**
     * Joins a list of IndexedWords together in the correct order without putting spaces before commas.
     * @param words the list of words to be joined
     * @return the string representing the words
     */
    public static String join(Set<IndexedWord> words) {
        List<IndexedWord> wordsList = new ArrayList<>(words);
        wordsList.sort(new IndexComparator());

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < wordsList.size(); i++) {
            IndexedWord indexedWord = wordsList.get(i);
            String tag = indexedWord.tag();
            String word = indexedWord.word();
            boolean shortened = isShortened(word);

            if (!tag.equals(".")) {
                if (i != 0 && !tag.equals(",")  && !shortened) buffer.append(" ");
                if (i == 0 && shortened) {
                    buffer.append(indexedWord.lemma());  // TODO: improve, sort of a hack now ("'s" becomes "be")
                } else {
                    buffer.append(indexedWord.word());
                }
            }
        }

        return buffer.toString();
    }

    /**
     * Whether or not a word token is shortened.
     * @param word the word to check
     * @return shortened or not
     */
    private static boolean isShortened(String word) {
        return word.startsWith("'") || word.equals("n't");
    }

    /**
     * Recursively finds the components of a compound (ex: subject, object, etc.).
     * @param parent the simple subject that serves as an entry point
     * @param graph the graph of the sentence
     * @param ignoredRelations relation types that shouldn't be followed or included
     * @return compound components
     */
    public static Set<IndexedWord> findCompoundComponents(IndexedWord parent, SemanticGraph graph, Set<String> ignoredRelations) {
        Set<IndexedWord> compoundComponents = new HashSet<>();
        compoundComponents.add(parent);

        for (IndexedWord child : graph.getChildren(parent)) {
            GrammaticalRelation relation = graph.reln(parent, child);
            if (!ignoredRelations.contains(relation.getShortName())) {
                compoundComponents.addAll(findCompoundComponents(child, graph, ignoredRelations));
            }
        }

        return compoundComponents;
    }

    /**
     * Finds out the negation status of a verb.
     * @param simpleVerb the verb to examine
     * @param graph the graph of the sentence
     * @return true if negated
     */
    public static boolean isNegated(IndexedWord simpleVerb, SemanticGraph graph) {
        Set<IndexedWord> children = graph.getChildren(simpleVerb);
        int negations = 0;

        for (IndexedWord child : children) {
            if (graph.reln(simpleVerb, child).getShortName().equals("neg")) {
                negations++;
            }
        }

        return negations % 2 != 0;
    }

    /**
     * Reduce a variable amount of Resemblance objects to the lowest common denominator.
     * @param resemblances range of Resemblance to reduce
     * @return common denominator
     */
    public static Resemblance reduce(Resemblance... resemblances) {
        Resemblance lowestCommonDenominator = Resemblance.FULL;  // default

        for (Resemblance resemblance : resemblances) {
            if (resemblance == Resemblance.NONE) {
                lowestCommonDenominator = resemblance; break;
            } else if (resemblance == Resemblance.SLIGHT) {
                lowestCommonDenominator = resemblance;
            } else if (resemblance == Resemblance.CLOSE) {
                lowestCommonDenominator = resemblance;
            }
        }

        return lowestCommonDenominator;
    }

    /**
     * Used to sort IndexedWords by index.
     */
    public static class IndexComparator implements Comparator<IndexedWord> {
        @Override
        public int compare(IndexedWord x, IndexedWord y) {
            int xn = x.index();
            int yn = y.index();
            if (xn == yn) {
                return 0;
            } else {
                if (xn > yn) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }
}
