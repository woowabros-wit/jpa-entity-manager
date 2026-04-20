package persistence;

import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PersistenceContextTest {

    private PersistenceContext persistenceContext;

    @BeforeEach
    void setUp() {
        persistenceContext = new PersistenceContext();
    }

    @DisplayName("get - 조회 결과가 없는 경우 null 을 반환한다")
    @Test
    void get() {
        // given
        final Class<User> entityClass = User.class;
        final EntityKey entityKey = new EntityKey(entityClass, 1L);

        // when
        final Object result = persistenceContext.get(entityKey);

        // then
        assertThat(result).isNull();
    }

    @DisplayName("put - 엔티티를 저장한다")
    @Test
    void put() {
        // given
        final Class<User> entityClass = User.class;
        final EntityKey entityId = new EntityKey(entityClass, 1L);
        final User user = new User(1L, "test-user", 33);

        // when
        persistenceContext.put(entityId, user);

        // then
        final Object result = persistenceContext.get(entityId);
        assertThat(result).isSameAs(user);
    }

}