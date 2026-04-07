package persistence.fixture;


import annotation.Id;
import annotation.Table;

@Table(name = "users")
public class User {

    @Id
    private Long id;
    private String name;
    private int age;

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
