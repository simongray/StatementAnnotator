package statements.profile;

import statements.core.Statement;
import statements.core.StatementComponent;
import statements.core.StatementUtils;

import java.util.Set;

/**
 * Created by simongray on 14/07/16.
 */
public class ComponentSearchString implements Predicate {
    private final Class componentType;
    private final String searchString;

    public ComponentSearchString(Class componentType, String searchString) {
        this.componentType = componentType;
        this.searchString = searchString.toLowerCase();
    }

    @Override
    public boolean evaluate(Statement statement) {
        Set<StatementComponent> components = statement.getComponents();

        for (StatementComponent component : components) {
            if (component.getClass().equals(componentType)) {
                if (StatementUtils.join(component.getCompound()).toLowerCase().equals(searchString)) {
                    return true;
                }
            }
        }

        return false;
    }
}
