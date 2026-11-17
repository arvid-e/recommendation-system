package io.github.arvide.movierecommender.dto;

import java.util.List;

import io.github.arvide.movierecommender.entity.Rating;

public record Neighbour(int userId, String name, double similarity, List<Rating> ratings) {
}