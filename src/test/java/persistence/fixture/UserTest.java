package persistence.fixture;

import annotation.Id;
import annotation.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void findIdAnnotationTest() {
        Class<User> entityClass = User.class;

        String name = "";
        Field[] declaredFields = entityClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Id.class)) {
                name = declaredField.getName();
            }
        }

        assertThat(name).isEqualTo("id");
    }

    @Test
    void findTableNameAnnotation() {
        Class<User> entityClass = User.class;

        Table annotation = entityClass.getAnnotation(Table.class);
        String name = annotation.name();
        assertThat(name).isEqualTo("users");
    }
}