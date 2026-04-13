package persistence.repository;

import persistence.entity.User;
import persistence.query.*;

import java.sql.Connection;
import java.util.List;

public class UserRepository {
    private static final String TABLE_NAME = "users";

    private final QueryExecutor executor;

    public UserRepository(Connection connection) {
        this.executor = new QueryExecutor(connection);
    }

    public List<User> findAll() throws Exception {
        String sql = new SelectQueryBuilder()
                .select("*")
                .from(TABLE_NAME)
                .build();

        return executor.query(sql, User.class);
    }

    public List<User> findByAgeGreaterThan(int age) throws Exception {
        NamedParameterQuery query = new NamedParameterQuery(
                "SELECT * FROM " + TABLE_NAME + " WHERE age > :age ORDER BY age DESC"
        );
        query.setParameter("age", age);

        return executor.query(query, User.class);
    }

    public int insert(User user) throws Exception {
        String sql = new InsertQueryBuilder()
                .into(TABLE_NAME)
                .value("name", "?")
                .value("age", "?")
                .value("email", "?")
                .build();

        return executor.execute(sql, user.getName(), user.getAge(), user.getEmail());
    }

    public int insertWithNamedParams(User user) throws Exception {
        NamedParameterQuery query = new NamedParameterQuery(
                "INSERT INTO " + TABLE_NAME + " (name, age, email) VALUES (:name, :age, :email)"
        );
        query.setParameter("name", user.getName());
        query.setParameter("age", user.getAge());
        query.setParameter("email", user.getEmail());

        return executor.execute(query);
    }

    public int update(User user) throws Exception {
        String sql = new UpdateQueryBuilder()
                .table(TABLE_NAME)
                .set("name", "?")
                .set("age", "?")
                .set("email", "?")
                .where("id = ?")
                .build();

        return executor.execute(sql, user.getName(), user.getAge(), user.getEmail(), user.getId());
    }

    public int updateWithNamedParams(User user) throws Exception {
        NamedParameterQuery query = new NamedParameterQuery(
                "UPDATE " + TABLE_NAME + " SET name = :name, age = :age, email = :email WHERE id = :id"
        );
        query.setParameter("name", user.getName());
        query.setParameter("age", user.getAge());
        query.setParameter("email", user.getEmail());
        query.setParameter("id", user.getId());

        return executor.execute(query);
    }

    public int deleteById(Long id) throws Exception {
        String sql = new DeleteQueryBuilder()
                .from(TABLE_NAME)
                .where("id = ?")
                .build();

        return executor.execute(sql, id);
    }

    public int deleteByEmailWithNamedParams(String email) throws Exception {
        NamedParameterQuery query = new NamedParameterQuery(
                "DELETE FROM " + TABLE_NAME + " WHERE email = :email"
        );
        query.setParameter("email", email);

        return executor.execute(query);
    }
}