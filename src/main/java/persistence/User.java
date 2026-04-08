package persistence;

public class User {
    private Long id;
    private String name;
    private Integer age;
    private String email;

    // 기본 생성자 필수!
    public User() {
    }

    // Getters/Setters...

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public String getEmail() {
        return email;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}