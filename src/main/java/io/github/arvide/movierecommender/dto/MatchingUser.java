package io.github.arvide.movierecommender.dto;

public record MatchingUser(int userId, String name, double similarity) {
}
