package persistence.fixture;


import persistence.annotation.Id;
import persistence.annotation.Table;

@Table(name = "users")
public class User2 {

    @Id
    private Long id;
    private String name;
    private int age;

    public User2() {
    }

    public User2(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
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
