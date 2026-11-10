package io.github.arvide.movierecommender.entity;

public record RatingId(Integer userId, Integer movieId) implements java.io.Serializable {
}
