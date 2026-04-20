package persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EntityEntry {
    private final Object entity;
    private final Object[] loadedState;

    public EntityEntry(Object entity, Object[] loadedState) {
        this.entity = entity;
        this.loadedState = Arrays.copyOf(loadedState, loadedState.length);
    }

    public Object getEntity() {
        return entity;
    }

    public boolean isDirty(Object[] currentState) {
        return !Arrays.equals(loadedState, currentState);
    }

    public int[] findModified(Object[] currentState) {
        List<Integer> modified = new ArrayList<>();
        for (int i = 0; i < loadedState.length; i++) {
            if (!Objects.equals(loadedState[i], currentState[i])) {
                modified.add(i);
            }
        }
        return modified.stream().mapToInt(i -> i).toArray();
    }
}
