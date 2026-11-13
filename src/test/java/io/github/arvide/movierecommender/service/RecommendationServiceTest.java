package io.github.arvide.movierecommender.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import io.github.arvide.movierecommender.dto.Recommendation;
import io.github.arvide.movierecommender.entity.Movie;
import io.github.arvide.movierecommender.entity.Rating;
import io.github.arvide.movierecommender.entity.User;
import io.github.arvide.movierecommender.repository.MovieRepository;
import io.github.arvide.movierecommender.repository.RatingRepository;
import io.github.arvide.movierecommender.repository.UserRepository;

@DataJpaTest
@Import(RecommendationService.class)
public class RecommendationServiceTest {

    @Autowired
    MovieRepository movieRepo;
    @Autowired
    UserRepository userRepo;
    @Autowired
    RatingRepository ratingRepo;
    @Autowired
    RecommendationService recommendationService;

    @Test
    public void recommendsMoviesLikedBySimilarUsers() {

        // Arrange
        // movies
        movie(1, "A");
        movie(2, "B");
        movie(3, "C");
        movie(4, "D");
        movie(5, "E");

        // users
        user(1);
        user(2);
        user(3);

        // ratings
        rating(1, 1, 2);
        rating(1, 2, 2.5);
        rating(1, 3, 3);
        rating(1, 4, 2);
        rating(1, 5, 1);

        rating(2, 1, 4);
        rating(2, 2, 3.5);

        rating(3, 1, 4);
        rating(3, 2, 2);
        rating(3, 3, 4.5);
        rating(3, 4, 3);
        rating(3, 5, 2.5);


        // Act
        List<Recommendation> result = recommendationService.getRecommendationsFor(2);

        // Assert
        assertEquals(3, result.size());
    }

    private void user(int id) {
        userRepo.save(new User(id, "user" + id));
    }

    private void movie(int id, String title) {
        movieRepo.save(new Movie(id, title, 2000));
    }

    private void rating(int userId, int movieId, double rating) {
        ratingRepo.save(new Rating(userId, movieId, rating));
    }
}