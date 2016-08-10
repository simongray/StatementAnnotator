package statements.patterns;

import statements.core.StatementComponent;

import java.util.Collection;

public interface Pattern {
    boolean matches(StatementComponent statementComponent);
    StatementComponent getCaptured();
    Class[] getTypes();
    boolean mustMatchAll();
    boolean isOptional();

    default boolean containsTypes(Class... componentTypes) {
        for (Class componentType : componentTypes) {
            for (Class type : getTypes()) {
                if (componentType.equals(type)) return true;
            }
        }

        return false;
    }

    default boolean containsTypes(Collection<StatementComponent> components) {
        for (StatementComponent component : components) {
            for (Class type : getTypes()) {
                if (component.getClass().equals(type)) return true;
            }
        }

        return false;
    }
}
