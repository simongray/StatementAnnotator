package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Utilities for Statement and related classes.
 */
public class StatementUtils {
    private static final Logger logger = LoggerFactory.getLogger(StatementUtils.class);

    /**
     * Joins a list of IndexedWords together in the correct order without putting spaces before commas.
     *
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
     * Remove certain words from a list of words.
     *
     * @param undesired words to remove
     * @param allWords words to remove from
     * @return remaining words
     */
    public static Set<IndexedWord> without(Set<IndexedWord> undesired, Set<IndexedWord> allWords) {
        Set<IndexedWord> remainingWords = new HashSet<>(allWords);
        remainingWords.removeIf(indexedWord -> undesired.contains(indexedWord));
        return remainingWords;
    }

    /**
     * Whether or not a word token is shortened.
     *
     * @param word the word to check
     * @return shortened or not
     */
    private static boolean isShortened(String word) {
        return word.startsWith("'") || word.equals("n't");
    }

    /**
     * Recursively finds the components of a compound (ex: subject, object, etc.).
     *
     * @param parent the word that serves as an entry point
     * @param graph the graph of the sentence
     * @param ignoredRelations relation types that shouldn't be followed or included
     * @return compound components
     */
    public static Set<IndexedWord> findCompoundComponents(IndexedWord parent, SemanticGraph graph, Set<String> ignoredRelations) {
        if (ignoredRelations == null) ignoredRelations = new HashSet<>();
        Set<IndexedWord> compoundComponents = new HashSet<>();
        compoundComponents.add(parent);
        logger.info("added " + parent + " as part of compound");

        for (IndexedWord child : graph.getChildren(parent)) {
            GrammaticalRelation relation = graph.reln(parent, child);
            if (!ignoredRelations.contains(relation.getShortName())) {
                compoundComponents.addAll(findCompoundComponents(child, graph, ignoredRelations));
            } else {
                logger.info("ignoring " + child + " with relation " + relation.getShortName() + " (ignored relations: " + ignoredRelations + ")");
            }
        }

        return compoundComponents;
    }

    /**
     * Recursively finds specific descendants of a word.
     *
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
     * Create a relations map for a specific compound part.
     * Useful for isolating words such as negations, markers, copulas, etc.
     * Also useful for separating conjunctions.
     *
     * @param words the entries to find relations for
     * @param relation the relation type
     * @param graph the graph to search in
     * @return relations map
     */
    public static Map<IndexedWord, Set<IndexedWord>> makeRelationsMap(Set<IndexedWord> words, String relation, SemanticGraph graph) {
        Map<IndexedWord, Set<IndexedWord>> relationsMap = new HashMap<>();
        Set<IndexedWord> allChildren = new HashSet<>();

        // map children to parents
        for (IndexedWord word : words) {
            Set<IndexedWord> children = StatementUtils.findSpecificDescendants(relation, word, graph);
            relationsMap.put(word, children);
            allChildren.addAll(children);
        }

        // children cannot also be parents in this simple relations map
        for (IndexedWord child : allChildren) {
            relationsMap.remove(child);
        }

        return relationsMap;
    }

    /**
     * Finds out the negation status based on a set of negations.
     *
     * @param negations
     * @return
     */
    public static boolean isNegated(Set<IndexedWord> negations) {
        return negations.size() % 2 != 0;
    }

    /**
     * Reduce a variable amount of Resemblance objects to the lowest common denominator.
     *
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
     *
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
     *
     * @param statements statements to match
     * @return search keywords
     */
    public static Set<String> getSearchKeywords(Set<Statement> statements) {
        return null;  // TODO: implement, apply some filter like stopwords to total words and return words that survive
    }

    /**
     * Print statements of a sentence in a pretty way.
     *
     * @param statements statements from a sentence.
     */
    public static void printStatements(Set<Statement> statements) {
        printStatements(statements, "");
    }

    /**
     * Helper method for printStatements(CoreMap sentence).
     *
     * @param statements statements from a sentence.
     */
    private static void printStatements(Set<Statement> statements, String indent) {
        if (statements != null) {
            int i = 0;
            for (Statement statement : statements) {
                System.out.println(indent + "  |_ statement: " + statement);

                for (StatementComponent component : statement.getComponents()) {
                    String prefix = i < statements.size() - 1? "  |  |_ " : "     |_ ";
                    if (component instanceof Statement) {
                        System.out.println(indent + prefix + "component: " + ((Statement) component).getComponents());  // TODO: make pretty with indent and stuff
                    } else {
                        System.out.println(indent + prefix + "component: " + component);
                    }
                }

                i++;
            }
        } else {
            System.out.println("  |_ NO STATEMENTS");
        }
    }

    /**
     * Test whether two sets intersect.
     *
     * @param set1 set 1
     * @param set2 set 2
     * @return
     */
    public static <T> boolean intersects(Set<T> set1, Set<T> set2) {
        for (T component : set1) {
            if (set2.contains(component)) {
                return true;
            }
        }

        return false;
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
