package statements.core;

import java.util.HashSet;
import java.util.Set;

public class SpecialWords {
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

    public static final Set<String> SPECIFIC_DETERMINERS = new HashSet<>();
    static {
        // definite article
        SPECIFIC_DETERMINERS.add("the");

        // possessives
        SPECIFIC_DETERMINERS.addAll(POSSESSIVES);

        // demonstratives
        SPECIFIC_DETERMINERS.addAll(DEMONSTRATIVES);

        // interrogatives
        SPECIFIC_DETERMINERS.add("which");
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

    public static Set<String> uninterestingNouns = new HashSet<>();
    static {
        uninterestingNouns.add("it");
        uninterestingNouns.add("they");
        uninterestingNouns.add("them");
        uninterestingNouns.add("this");
        uninterestingNouns.add("that");
        uninterestingNouns.add("he");
        uninterestingNouns.add("she");
        uninterestingNouns.add("her");
        uninterestingNouns.add("him");
        uninterestingNouns.add("you");
        uninterestingNouns.add("here");
        uninterestingNouns.add("there");
        uninterestingNouns.add("who");
        uninterestingNouns.add("what");
        uninterestingNouns.add("which");
        uninterestingNouns.add("those");
        uninterestingNouns.add("all");
        uninterestingNouns.add("thing");
        uninterestingNouns.add("one");
        uninterestingNouns.add("some");
        uninterestingNouns.add("someone");
    }
}
