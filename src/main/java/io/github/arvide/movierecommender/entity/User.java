package io.github.arvide.movierecommender.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    public User(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    protected User() {
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
