package mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import entity.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
class ResultSetMapperTest {

    private Connection connection;
    private ResultSetMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:mapper_test;DB_CLOSE_DELAY=-1");
        mapper = new ResultSetMapper();
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DROP ALL OBJECTS");
        }
        connection.close();
    }

    @Test
    void 기본_매핑() throws Exception {
        createTable("CREATE TABLE users (id BIGINT, name VARCHAR(255), age INT)");
        execute("INSERT INTO users VALUES (1, 'Alice', 25)");

        try (ResultSet rs = query("SELECT id, name, age FROM users")) {
            List<User> result = mapper.mapToList(rs, User.class);

            assertThat(result).hasSize(1);
            assertThat(getField(result.getFirst(), "id")).isEqualTo(1L);
            assertThat(getField(result.getFirst(), "name")).isEqualTo("Alice");
            assertThat(getField(result.getFirst(), "age")).isEqualTo(25);
        }
    }

    @Test
    void snake_case를_camelCase로_변환() throws Exception {
        createTable("CREATE TABLE posts (id BIGINT, user_name VARCHAR(255), created_at TIMESTAMP)");
        execute("INSERT INTO posts VALUES (1, 'Bob', '2024-01-15 10:30:00')");

        try (ResultSet rs = query("SELECT id, user_name, created_at FROM posts")) {
            List<Post> result = mapper.mapToList(rs, Post.class);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().userName).isEqualTo("Bob");
            assertThat(result.getFirst().createdAt).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
        }
    }

    @Test
    void 매칭되지_않는_컬럼은_무시() throws Exception {
        createTable("CREATE TABLE users (id BIGINT, name VARCHAR(255), unknown_col VARCHAR(255))");
        execute("INSERT INTO users VALUES (1, 'Alice', 'ignored')");

        try (ResultSet rs = query("SELECT id, name, unknown_col FROM users")) {
            List<User> result = mapper.mapToList(rs, User.class);

            assertThat(result).hasSize(1);
            assertThat(getField(result.getFirst(), "name")).isEqualTo("Alice");
        }
    }

    @Test
    void 기본_생성자가_없으면_예외() throws Exception {
        createTable("CREATE TABLE users (id BIGINT)");
        execute("INSERT INTO users VALUES (1)");

        try (ResultSet rs = query("SELECT id FROM users")) {
            rs.next();
            assertThatThrownBy(() -> mapper.mapToObject(rs, NoDefaultConstructor.class))
                .isInstanceOf(NoSuchMethodException.class);
        }
    }

    @Test
    void INTEGER를_Long으로_변환() throws Exception {
        createTable("CREATE TABLE numbers (id BIGINT, amount INT)");
        execute("INSERT INTO numbers VALUES (1, 42)");

        try (ResultSet rs = query("SELECT id, amount FROM numbers")) {
            List<LongHolder> result = mapper.mapToList(rs, LongHolder.class);

            assertThat(result.getFirst().id).isEqualTo(1L);
            assertThat(result.getFirst().amount).isEqualTo(42L);
        }
    }

    @Test
    void BOOLEAN_타입_변환() throws Exception {
        createTable("CREATE TABLE flags (id BIGINT, active BOOLEAN)");
        execute("INSERT INTO flags VALUES (1, TRUE)");

        try (ResultSet rs = query("SELECT id, active FROM flags")) {
            List<BooleanHolder> result = mapper.mapToList(rs, BooleanHolder.class);

            assertThat(result.getFirst().active).isTrue();
        }
    }

    @Test
    void primitive_int_타입_변환() throws Exception {
        createTable("CREATE TABLE scores (amount INT)");
        execute("INSERT INTO scores VALUES (10)");

        try (ResultSet rs = query("SELECT amount FROM scores")) {
            List<PrimitiveIntHolder> result = mapper.mapToList(rs, PrimitiveIntHolder.class);
            assertThat(result.getFirst().amount).isEqualTo(10);
        }
    }

    @Test
    void primitive_long_타입_변환() throws Exception {
        createTable("CREATE TABLE scores (amount BIGINT)");
        execute("INSERT INTO scores VALUES (999999999999)");

        try (ResultSet rs = query("SELECT amount FROM scores")) {
            List<PrimitiveLongHolder> result = mapper.mapToList(rs, PrimitiveLongHolder.class);
            assertThat(result.getFirst().amount).isEqualTo(999999999999L);
        }
    }

    @Test
    void Double_래퍼_타입_변환() throws Exception {
        createTable("CREATE TABLE scores (score DOUBLE)");
        execute("INSERT INTO scores VALUES (3.14)");

        try (ResultSet rs = query("SELECT score FROM scores")) {
            List<DoubleHolder> result = mapper.mapToList(rs, DoubleHolder.class);
            assertThat(result.getFirst().score).isEqualTo(3.14);
        }
    }

    @Test
    void primitive_double_타입_변환() throws Exception {
        createTable("CREATE TABLE scores (score DOUBLE)");
        execute("INSERT INTO scores VALUES (2.71)");

        try (ResultSet rs = query("SELECT score FROM scores")) {
            List<PrimitiveDoubleHolder> result = mapper.mapToList(rs, PrimitiveDoubleHolder.class);
            assertThat(result.getFirst().score).isEqualTo(2.71);
        }
    }

    @Test
    void Float_래퍼_타입_변환() throws Exception {
        createTable("CREATE TABLE scores (score REAL)");
        execute("INSERT INTO scores VALUES (1.5)");

        try (ResultSet rs = query("SELECT score FROM scores")) {
            List<FloatHolder> result = mapper.mapToList(rs, FloatHolder.class);
            assertThat(result.getFirst().score).isEqualTo(1.5f);
        }
    }

    @Test
    void primitive_float_타입_변환() throws Exception {
        createTable("CREATE TABLE scores (score REAL)");
        execute("INSERT INTO scores VALUES (2.5)");

        try (ResultSet rs = query("SELECT score FROM scores")) {
            List<PrimitiveFloatHolder> result = mapper.mapToList(rs, PrimitiveFloatHolder.class);
            assertThat(result.getFirst().score).isEqualTo(2.5f);
        }
    }

    @Test
    void Short_래퍼_타입_변환() throws Exception {
        createTable("CREATE TABLE rankings (rank SMALLINT)");
        execute("INSERT INTO rankings VALUES (3)");

        try (ResultSet rs = query("SELECT rank FROM rankings")) {
            List<ShortHolder> result = mapper.mapToList(rs, ShortHolder.class);
            assertThat(result.getFirst().rank).isEqualTo((short) 3);
        }
    }

    @Test
    void primitive_short_타입_변환() throws Exception {
        createTable("CREATE TABLE rankings (rank SMALLINT)");
        execute("INSERT INTO rankings VALUES (7)");

        try (ResultSet rs = query("SELECT rank FROM rankings")) {
            List<PrimitiveShortHolder> result = mapper.mapToList(rs, PrimitiveShortHolder.class);
            assertThat(result.getFirst().rank).isEqualTo((short) 7);
        }
    }

    @Test
    void primitive_boolean_타입_변환() throws Exception {
        createTable("CREATE TABLE flags (active BOOLEAN)");
        execute("INSERT INTO flags VALUES (FALSE)");

        try (ResultSet rs = query("SELECT active FROM flags")) {
            List<PrimitiveBooleanHolder> result = mapper.mapToList(rs, PrimitiveBooleanHolder.class);
            assertThat(result.getFirst().active).isFalse();
        }
    }

    @Test
    void 모든_지원_타입_한번에_매핑() throws Exception {
        createTable("CREATE TABLE all_types ("
            + "primitive_int INT, wrapper_int INT, "
            + "primitive_long BIGINT, wrapper_long BIGINT, "
            + "primitive_double DOUBLE, wrapper_double DOUBLE, "
            + "primitive_float REAL, wrapper_float REAL, "
            + "primitive_short SMALLINT, wrapper_short SMALLINT, "
            + "primitive_boolean BOOLEAN, wrapper_boolean BOOLEAN, "
            + "text VARCHAR(255), created_at TIMESTAMP)");
        execute("INSERT INTO all_types VALUES ("
            + "1, 2, 3, 4, 1.1, 2.2, 3.3, 4.4, 5, 6, TRUE, FALSE, 'hello', '2024-06-01 12:00:00')");

        try (ResultSet rs = query("SELECT * FROM all_types")) {
            List<AllTypesHolder> result = mapper.mapToList(rs, AllTypesHolder.class);

            AllTypesHolder h = result.getFirst();
            assertThat(h.primitiveInt).isEqualTo(1);
            assertThat(h.wrapperInt).isEqualTo(2);
            assertThat(h.primitiveLong).isEqualTo(3L);
            assertThat(h.wrapperLong).isEqualTo(4L);
            assertThat(h.primitiveDouble).isEqualTo(1.1);
            assertThat(h.wrapperDouble).isEqualTo(2.2);
            assertThat(h.primitiveFloat).isEqualTo(3.3f);
            assertThat(h.wrapperFloat).isEqualTo(4.4f);
            assertThat(h.primitiveShort).isEqualTo((short) 5);
            assertThat(h.wrapperShort).isEqualTo((short) 6);
            assertThat(h.primitiveBoolean).isTrue();
            assertThat(h.wrapperBoolean).isFalse();
            assertThat(h.text).isEqualTo("hello");
            assertThat(h.createdAt).isEqualTo(LocalDateTime.of(2024, 6, 1, 12, 0, 0));
        }
    }

    @Test
    void 타입_불일치시_예외() throws Exception {
        createTable("CREATE TABLE items (id BIGINT, name VARCHAR(255))");
        execute("INSERT INTO items VALUES (1, 'test')");

        try (ResultSet rs = query("SELECT id, name FROM items")) {
            rs.next();
            assertThatThrownBy(() -> mapper.mapToObject(rs, TypeMismatchHolder.class))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void 빈_ResultSet이면_빈_리스트() throws Exception {
        createTable("CREATE TABLE users (id BIGINT, name VARCHAR(255))");

        try (ResultSet rs = query("SELECT id, name FROM users")) {
            List<User> result = mapper.mapToList(rs, User.class);

            assertThat(result).isEmpty();
        }
    }

    @Test
    void NULL_값은_필드에_설정하지_않음() throws Exception {
        createTable("CREATE TABLE users (id BIGINT, name VARCHAR(255))");
        execute("INSERT INTO users VALUES (1, NULL)");

        try (ResultSet rs = query("SELECT id, name FROM users")) {
            List<User> result = mapper.mapToList(rs, User.class);

            assertThat(getField(result.getFirst(), "id")).isEqualTo(1L);
            assertThat(getField(result.getFirst(), "name")).isNull();
        }
    }

    @Test
    void 여러_행_매핑() throws Exception {
        createTable("CREATE TABLE users (id BIGINT, name VARCHAR(255), age INT)");
        execute("INSERT INTO users VALUES (1, 'Alice', 25)");
        execute("INSERT INTO users VALUES (2, 'Bob', 30)");
        execute("INSERT INTO users VALUES (3, 'Charlie', 35)");

        try (ResultSet rs = query("SELECT id, name, age FROM users")) {
            List<User> result = mapper.mapToList(rs, User.class);

            assertThat(result).hasSize(3);
            assertThat(getField(result.getFirst(), "name")).isEqualTo("Alice");
            assertThat(getField(result.get(1), "name")).isEqualTo("Bob");
            assertThat(getField(result.get(2), "name")).isEqualTo("Charlie");
        }
    }

    // --- 테스트용 내부 클래스 ---
    static class AllTypesHolder {

        private int primitiveInt;
        private Integer wrapperInt;
        private long primitiveLong;
        private Long wrapperLong;
        private double primitiveDouble;
        private Double wrapperDouble;
        private float primitiveFloat;
        private Float wrapperFloat;
        private short primitiveShort;
        private Short wrapperShort;
        private boolean primitiveBoolean;
        private Boolean wrapperBoolean;
        private String text;
        private LocalDateTime createdAt;
    }

    static class Post {

        private Long id;
        private String userName;
        private LocalDateTime createdAt;
    }

    static class NoDefaultConstructor {

        private final Long id;

        NoDefaultConstructor(Long id) {
            this.id = id;
        }
    }

    static class LongHolder {

        private Long id;
        private Long amount;
    }

    static class BooleanHolder {

        private Long id;
        private Boolean active;
    }

    static class TypeMismatchHolder {

        private LocalDateTime name;
    }

    static class PrimitiveIntHolder {

        private int amount;
    }

    static class PrimitiveLongHolder {

        private long amount;
    }

    static class DoubleHolder {

        private Double score;
    }

    static class PrimitiveDoubleHolder {

        private double score;
    }

    static class FloatHolder {

        private Float score;
    }

    static class PrimitiveFloatHolder {

        private float score;
    }

    static class ShortHolder {

        private Short rank;
    }

    static class PrimitiveShortHolder {

        private short rank;
    }

    static class PrimitiveBooleanHolder {

        private boolean active;
    }

    private void createTable(String ddl) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(ddl);
        }
    }

    private void execute(String sql) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private ResultSet query(String sql) throws Exception {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    private Object getField(Object obj, String fieldName) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}
