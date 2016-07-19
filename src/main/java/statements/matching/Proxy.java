package statements.matching;

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
            for (String word : words) this.words.add(word);
        } else {
            this.words = null;
        }
    }

    public boolean matches(Statement statement) {
        for (StatementComponent component : statement.getComponents()) {
            if (getType().equals(component.getClass())) {
                System.out.println("found " + getType());
                // TODO: more stuff needs to be done
                return true;
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
