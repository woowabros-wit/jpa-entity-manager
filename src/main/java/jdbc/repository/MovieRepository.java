package jdbc.repository;

import jdbc.NamedParameterQuery;
import jdbc.QueryExecutor;
import jdbc.domain.Movie;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class MovieRepository {

    private final QueryExecutor executor;

    public MovieRepository(Connection connection) {
        this.executor = new QueryExecutor(connection);
    }

    public void insert(Movie movie) throws SQLException {
        NamedParameterQuery query = new NamedParameterQuery(
                "INSERT INTO movie (name, attendance, is_masterpiece, release_date, rating) " +
                        "VALUES (:name, :attendance, :isMasterpiece, :releaseDate, :rating)");
        query.setParameter("name", movie.getName())
                .setParameter("attendance", movie.getAttendance())
                .setParameter("isMasterpiece", movie.getMasterpiece())
                .setParameter("releaseDate", movie.getReleaseDate())
                .setParameter("rating", movie.getRating());
        executor.execute(query);
    }

    public List<Movie> findAll() throws Exception {
        return executor.query("SELECT name, attendance, is_masterpiece, release_date, rating FROM movie", Movie.class);
    }

    public List<Movie> findByName(String name) throws Exception {
        NamedParameterQuery query = new NamedParameterQuery(
                "SELECT name, attendance, is_masterpiece, release_date, rating FROM movie WHERE name = :name");
        query.setParameter("name", name);
        return executor.query(query, Movie.class);
    }

    public void update(Movie movie, String targetName) throws SQLException {
        NamedParameterQuery query = new NamedParameterQuery(
                "UPDATE movie SET attendance = :attendance, is_masterpiece = :isMasterpiece, " +
                        "release_date = :releaseDate, rating = :rating WHERE name = :name");
        query.setParameter("attendance", movie.getAttendance())
                .setParameter("isMasterpiece", movie.getMasterpiece())
                .setParameter("releaseDate", movie.getReleaseDate())
                .setParameter("rating", movie.getRating())
                .setParameter("name", targetName);
        executor.execute(query);
    }

    public void deleteByName(String name) throws SQLException {
        NamedParameterQuery query = new NamedParameterQuery(
                "DELETE FROM movie WHERE name = :name");
        query.setParameter("name", name);
        executor.execute(query);
    }
}
