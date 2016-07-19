package statements.matching;

import statements.core.AbstractComponent;
import statements.core.Statement;
import statements.core.StatementComponent;

import java.util.HashSet;
import java.util.Set;

public class Proxy {
    private final Class type;
    private final Set<String> words;

    /**
     * Standard proxy constructor.
     *
     * @param type the type of component to emulate
     * @param words the words of the component
     */
    public Proxy(Class type, String... words) {
        this.type = type;

        if (words.length > 0) {
            this.words = new HashSet<>();
            for (String word : words) this.words.add(word.toLowerCase());
        } else {
            this.words = null;
        }
    }

    public boolean matches(Statement statement) {
        for (StatementComponent component : statement.getComponents()) {
            if (type.equals(component.getClass())) {
                if (words == null) {
                    System.out.println("found " + getType());
                    return true;
                } else {
                    if (component instanceof AbstractComponent) {
                        AbstractComponent abstractComponent = (AbstractComponent) component;

                        for (String word : words) {
                            if (word.equals(abstractComponent.getNormalCompound())) {
                                System.out.println("found " + word);
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    public Class getType() {
        return type;
    }

    public Set<String> getWords() {
        return words;
    }
}
