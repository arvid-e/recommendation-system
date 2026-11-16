package io.github.arvide.movierecommender.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.arvide.movierecommender.dto.Recommendation;
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

    /**
     * Find recommendations for a target user using Euclidean distance or Pearson
     * correlation formula.
     *
     * @param userId          - User to find recommendations for.
     * @param numberOfResults - How many results to show.
     * @param formula         - Which formula to calculate similary score with.
     * @return - List of recommendations.
     */
    public List<Recommendation> getRecommendationsFor(Integer userId, Integer numberOfResults, String formula) {
        userRepo.findById(userId).orElseThrow();

        final int MIN_SHARED = formula.equals("pearson") ? 3 : 2;

        // Save all ratings by the target user
        HashMap<Integer, Double> targetRatings = new HashMap<>();
        for (Rating rating : ratingRepo.findByUserId(userId)) {
            targetRatings.put(rating.getMovieId(), rating.getRating());
        }

        // Get all users that has rated at least one same movie
        List<User> candidates = userRepo.findUsersSharingMovieWith(userId);
        HashMap<Integer, List<Rating>> candidateRatings = new HashMap<>();
        HashMap<Integer, Double> userSimilarityMap = new HashMap<>();

        // Compute similarity for each candidate
        for (User user : candidates) {
            if (user.getId().equals(userId))
                continue;

            List<Double> targetRatingList = new ArrayList<>();
            List<Double> candidateRatingList = new ArrayList<>();

            List<Rating> ratings = ratingRepo.findByUserId(user.getId());
            candidateRatings.put(user.getId(), ratings);
            for (Rating rating : ratings) {
                Double targetRating = targetRatings.get(rating.getMovieId());

                if (targetRating != null) {
                    targetRatingList.add(targetRating);
                    candidateRatingList.add(rating.getRating());
                }
            }

            if (targetRatingList.size() < MIN_SHARED)
                continue;

            double similarity = switch (formula) {
                case "pearson" -> pearson(targetRatingList, candidateRatingList);
                default -> euclidean(targetRatingList, candidateRatingList);
            };

            if (similarity <= 0)
                continue;

            userSimilarityMap.put(user.getId(), similarity);
        }

        final int K = 20; // how many neighbours to use
        final int MIN_RATERS = 2; // movie must be rated by at least this many neighbours

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
        List<Recommendation> recommendations = new ArrayList<>();
        for (Integer movieId : weightedSum.keySet()) {
            if (raterCount.get(movieId) < MIN_RATERS)
                continue;
            double prediction = weightedSum.get(movieId) / similaritySum.get(movieId);
            String title = movieRepo.findById(movieId).orElseThrow().getTitle();
            recommendations.add(new Recommendation(movieId, title, prediction));
        }

        recommendations.sort((a, b) -> Double.compare(b.prediction(), a.prediction()));

        return recommendations.stream().limit(numberOfResults).toList();
    }

    private double euclidean(List<Double> targetRatings, List<Double> candidateRatings) {
        double sumOfSquares = 0;
        for (int i = 0; i < targetRatings.size(); i++) {
            double diff = targetRatings.get(i) - candidateRatings.get(i);
            sumOfSquares += diff * diff;
        }

        return 1 / (1 + Math.sqrt(sumOfSquares));
    }

    private double pearson(List<Double> targetRatings, List<Double> candidateRatings) {
        int n = targetRatings.size();
        double meanTarget = targetRatings.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double meanCandidate = candidateRatings.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        double numerator = 0, sumSqX = 0, sumSqY = 0;
        for (int i = 0; i < n; i++) {
            double dx = targetRatings.get(i) - meanTarget;
            double dy = candidateRatings.get(i) - meanCandidate;
            numerator += dx * dy;
            sumSqX += dx * dx;
            sumSqY += dy * dy;
        }

        double denominator = Math.sqrt(sumSqX) * Math.sqrt(sumSqY);
        if (denominator == 0)
            return 0;
        return numerator / denominator;
    }
}
