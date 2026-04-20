package entity;

import persistence.Id;
import persistence.Table;

// Entity 클래스
@Table(name = "users")
public class User {
    @Id
    private Long id;
    private String name;
    private Integer age;
    private String email;

    private Integer height;

    // 기본 생성자 필수!
    public User() {
        this(null, null, null, null, null);
    }

    public User(String name, int age) {
        this(null, name, age, null, null);
    }

    public User(final Long id, final String name, final Integer age, final String email, final Integer height) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.email = email;
        this.height = height;
    }

    // Getters/Setters...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}
