package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.HashSet;
import java.util.Set;

/**
 * using as source: https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
 *
 *
 Number
 Tag
 Description
 1.	CC	Coordinating conjunction
 2.	CD	Cardinal number
 3.	DT	Determiner
 4.	EX	Existential there
 5.	FW	Foreign word
 6.	IN	Preposition or subordinating conjunction

 10.	LS	List item marker
 11.	MD	Modal

 16.	PDT	Predeterminer
 17.	POS	Possessive ending

 23.	RP	Particle
 24.	SYM	Symbol
 25.	TO	to

 33.	WDT	Wh-determiner

 */
public class PartsOfSpeech {
    // verbs
    public static final String VB = "VB";  // Verb, base form
    public static final String VBD = "VBD";  // Verb, past tense
    public static final String VBG = "VBG";  // VBG	Verb, gerund or present participle
    public static final String VBN = "VBN";  // Verb, past participle
    public static final String VBP = "VBP";  // Verb, non-3rd person singular present
    public static final String VBZ = "VBZ";  // Verb, 3rd person singular present

    // nouns
    public static final String NN = "NN";  // Noun, singular or mass
    public static final String NNP = "NNP";  // Proper noun, singular
    public static final String NNS = "NNS";  // Noun, plural
    public static final String NNPS = "NNPS";  // Proper noun, plural

    // adjectives
    public static final String JJ = "JJ";  // Adjective
    public static final String JJR = "JJR";  // Adjective, comparative
    public static final String JJS = "JJS";  // Adjective, superlative

    // adverbs
    public static final String RB = "RB";  // Adverb
    public static final String RBR = "RBR";  // Adverb, comparative
    public static final String RBS = "RBS";  // Adverb, superlative
    public static final String WRB = "WRB";  // Wh-adverb

    // pronouns
    public static final String PRP = "PRP";  // Personal pronoun
    public static final String PRP$ = "PRP$";  // Possessive pronoun
    public static final String WP = "WP";  // Wh-pronoun
    public static final String WP$ = "WP$";  // Possessive wh-pronoun

    // other
    public static final String UH = "UH";  // interjections

    /**
     * Outgoing relations which are stored internally in any AbstractComponent, but not shown/used by default.
     *
     * An example is the NEG relation, which is separated out from the other words,
     * but used to adjust results when doing comparisons.
     */
    public static final Set<String> VERBS = new HashSet<>();
    static {
        VERBS.add(VB);
        VERBS.add(VBD);
        VERBS.add(VBG);
        VERBS.add(VBN);
        VERBS.add(VBP);
        VERBS.add(VBZ);
    }

    public static final Set<String> NOUNS = new HashSet<>();
    static {
        NOUNS.add(NN);
        NOUNS.add(NNP);
        NOUNS.add(NNS);
        NOUNS.add(NNPS);
    }

    public static final Set<String> ADJECTIVES = new HashSet<>();
    static {
        ADJECTIVES.add(JJ);
        ADJECTIVES.add(JJR);
        ADJECTIVES.add(JJS);
    }

    public static final Set<String> ADVERBS = new HashSet<>();
    static {
        ADVERBS.add(RB);
        ADVERBS.add(RBR);
        ADVERBS.add(RBS);
        ADVERBS.add(WRB);
    }

    public static final Set<String> PRONOUNS = new HashSet<>();
    static {
        PRONOUNS.add(PRP);
        PRONOUNS.add(PRP$);
        PRONOUNS.add(WP);
        PRONOUNS.add(WP$);
    }

    public static final Set<String> PLURAL = new HashSet<>();
    static {
        PLURAL.add(NNS);
        PLURAL.add(NNPS);
    }

    public static final Set<String> INTERJECTIONS = new HashSet<>();
    static {
        INTERJECTIONS.add(UH);
    }

    public static final Set<String> LEXICAL_WORDS = new HashSet<>();
    static {
        LEXICAL_WORDS.addAll(VERBS);
        LEXICAL_WORDS.addAll(NOUNS);
        LEXICAL_WORDS.addAll(ADJECTIVES);
        LEXICAL_WORDS.addAll(ADVERBS);
    }

    /**
     * Reduce a set of words based on their part-of-speech tags.
     *
     * @param words the words to reduce
     * @param allowedtags the tags present after reducing
     * @return reduced words
     */
    public static Set<IndexedWord> reduceToAllowedTags(Set<IndexedWord> words, Set<String> allowedtags) {
        Set<IndexedWord> badWords = new HashSet<>();
        Set<IndexedWord> reducedWords = new HashSet<>(words);

        for (IndexedWord word : words) {
            if (!allowedtags.contains(word.tag())) badWords.add(word);
        }

        reducedWords.removeAll(badWords);

        return reducedWords;
    }
}
