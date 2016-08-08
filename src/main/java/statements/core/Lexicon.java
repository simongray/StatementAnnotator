package statements.core;

import java.util.HashSet;
import java.util.Set;

public class Lexicon {
    // ref: https://learnenglish.britishcouncil.org/en/english-grammar/determiners-and-quantifiers
    public static final Set<String> POSSESSIVES = new HashSet<>();
    static {
        // possessives
        POSSESSIVES.add("my");
        POSSESSIVES.add("your");
        POSSESSIVES.add("his");
        POSSESSIVES.add("her");
        POSSESSIVES.add("its");
        POSSESSIVES.add("our");
        POSSESSIVES.add("their");
        POSSESSIVES.add("whose");
    }

    public static final Set<String> DEMONSTRATIVES = new HashSet<>();
    static {
        // demonstratives
        DEMONSTRATIVES.add("this");
        DEMONSTRATIVES.add("that");
        DEMONSTRATIVES.add("these");
        DEMONSTRATIVES.add("those");
    }

    /**
     * Local Determiners is a neologism of mine. For the purpose of sorting through statements
     * specific determiners such as "this" or "my" are categorically different from "the".
     * Anything local to a user's situation is not useful for comparison "my x" or "those ys"
     * while "the x" can both refer to a specific instance or something more general.
     */
    public static final Set<String> LOCAL_DETERMINERS = new HashSet<>();
    {
        // possessives
        LOCAL_DETERMINERS.addAll(POSSESSIVES);

        // demonstratives
        LOCAL_DETERMINERS.addAll(DEMONSTRATIVES);
    }

    public static final Set<String> SPECIFIC_DETERMINERS = new HashSet<>();
    static {
        // definite article
        SPECIFIC_DETERMINERS.add("the");

        // interrogatives
        LOCAL_DETERMINERS.add("which");

        // other specific determiners
        SPECIFIC_DETERMINERS.addAll(LOCAL_DETERMINERS);
    }

    public static final Set<String> GENERAL_DETERMINERS = new HashSet<>();
    static {
        GENERAL_DETERMINERS.add("a");
        GENERAL_DETERMINERS.add("an");
        GENERAL_DETERMINERS.add("any");
        GENERAL_DETERMINERS.add("another");
        GENERAL_DETERMINERS.add("other");
        GENERAL_DETERMINERS.add("what");
    }

    public static final Set<String> FIRST_PERSON = new HashSet<>();
    static {
        FIRST_PERSON.add("i");
        FIRST_PERSON.add("me");
        FIRST_PERSON.add("we");
        FIRST_PERSON.add("us");
    }

    public static final Set<String> SECOND_PERSON = new HashSet<>();
    static {
        SECOND_PERSON.add("you");
        SECOND_PERSON.add("y'all");
        SECOND_PERSON.add("yall");
    }

    public static final Set<String> PLURAL_NON_NOUNS = new HashSet<>();
    static {
        PLURAL_NON_NOUNS.add("we");
        PLURAL_NON_NOUNS.add("us");
        PLURAL_NON_NOUNS.add("they");
        PLURAL_NON_NOUNS.add("them");
        PLURAL_NON_NOUNS.add("these");
        PLURAL_NON_NOUNS.add("those");
    }

    /**
     * This set of nouns comprises second and third person pronouns, but not first person.
     * It also adds other words that do not carry any information without context.
     */
    public static Set<String> uninterestingNouns = new HashSet<>();
    static {
        uninterestingNouns.add("you");
        uninterestingNouns.add("he");
        uninterestingNouns.add("she");
        uninterestingNouns.add("it");
        uninterestingNouns.add("they");
        uninterestingNouns.add("him");
        uninterestingNouns.add("her");
        uninterestingNouns.add("them");
        uninterestingNouns.add("this");
        uninterestingNouns.add("that");
        uninterestingNouns.add("these");
        uninterestingNouns.add("those");
        uninterestingNouns.add("here");
        uninterestingNouns.add("there");
        uninterestingNouns.add("who");
        uninterestingNouns.add("what");
        uninterestingNouns.add("which");
        uninterestingNouns.add("all");
        uninterestingNouns.add("thing");
        uninterestingNouns.add("one");
        uninterestingNouns.add("some");
        uninterestingNouns.add("someone");
    }
}
