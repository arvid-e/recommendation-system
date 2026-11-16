package io.github.arvide.movierecommender.dto;

import java.util.List;

import io.github.arvide.movierecommender.entity.Rating;

public record Neighbour(int userId, double similarity, List<Rating> ratings) {
}