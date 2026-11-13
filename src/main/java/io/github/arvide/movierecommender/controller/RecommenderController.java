package io.github.arvide.movierecommender.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.github.arvide.movierecommender.dto.Recommendation;
import io.github.arvide.movierecommender.service.RecommendationService;

@RestController
@RequestMapping("/api/v1/users")
public class RecommenderController {

    private final RecommendationService recommendationService;

    public RecommenderController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/{userId}/recommendations")
    public List<Recommendation> getRecommendations(@PathVariable Integer userId) {
        return recommendationService.getRecommendationsFor(userId);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NoSuchElementException e) {
        return Map.of("error", "Not found");
    }
}