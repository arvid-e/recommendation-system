package io.github.arvide.movierecommender.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import io.github.arvide.movierecommender.entity.Movie;
import io.github.arvide.movierecommender.entity.Rating;
import io.github.arvide.movierecommender.entity.User;
import io.github.arvide.movierecommender.repository.MovieRepository;
import io.github.arvide.movierecommender.repository.RatingRepository;
import io.github.arvide.movierecommender.repository.UserRepository;

@Service
public class RecommendationService {
    private final MovieRepository movieRepo;
    private final UserRepository userRepo;
    private final RatingRepository ratingRepo;

    public RecommendationService(MovieRepository movieRepo, UserRepository userRepo, RatingRepository ratingRepo) {
        this.movieRepo = movieRepo;
        this.userRepo = userRepo;
        this.ratingRepo = ratingRepo;
    }

    // 1. Load the target’s ratings.
    // 2. Find users who rated the same movies.
    // 3. Score each user: shared movies → differences → squares → sum → square root → 1 / (1 + distance).
    // 4. Keep the most similar users.
    // 5. Collect their ratings for movies the target hasn’t seen, weighted by similarity.
    // 6. Predict each movie’s rating as the weighted average.
    // 7. Drop movies with too few raters or a low prediction.
    // 8. Return the highest predicted movies.
    public List<Movie> getRecommendationFor(Integer userId) {
        userRepo.findById(userId).orElseThrow();

        // Save all ratings by the target user
        HashMap<Integer, Double> targetRatings = new HashMap<>();
        for (Rating rating : ratingRepo.findByUserId(userId)) {
            targetRatings.put(rating.getMovieId(), rating.getRating());
        }

        // Get all users that has rated at least one same movie
        List<User> candidates = userRepo.findUsersSharingMovieWith(userId);
        HashMap<Integer, List<Rating>> candidateRatings = new HashMap<>();
        HashMap<Integer, Double> userSimilarityMap = new HashMap<>();
        final int MIN_SHARED = 2;

        // Compute similarity for each candidate
        for (User user : candidates) {
            if (user.getId().equals(userId))
                continue;

            double sumOfSquares = 0;
            int shared = 0;

            List<Rating> ratings = ratingRepo.findByUserId(user.getId());
            candidateRatings.put(user.getId(), ratings);
            for (Rating rating : ratings) {
                Double targetRating = targetRatings.get(rating.getMovieId());

                if (targetRating != null) {
                    double diff = targetRating - rating.getRating();
                    sumOfSquares += diff * diff;
                    shared++;
                }
            }

            if (shared < MIN_SHARED)
                continue;

            double distance = Math.sqrt(sumOfSquares);
            double similarity = 1 / (1 + distance);
            userSimilarityMap.put(user.getId(), similarity);
        }

        final int K = 20; // how many neighbours to use
        final int MIN_RATERS = 2; // movie must be rated by at least this many neighbours
        final double MIN_SCORE = 3.5; // don't recommend below this predicted rating
        final int N = 10; // how many recommendations to return

        // Sort top k neighbours by similarity
        List<Map.Entry<Integer, Double>> neighbours = userSimilarityMap.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(K)
                .toList();

        // Accumulate weighted ratings for movies the target hasn't rated
        Map<Integer, Double> weightedSum = new HashMap<>();
        Map<Integer, Double> similaritySum = new HashMap<>();
        Map<Integer, Integer> raterCount = new HashMap<>();

        for (Map.Entry<Integer, Double> neighbour : neighbours) {
            double similarity = neighbour.getValue();
            for (Rating rating : candidateRatings.get(neighbour.getKey())) {
                int movieId = rating.getMovieId();

                // skip movies target has seen
                if (targetRatings.containsKey(movieId))
                    continue;

                weightedSum.merge(movieId, similarity * rating.getRating(), Double::sum);
                similaritySum.merge(movieId, similarity, Double::sum);
                raterCount.merge(movieId, 1, Integer::sum);
            }
        }

        // Predicted rating = weighted average, then filter
        Map<Integer, Double> predicted = new HashMap<>();
        for (Integer movieId : weightedSum.keySet()) {
            if (raterCount.get(movieId) < MIN_RATERS)
                continue;
            double prediction = weightedSum.get(movieId) / similaritySum.get(movieId);
            if (prediction >= MIN_SCORE) {
                predicted.put(movieId, prediction);
            }
        }

        // Sort by prediction, take top N, load movies in that order
        List<Integer> topMovieIds = predicted.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(N)
                .map(Map.Entry::getKey)
                .toList();

        Map<Integer, Movie> moviesById = movieRepo.findAllById(topMovieIds).stream()
                .collect(Collectors.toMap(Movie::getId, m -> m));

        return topMovieIds.stream().map(moviesById::get).toList();
    }

}
