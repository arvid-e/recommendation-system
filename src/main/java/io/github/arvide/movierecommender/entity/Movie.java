package io.github.arvide.movierecommender.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    private Integer id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column (nullable = false)
    private Integer year;

    public Movie(Integer id, String title, Integer year) {
        this.id = id;
        this.title = title;
        this.year = year;
    }

    protected Movie() {
    }
}
