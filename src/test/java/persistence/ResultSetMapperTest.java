package persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class ResultSetMapperTest {

    private ResultSetMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ResultSetMapper();
    }

    @Test
    void snake_case를_camelCase로_변환한다() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnName(1)).thenReturn("created_at");
        when(rs.getObject(1)).thenReturn(Timestamp.valueOf("2026-01-01 12:00:00"));

        TestEntity entity = mapper.mapToObject(rs, TestEntity.class);

        assertThat(entity.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 12, 0, 0));
    }

    @Test
    void snakeToCamel_변환을_검증한다() {
        assertThat(mapper.snakeToCamel("created_at")).isEqualTo("createdAt");
        assertThat(mapper.snakeToCamel("user_name")).isEqualTo("userName");
        assertThat(mapper.snakeToCamel("id")).isEqualTo("id");
        assertThat(mapper.snakeToCamel("CREATED_AT")).isEqualTo("createdAt");
    }

    @Test
    void ResultSet을_객체로_매핑() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("name");
        when(rs.getObject(1)).thenReturn(1L);
        when(rs.getObject(2)).thenReturn("John");

        TestUser user = mapper.mapToObject(rs, TestUser.class);

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John");
    }

    @Test
    void ResultSet을_리스트로_매핑한다() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("name");
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getObject(1)).thenReturn(1L, 2L);
        when(rs.getObject(2)).thenReturn("Foo", "Bar");

        var users = mapper.mapToList(rs, TestUser.class);

        assertThat(users).hasSize(2);
        assertThat(users.get(0).getName()).isEqualTo("Foo");
        assertThat(users.get(1).getName()).isEqualTo("Bar");
    }

    @Test
    void 매칭되는_필드가_없으면_무시한다() throws Exception {
        // Given
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);
        when(metaData.getColumnName(1)).thenReturn("id");
        when(metaData.getColumnName(2)).thenReturn("extra_column");
        when(rs.getObject(1)).thenReturn(1L);
        when(rs.getObject(2)).thenReturn("ignored");

        // When
        TestUser user = mapper.mapToObject(rs, TestUser.class);

        // Then
        assertThat(user.getId()).isEqualTo(1L);
    }

    @Test
    void 기본_생성자가_없으면_예외를_발생시킨다() {
        ResultSet rs = mock(ResultSet.class);

        assertThatThrownBy(() -> mapper.mapToObject(rs, NoDefaultConstructor.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("기본 생성자가 없습니다");
    }

    @Test
    void 타입이_일치하지_않으면_예외를_발생시킨다() throws Exception {
        // Given
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnName(1)).thenReturn("age");
        when(rs.getObject(1)).thenReturn("not_a_number");

        assertThatThrownBy(() -> mapper.mapToObject(rs, TestUser.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("타입 변환 실패");
    }

    @Test
    void null_값은_그대로_매핑한다() throws Exception {
        // Given
        ResultSet rs = mock(ResultSet.class);
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);
        when(metaData.getColumnName(1)).thenReturn("name");
        when(rs.getObject(1)).thenReturn(null);

        // When
        TestUser user = mapper.mapToObject(rs, TestUser.class);

        // Then
        assertThat(user.getName()).isNull();
    }

    static class TestUser {
        private Long id;
        private String name;
        private int age;

        public TestUser() {
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }

    static class TestEntity {
        private LocalDateTime createdAt;

        public TestEntity() {
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }

    static class NoDefaultConstructor {
        NoDefaultConstructor(String required) {
        }
    }

}