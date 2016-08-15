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
    static {
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

    public static final Set<String> FIRST_PERSON_POSSESSIVES = new HashSet<>();
    public static final Set<String> FIRST_PERSON_POSSESSIVE_NOUNS = new HashSet<>();
    static {
        FIRST_PERSON_POSSESSIVES.add("my");
        FIRST_PERSON_POSSESSIVES.add("our");

        FIRST_PERSON_POSSESSIVE_NOUNS.add("mine");
        FIRST_PERSON_POSSESSIVE_NOUNS.add("ours");
    }

    public static final Set<String> SECOND_PERSON_POSSESSIVES = new HashSet<>();
    public static final Set<String> SECOND_PERSON_POSSESSIVE_NOUNS = new HashSet<>();
    static {
        SECOND_PERSON_POSSESSIVES.add("your");

        SECOND_PERSON_POSSESSIVE_NOUNS.add("yours");
    }

    public static final Set<String> THIRD_PERSON_POSSESSIVES = new HashSet<>();
    public static final Set<String> THIRD_PERSON_POSSESSIVE_NOUNS = new HashSet<>();
    static {
        THIRD_PERSON_POSSESSIVES.add("their");

        THIRD_PERSON_POSSESSIVE_NOUNS.add("theirs");
    }
}
