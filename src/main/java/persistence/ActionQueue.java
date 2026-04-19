package persistence;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActionQueue {

    private final List<EntityAction> actions = new ArrayList<>();

    public void addInsertAction(Object entity) {
        actions.add(new InsertAction(entity));
    }

    public void addUpdateAction(Object entity) {
        actions.add(new UpdateAction(entity));
    }

    public void addDeleteAction(Object entity) {
        actions.add(new DeleteAction(entity));
    }

    public void executeActions(SimpleEntityPersister persister) throws SQLException {
        for (EntityAction action : actions) {
            action.execute(persister);
        }
        clear();
    }

    public void clear() {
        actions.clear();
    }

    interface EntityAction {
        void execute(SimpleEntityPersister persister) throws SQLException;
    }

    record InsertAction(Object entity) implements EntityAction {

        @Override
            public void execute(SimpleEntityPersister persister) throws SQLException {
                persister.insert(entity);
            }
        }

    record UpdateAction(Object entity) implements EntityAction {

        @Override
            public void execute(SimpleEntityPersister persister) throws SQLException {
                persister.update(entity);
            }
        }

    record DeleteAction(Object entity) implements EntityAction {

        @Override
            public void execute(SimpleEntityPersister persister) throws SQLException {
                persister.delete(entity);
            }
        }
}
