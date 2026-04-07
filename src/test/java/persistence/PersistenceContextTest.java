package persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.fixture.User;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceContextTest {

    private PersistenceContext pc;

    @BeforeEach
    void setUp() {
        pc = new PersistenceContext();
    }

    @Test
    void 저장한_엔티티를_조회할_수_있다() {
        User user = new User();
        pc.put(User.class, 1L, user);

        User found = pc.get(User.class, 1L);

        assertSame(user, found);
    }

    @Test
    void 저장하지_않은_엔티티는_null을_반환한다() {
        User found = pc.get(User.class, 1L);

        assertNull(found);
    }

    @Test
    void 같은_타입_다른_ID는_다른_인스턴스를_반환한다() {
        User user1 = new User();
        User user2 = new User();
        pc.put(User.class, 1L, user1);
        pc.put(User.class, 2L, user2);

        assertSame(user1, pc.get(User.class, 1L));
        assertSame(user2, pc.get(User.class, 2L));
        assertNotSame(user1, user2);
    }

    @Test
    void 다른_타입_같은_ID는_서로_영향을_주지_않는다() {
        User user = new User();
        String other = "other";
        pc.put(User.class, 1L, user);
        pc.put(String.class, 1L, other);

        assertSame(user, pc.get(User.class, 1L));
        assertSame(other, pc.get(String.class, 1L));
    }
}
