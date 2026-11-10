package io.github.arvide.movierecommender.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(RatingId.class)
@Table(name = "ratings")
public class Rating {
    @Id 
    private Integer userId;

    @Id
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
