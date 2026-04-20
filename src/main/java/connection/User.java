package connection;

@Table(name = "users")
public class User {

    @Id
    private Long id;
    private String name;
    private Integer age;

    public User() {}

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
