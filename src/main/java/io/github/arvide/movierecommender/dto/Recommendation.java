package io.github.arvide.movierecommender.dto;

public record Recommendation(int movieId, String movieTitle, double prediction) {
}
