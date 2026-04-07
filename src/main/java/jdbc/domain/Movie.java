package jdbc.domain;

import java.time.LocalDate;

public class Movie {
    private String name;
    private Long attendance;
    private Boolean isMasterpiece;
    private LocalDate releaseDate;
    private Double rating;

    public Movie() {}

    public Movie(String name, Long attendance, Boolean isMasterpiece, LocalDate releaseDate, Double rating) {
        this.name = name;
        this.attendance = attendance;
        this.isMasterpiece = isMasterpiece;
        this.releaseDate = releaseDate;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public Long getAttendance() {
        return attendance;
    }

    public Boolean getMasterpiece() {
        return isMasterpiece;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public Double getRating() {
        return rating;
    }
}
