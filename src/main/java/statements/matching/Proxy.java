package statements.matching;

import statements.core.*;

import java.util.HashSet;
import java.util.Set;

public class Proxy {
    private final Class type;
    private final Set<String> words;

    /**
     * Standard private proxy constructor.
     * Use the factory functions below instead.
     *
     * @param type the type of component to emulate
     * @param words the words of the component
     */
    private Proxy(Class type, String... words) {
        this.type = type;

        if (words.length > 0) {
            this.words = new HashSet<>();
            for (String word : words) this.words.add(word.toLowerCase());
        } else {
            this.words = null;
        }
    }

    public static Proxy Subject(String... words) {
        return new Proxy(Subject.class, words);
    }

    public static Proxy Verb(String... words) {
        return new Proxy(Verb.class, words);
    }

    public static Proxy DirectObject(String... words) {
        return new Proxy(DirectObject.class, words);
    }

    public static Proxy IndirectObject(String... words) {
        return new Proxy(IndirectObject.class, words);
    }

    /**
     * Matches the statement if all conditions are met.
     * Examples of conditions:
     *     * specified component type found
     *     * (one of) specified word(s) found
     *
     * @param statement statement to test
     * @return true if proxy matches component in statement
     */
    public boolean matches(Statement statement) {
        for (StatementComponent component : statement.getComponents()) {
            if (type.equals(component.getClass())) {
                if (words == null) {
                    return true;
                } else {
                    if (component instanceof AbstractComponent) {
                        AbstractComponent abstractComponent = (AbstractComponent) component;

                        for (String word : words) {
                            if (word.equals(abstractComponent.getNormalCompound())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Returns the component(s) that match this particular proxy.
     *
     * @param statement  statement to test
     * @return the matching components
     */
    public Set<StatementComponent> getMatchingComponents(Statement statement) {
        Set<StatementComponent> matchingComponents = new HashSet<>();

        for (StatementComponent component : statement.getComponents()) {
            if (type.equals(component.getClass())) {
                if (component instanceof AbstractComponent) {
                    AbstractComponent abstractComponent = (AbstractComponent) component;

                    for (String word : words) {
                        if (word.equals(abstractComponent.getNormalCompound())) {
                            matchingComponents.add(component);
                        }
                    }
                }
            }
        }

        return matchingComponents;
    }
}
