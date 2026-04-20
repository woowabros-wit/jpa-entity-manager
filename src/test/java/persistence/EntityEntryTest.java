package persistence;

import entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EntityEntryTest {

    @DisplayName("isModified - 엔티티 필드 값이 하나라도 변경된 경우 true")
    @Test
    void isModified() throws Exception {
        // given
        final User user = new User(1L, "John", 30);
        final EntityEntry entityEntry = new EntityEntry(user);

        // when
        user.setName("Jane");

        // then
        assertThat(entityEntry.isModified()).isTrue();
    }

    @DisplayName("isModified - 엔티티 필드 값이 변경되지 않은 경우 false")
    @Test
    void isModified1() throws Exception {
        // given
        final User user = new User(1L, "John", 30);
        final EntityEntry entityEntry = new EntityEntry(user);

        // when
        user.setName("John");

        // then
        assertThat(entityEntry.isModified()).isFalse();
    }

}