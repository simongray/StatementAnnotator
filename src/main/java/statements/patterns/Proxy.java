package statements.patterns;

import statements.core.*;

import java.util.HashSet;
import java.util.Set;

public class Proxy {
    private final Class type;
    private final Set<String> words;
    private final Pattern pattern;

    /**
     * Standard private proxy constructor.
     * Use the factory functions below instead.
     *
     * @param type the type of component to emulate
     * @param words the words of the component
     */
    private Proxy(Class type, String... words) {
        this.type = type;
        this.pattern = null;

        if (words.length > 0) {
            this.words = new HashSet<>();
            for (String word : words) this.words.add(word.toLowerCase());
        } else {
            this.words = null;
        }
    }

    private Proxy(Class type, Set<String> words) {
        this.type = type;
        this.pattern = null;
        this.words = words;
    }

    /**
     * Constructor used for embedded statements.
     * Use the factory function below.
     *
     * @param proxies the embedded statement components to emulate
     */
    private Proxy(Proxy... proxies) {
        this.type = Statement.class;
        this.words = null;
        this.pattern = new Pattern(proxies);
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

    public static Proxy Subject(Set<String> words) {
        return new Proxy(Subject.class, words);
    }

    public static Proxy Verb(Set<String> words) {
        return new Proxy(Verb.class, words);
    }

    public static Proxy DirectObject(Set<String> words) {
        return new Proxy(DirectObject.class, words);
    }

    public static Proxy IndirectObject(Set<String> words) {
        return new Proxy(IndirectObject.class, words);
    }


    public static Proxy Statement(Proxy... proxies) {
        return new Proxy(proxies);
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
        if (type.equals(Statement.class)) {
            return matchesStatement(statement);
        } else {
            return matchesComponent(statement);
        }
    }

    private boolean matchesStatement(Statement statement) {
        if (statement.getEmbeddedStatement() != null) {
            return pattern.matches(statement.getEmbeddedStatement());
        }

        return false;
    }

    // TODO: really should only be making a single run through components, not one for every proxy!
    private boolean matchesComponent(Statement statement) {
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
