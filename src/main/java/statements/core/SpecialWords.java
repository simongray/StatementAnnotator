package statements.core;

import java.util.HashSet;
import java.util.Set;

public class SpecialWords {
    // ref: https://learnenglish.britishcouncil.org/en/english-grammar/determiners-and-quantifiers
    public static final Set<String> SPECIFIC_DETERMINERS = new HashSet<>();
    static {
        // definite article
        SPECIFIC_DETERMINERS.add("the");

        // possessives
        SPECIFIC_DETERMINERS.add("my");
        SPECIFIC_DETERMINERS.add("your");
        SPECIFIC_DETERMINERS.add("his");
        SPECIFIC_DETERMINERS.add("her");
        SPECIFIC_DETERMINERS.add("its");
        SPECIFIC_DETERMINERS.add("our");
        SPECIFIC_DETERMINERS.add("their");
        SPECIFIC_DETERMINERS.add("whose");

        // demonstratives
        SPECIFIC_DETERMINERS.add("this");
        SPECIFIC_DETERMINERS.add("that");
        SPECIFIC_DETERMINERS.add("these");
        SPECIFIC_DETERMINERS.add("those");

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

}
