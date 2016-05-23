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
                if (i != 0 && !tag.equals(",")  && !shortened) {
                    buffer.append(" ");
                }

                if (i == 0 && word.equals("n't")) {
                    buffer.append("not");  // special rule
                } else {
                    buffer.append(word);  // default
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
     * @param parent the word that serves as an entry point
     * @param graph the graph of the sentence
     * @param ignoredRelations relation types that shouldn't be followed or included
     * @return compound components
     */
    public static Set<IndexedWord> findCompoundComponents(IndexedWord parent, SemanticGraph graph, Set<String> ignoredRelations) {
        if (ignoredRelations == null) ignoredRelations = new HashSet<>();
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
     * Recursively finds specific descendants of a word.
     * @param word the word that serves as an entry point
     * @param graph the graph of the sentence
     * @return specific descendants
     */
    public static Set<IndexedWord> findSpecificDescendants(String relation, IndexedWord word, SemanticGraph graph) {
        Set<IndexedWord> specificDescendants = new HashSet<>();

        for (IndexedWord child : graph.getChildren(word)) {
            if (graph.reln(word, child).getShortName().equals(relation)) {
                specificDescendants.add(child);
            }
        }

        return specificDescendants;
    }

    /**
     * Finds out the negation status based on a set of negations.
     * @param negations
     * @return
     */
    public static boolean isNegated(Set<IndexedWord> negations) {
        return negations.size() % 2 != 0;
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
     * The haystack of statements partitioned into levels of resemblance to the needle statement.
     * This useful for finding agreements between statements.
     * @param needle statement that must be resembled
     * @param haystack statements to partition into levels of resemblance
     * @return map of resemblance to statements
     */
    public static Map<Resemblance, Set<Statement>> findResemblances(Statement needle, Set<Statement> haystack) {
        Map<Resemblance, Set<Statement>> partitions = new HashMap<>();

        for (Statement statement : haystack) {
            Resemblance resemblance = statement.resemble(needle);
            Set<Statement> partition = partitions.getOrDefault(resemblance, new HashSet<>());
            partition.add(statement);
            partitions.put(resemblance, partition);
        }

        return partitions;
    }

    /**
     * Get relevant text search keywords for finding resemblance to statements in text.
     * The keywords are useful for finding pieces of text that *may* contain resembling statements.
     * These pieces of text will then have to be put through the StatementAnnotator,
     * in order to get a set of statements for comparing to the original statements.
     * @param statements statements to match
     * @return search keywords
     */
    public static Set<String> getSearchKeywords(Set<Statement> statements) {
        return null;  // TODO: implement, apply some filter like stopwords to total words and return words that survive
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
