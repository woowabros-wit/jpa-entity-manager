package entitymanager;

import java.util.ArrayList;
import java.util.List;

public class ActionQueue {
    private final List<EntityAction> insertions = new ArrayList<>();
    private final List<EntityAction> updates = new ArrayList<>();
    private final List<EntityAction> deletions = new ArrayList<>();

    public void addInsertion(EntityAction action) {
        insertions.add(action);
    }

    public void addUpdate(EntityAction action) {
        updates.add(action);
    }

    public void addDeletion(EntityAction action) {
        deletions.add(action);
    }

    public void executeAll() throws Exception {
        for (EntityAction action : insertions) {
            action.execute();
        }
        for (EntityAction action : updates) {
            action.execute();
        }
        for (EntityAction action : deletions) {
            action.execute();
        }
        clear();
    }

    public void clear() {
        insertions.clear();
        updates.clear();
        deletions.clear();
    }
}
