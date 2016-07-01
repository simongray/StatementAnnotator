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
     * Include certain words with a list of words.
     *
     * @param desired words to remove
     * @param allWords words to remove from
     * @return remaining words
     */
    public static Set<IndexedWord> with(Set<IndexedWord> desired, Set<IndexedWord> allWords) {
        Set<IndexedWord> words = new HashSet<>(allWords);
        words.addAll(desired);
        return words;
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
     * Recursively finds the words of a compound in a greedy way.
     *
     * @param parent the word that serves as an entry point
     * @param graph the graph of the sentence
     * @param ignoredRelations relation types that shouldn't be followed or included
     * @return compound components
     */
    public static Set<IndexedWord> findCompound(IndexedWord parent, SemanticGraph graph, Set<String> ignoredRelations, Set<String> ownedScopes) {
        Set<IndexedWord> compoundComponents = new HashSet<>();
        compoundComponents.add(parent);

        for (IndexedWord child : graph.getChildren(parent)) {
            GrammaticalRelation relation = graph.reln(parent, child);

            // when encountering an owned scope, then that scope is added in full
            // in other cases, relations are added when they do not appear in the set of ignoredRelations
            if (ignoredRelations == null || !ignoredRelations.contains(relation.getShortName())) {
                if (ownedScopes != null && ownedScopes.contains(relation.getShortName())) {
                    compoundComponents.addAll(findCompound(child, graph));
                } else {
                    compoundComponents.addAll(findCompound(child, graph, null, ignoredRelations));
                }
            }
        }

        return compoundComponents;
    }

    /**
     * Recursively finds the words of a compound in a greedy way.
     *
     * @param parent the word that serves as an entry point
     * @param graph the graph of the sentence
     * @return compound components
     */
    public static Set<IndexedWord> findCompound(IndexedWord parent, SemanticGraph graph) {
        return findCompound(parent, graph, null, null);
    }

    /**
     * Recursively finds the words of a compound in a NON-greedy way.
     * Only relations in ownedScopes will be followed.
     * Useful for defining smaller (= not full) compounds used for later comparison.
     *
     * @param parent the word that serves as an entry point
     * @param graph the graph of the sentence
     * @param ownedScopes relations + descendants of that relation that will be added regardless of ignored relations
     * @return compound components
     */
    public static Set<IndexedWord> findLimitedCompound(IndexedWord parent, SemanticGraph graph, Set<String> ownedScopes) {
        Set<IndexedWord> compoundComponents = new HashSet<>();
        compoundComponents.add(parent);

        for (IndexedWord child : graph.getChildren(parent)) {
            GrammaticalRelation relation = graph.reln(parent, child);

            // when encountering an owned scope, then that scope is added in full
            // in other cases, relations are added when they do not appear in the set of ignoredRelations
            if (ownedScopes != null && ownedScopes.contains(relation.getShortName())) {
                compoundComponents.addAll(findLimitedCompound(child, graph, ownedScopes));
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
                specificDescendants.addAll(findCompound(child, graph));
            }
        }

        return specificDescendants;
    }

    /**
     * Recursively finds specific children of a word.
     * This differs from findSpecificDescendants() in that it only adds direct children.
     * It is useful for finding entries for a component, e.g. all of the subjects in a conjunction.
     *
     * @param word the word that serves as an entry point
     * @param graph the graph of the sentence
     * @return specific descendants
     */
    public static Set<IndexedWord> findSpecificChildren(String relation, IndexedWord word, SemanticGraph graph) {
        Set<IndexedWord> specificChildren = new HashSet<>();

        for (IndexedWord child : graph.getChildren(word)) {
            if (graph.reln(word, child).getShortName().equals(relation)) {
                specificChildren.add(child);
            }
        }

        return specificChildren;
    }

    /**
     * Create a map of parent-descendants (recursively found) based on a specific type of relation.
     * Useful for isolating words such as negations, markers, copulas, etc.
     *
     * @param words the entries to find relations for
     * @param relation the relation type
     * @param graph the graph to search in
     * @return relations map
     */
    public static Map<IndexedWord, Set<IndexedWord>> makeDescendantMap(Collection<IndexedWord> words, String relation, SemanticGraph graph) {
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
     * Create a map of direct parent-children relations based on a specific type of relation.
     * Useful for finding conjunctions for subjects or objects (but not verbs, see: findJointlyGoverned).
     *
     * @param words the entries to find relations for
     * @param relation the relation type
     * @param graph the graph to search in
     * @return relations map
     */
    public static Map<IndexedWord, Set<IndexedWord>> makeChildMap(Collection<IndexedWord> words, String relation, SemanticGraph graph) {
        Map<IndexedWord, Set<IndexedWord>> relationsMap = new HashMap<>();
        Set<IndexedWord> allChildren = new HashSet<>();

        // map children to parents
        for (IndexedWord word : words) {
            Set<IndexedWord> children = StatementUtils.findSpecificChildren(relation, word, graph);
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
     * Create sets of words that are linked by a common governor (= parent) in a specific relation.
     * Useful for finding conjunct verbs, for example, using the nsubj relation.
     *
     * @param words
     * @param relation
     * @param graph
     * @return
     */
    public static Set<Set<IndexedWord>> findJointlyGoverned(Set<IndexedWord> words, String relation, SemanticGraph graph) {
        Map<IndexedWord, Set<IndexedWord>> childMapping = new HashMap<>();

        // retrieve the relevant child nodes based on the relation
        // store word as child of each parent in child-parents mapping
        for (IndexedWord word : words) {
            Set<IndexedWord> specificChildren = findSpecificDescendants(relation, word, graph);
            logger.info("specific children for " + word + " with relation " + relation + ": " + specificChildren);

            for (IndexedWord child : specificChildren) {
                Set<IndexedWord> parents = childMapping.getOrDefault(child, new HashSet<>());
                parents.add(word);
                childMapping.put(child, parents);
            }
        }

        logger.info("child mapping: " + childMapping);

        Set<Set<IndexedWord>> jointlyGoverned = new HashSet<>();
        for (Set<IndexedWord> jointlyGovernedSet : childMapping.values()) {
            jointlyGoverned.add(jointlyGovernedSet);
        }

        // make sure that each word is also separately represented as a set
        // if they are jointly governed, then they will be merged anyway
        // this ensures that all words survive the merge process
        for (IndexedWord word : words) {
            Set<IndexedWord> singleWordSet = new HashSet<>();
            singleWordSet.add(word);
            jointlyGoverned.add(singleWordSet);
        }

        // merge the words together into sets when they share parents
        merge(jointlyGoverned);

        return jointlyGoverned;
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
     * Merge sets that intersect.
     * Useful for merging component or verb sets, for example.
     *
     * @param objectSets
     * @return
     */
    public static <T> void merge(Collection<Set<T>> objectSets) {
        Collection<Set<T>> mergedObjectSets = new HashSet<>();

        for (Set<T> objectSet : objectSets) {
            Set<T> mergedObjectSet = new HashSet<>(objectSet);

            for (Set<T> otherObjectSet : objectSets) {
                if (!mergedObjectSet.equals(otherObjectSet) && intersects(mergedObjectSet, otherObjectSet)) {
                    logger.info("merging " + otherObjectSet + " into " + mergedObjectSet);
                    mergedObjectSet.addAll(otherObjectSet);
                }
            }

            mergedObjectSets.add(mergedObjectSet);  // re-adding identical merged sets has no effect
        }

        objectSets.clear();
        objectSets.addAll(mergedObjectSets);
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
