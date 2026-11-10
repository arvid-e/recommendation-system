package io.github.arvide.movierecommender.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Rating {
    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Integer movieId;

    @Column(nullable = false)
    private double rating;

    public Rating(Integer userId, Integer movieId, double rating) {
        this.userId = userId;
        this.movieId = movieId;
        this.rating = rating;
    }

    protected Rating() {
    }

    public Integer getMovieId() {
        return movieId;
    }

    public double getRating() {
        return rating;
    }

    public Integer getUserId() {
        return userId;
    }
}
