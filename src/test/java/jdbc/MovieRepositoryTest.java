package jdbc;

import database.H2;
import jdbc.domain.Movie;
import jdbc.repository.MovieRepository;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovieRepositoryTest {

    private static H2 h2;
    private Connection connection;
    private MovieRepository repository;

    @BeforeAll
    static void beforeAll() throws SQLException {
        h2 = new H2();
        h2.start();
    }

    @AfterAll
    static void afterAll() {
        h2.stop();
    }

    @BeforeEach
    void setUp() throws SQLException {
        connection = h2.getConnection();
        repository = new MovieRepository(connection);

        Statement stmt = connection.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS movie (" +
                "name VARCHAR(255) PRIMARY KEY, " +
                "attendance BIGINT, " +
                "is_masterpiece BOOLEAN, " +
                "release_date DATE, " +
                "rating DOUBLE" +
                ")");
        stmt.execute("DELETE FROM movie");
        stmt.close();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    @DisplayName("insert_영화를_저장한다")
    void test01() throws Exception {
        Movie movie = new Movie("체인소맨 레제편", 3_450_000L, true, LocalDate.of(2025, 9, 26), 9.9);

        repository.insert(movie);

        List<Movie> movies = repository.findAll();
        assertEquals(1, movies.size());
        assertEquals("체인소맨 레제편", movies.get(0).getName());
    }

    @Test
    @DisplayName("insert_여러_영화를_저장하고_모두_조회한다")
    void test02() throws Exception {
        Movie movie1 = new Movie("체인소맨 레제편", 3_450_000L, true, LocalDate.of(2025, 9, 26), 9.9);
        Movie movie2 = new Movie("룩백", 300_000L, true, LocalDate.of(2024, 9, 5), 9.9);
        Movie movie3 = new Movie("진격의 거인 The Last Attack", 1_020_000L, true, LocalDate.of(2025, 3, 13), 9.9);

        repository.insert(movie1);
        repository.insert(movie2);
        repository.insert(movie3);

        List<Movie> movies = repository.findAll();
        assertEquals(3, movies.size());
    }

    @Test
    @DisplayName("findAll_영화가_없으면_빈_리스트를_반환한다")
    void test03() throws Exception {
        List<Movie> movies = repository.findAll();

        assertTrue(movies.isEmpty());
    }

    @Test
    @DisplayName("findByName_이름으로_영화를_조회한다")
    void test04() throws Exception {
        Movie movie1 = new Movie("체인소맨 레제편", 3_450_000L, true, LocalDate.of(2025, 9, 26), 9.9);
        Movie movie2 = new Movie("룩백", 300_000L, true, LocalDate.of(2024, 9, 5), 9.9);
        Movie movie3 = new Movie("진격의 거인 The Last Attack", 1_020_000L, true, LocalDate.of(2025, 3, 13), 9.9);

        repository.insert(movie1);
        repository.insert(movie2);
        repository.insert(movie3);

        List<Movie> movies = repository.findByName("체인소맨 레제편");

        assertEquals(1, movies.size());
        assertEquals("체인소맨 레제편", movies.get(0).getName());
        assertEquals(3_450_000L, movies.get(0).getAttendance());
        assertTrue(movies.get(0).getMasterpiece());
        assertEquals(LocalDate.of(2025, 9, 26), movies.get(0).getReleaseDate());
        assertEquals(9.9, movies.get(0).getRating());
    }

    @Test
    @DisplayName("findByName_존재하지_않는_이름이면_빈_리스트를_반환한다")
    void test06() throws Exception {
        Movie movie1 = new Movie("체인소맨 레제편", 3_450_000L, true, LocalDate.of(2025, 9, 26), 9.9);
        Movie movie2 = new Movie("룩백", 300_000L, true, LocalDate.of(2024, 9, 5), 9.9);
        Movie movie3 = new Movie("진격의 거인 The Last Attack", 1_020_000L, true, LocalDate.of(2025, 3, 13), 9.9);

        repository.insert(movie1);
        repository.insert(movie2);
        repository.insert(movie3);

        List<Movie> movies = repository.findByName("우마무스메 신시대의 문");

        assertTrue(movies.isEmpty());
    }

    @Test
    @DisplayName("update_영화_정보를_수정한다")
    void test07() throws Exception {
        Movie movie = new Movie("모노노케 히메", 200_000L, false, LocalDate.of(2003, 4, 25), 1.0);
        repository.insert(movie);

        Movie updated = new Movie("모노노케 히메", 200_000L, true, LocalDate.of(2003, 4, 25), 9.9);
        repository.update(updated, "모노노케 히메");

        List<Movie> movies = repository.findByName("모노노케 히메");
        assertEquals(1, movies.size());
        assertEquals(200_000L, movies.get(0).getAttendance());
        assertEquals(9.9, movies.get(0).getRating());
    }

    @Test
    @DisplayName("update_다른_영화에는_영향을_주지_않는다")
    void test08() throws Exception {
        Movie movie1 = new Movie("모노노케 히메", 200_000L, false, LocalDate.of(2003, 4, 25), 1.0);
        Movie movie2 = new Movie("룩백", 300_000L, true, LocalDate.of(2024, 9, 5), 9.5);
        repository.insert(movie1);
        repository.insert(movie2);

        Movie updated = new Movie("모노노케 히메", 200_000L, true, LocalDate.of(2003, 4, 25), 9.9);
        repository.update(updated, "모노노케 히메");

        List<Movie> movies = repository.findByName("룩백");
        assertEquals(300_000L, movies.get(0).getAttendance());
        assertEquals(9.5, movies.get(0).getRating());
    }

    @Test
    @DisplayName("deleteByName_이름으로_영화를_삭제한다")
    void test09() throws Exception {
        Movie movie1 = new Movie("모노노케 히메", 200_000L, false, LocalDate.of(2003, 4, 25), 1.0);
        Movie movie2 = new Movie("룩백", 300_000L, true, LocalDate.of(2024, 9, 5), 9.5);
        repository.insert(movie1);
        repository.insert(movie2);

        repository.deleteByName("룩백");

        List<Movie> movies = repository.findAll();
        assertEquals(1, movies.size());
        assertEquals("모노노케 히메", movies.get(0).getName());
    }

    @Test
    @DisplayName("deleteByName_삭제_후_해당_영화가_조회되지_않는다")
    void test10() throws Exception {
        Movie movie = new Movie("체인소맨 레제편", 3_450_000L, true, LocalDate.of(2025, 9, 26), 9.9);
        repository.insert(movie);

        repository.deleteByName("체인소맨 레제편");

        List<Movie> movies = repository.findByName("체인소맨 레제편");
        assertTrue(movies.isEmpty());
    }
}
