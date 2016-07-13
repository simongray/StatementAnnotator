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
 7.	JJ	Adjective
 8.	JJR	Adjective, comparative
 9.	JJS	Adjective, superlative
 10.	LS	List item marker
 11.	MD	Modal
 12.	NN	Noun, singular or mass
 13.	NNS	Noun, plural
 14.	NNP	Proper noun, singular
 15.	NNPS	Proper noun, plural
 16.	PDT	Predeterminer
 17.	POS	Possessive ending
 18.	PRP	Personal pronoun
 19.	PRP$	Possessive pronoun
 20.	RB	Adverb
 21.	RBR	Adverb, comparative
 22.	RBS	Adverb, superlative
 23.	RP	Particle
 24.	SYM	Symbol
 25.	TO	to
 26.	UH	Interjection

        [verbs taken out]

 33.	WDT	Wh-determiner
 34.	WP	Wh-pronoun
 35.	WP$	Possessive wh-pronoun
 36.	WRB	Wh-adverb
 */
public class Tags {
    public static final String VB = "VB";  // Verb, base form
    public static final String VBD = "VBD";  // Verb, past tense
    public static final String VBG = "VBG";  // VBG	Verb, gerund or present participle
    public static final String VBN = "VBN";  // Verb, past participle
    public static final String VBP = "VBP";  // Verb, non-3rd person singular present
    public static final String VBZ = "VBZ";  // Verb, 3rd person singular present

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

    /**
     * Reduce a set of words based on their part-of-speech tags.
     *
     * @param words the words to reduce
     * @param allowedtags the tags present after reducing
     * @return reduced words
     */
    public static Set<IndexedWord> reduce(Set<IndexedWord> words, Set<String> allowedtags) {
        Set<IndexedWord> badWords = new HashSet<>();
        Set<IndexedWord> reducedWords = new HashSet<>(words);

        for (IndexedWord word : words) {
            if (!allowedtags.contains(word.tag())) badWords.add(word);
        }

        reducedWords.removeAll(badWords);

        return reducedWords;
    }
}
