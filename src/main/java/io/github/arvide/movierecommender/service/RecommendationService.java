package io.github.arvide.movierecommender.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import io.github.arvide.movierecommender.dto.MatchingUser;
import io.github.arvide.movierecommender.dto.Neighbour;
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
    private static final int K = 20; // how many neighbours to use

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
    public List<Recommendation> getRecommendationsFor(Integer userId, int numberOfResults, String formula) {
        userRepo.findById(userId).orElseThrow();

        Map<Integer, Double> targetRatings = getRatingsMap(userId);
        List<Neighbour> similarities = calculateSimilarities(userId, targetRatings, formula);
        List<Neighbour> neighbours = topNeighbours(similarities, K);

        return predict(neighbours, targetRatings).stream()
                .limit(numberOfResults)
                .toList();
    }

    /**
     * Saves all ratings for a user in a ratings map.
     *
     * @param userId - The user to get ratings from.
     * @return - Map with a user's ratings.
     */
    private Map<Integer, Double> getRatingsMap(Integer userId) {
        HashMap<Integer, Double> targetRatings = new HashMap<>();
        for (Rating rating : ratingRepo.findByUserId(userId)) {
            targetRatings.put(rating.getMovieId(), rating.getRating());
        }

        return targetRatings;
    }

    /**
     * Calculate similarity scores for users and their movie ratings.
     *
     * @param userId        - The user id of target user.
     * @param targetRatings - The target user's ratings.
     * @param formula       - Which similarity calculation formula to use.
     * @return - List of neighbour users and their similarity scores.
     */
    private List<Neighbour> calculateSimilarities(Integer userId, Map<Integer, Double> targetRatings,
            String formula) {
        final int MIN_SHARED = formula.equals("pearson") ? 3 : 2;

        List<Neighbour> neighbours = new ArrayList<>();

        // Get all users that has rated at least one same movie
        List<User> candidates = userRepo.findUsersSharingMovieWith(userId);

        // Compute similarity for each candidate
        for (User user : candidates) {
            if (user.getId().equals(userId))
                continue;

            List<Double> targetRatingList = new ArrayList<>();
            List<Double> candidateRatingList = new ArrayList<>();

            List<Rating> ratings = ratingRepo.findByUserId(user.getId());
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

            Neighbour neighbour = new Neighbour(user.getId(), user.getName(), similarity, ratings);
            neighbours.add(neighbour);

        }

        return neighbours;
    }

    /**
     * Gets a sorted list of the top similar neighbours.
     *
     * @param similarities - Neighbours with calculated similarities.
     * @param k            - How many neighbours to use.
     * @return - List of the top similar neighbours.
     */
    private List<Neighbour> topNeighbours(List<Neighbour> similarities, int k) {
        return similarities.stream()
                .sorted(Comparator.comparingDouble(Neighbour::similarity).reversed())
                .limit(k)
                .toList();
    }

    /**
     * Calculate score predictions and returns movie recommendations for a user.
     *
     * @param neighbours    - Neighbours with calculated similarities.
     * @param targetRatings - The ratings of the target user.
     * @return - List of movie recommendations and their predicted scores.
     */
    private List<Recommendation> predict(List<Neighbour> neighbours, Map<Integer, Double> targetRatings) {
        Map<Integer, Double> weightedSum = new HashMap<>();
        Map<Integer, Double> similaritySum = new HashMap<>();
        Map<Integer, Integer> raterCount = new HashMap<>();

        for (Neighbour neighbour : neighbours) {
            for (Rating rating : neighbour.ratings()) {
                int movieId = rating.getMovieId();

                // skip movies target has seen
                if (targetRatings.containsKey(movieId))
                    continue;

                weightedSum.merge(movieId, neighbour.similarity() * rating.getRating(), Double::sum);
                similaritySum.merge(movieId, neighbour.similarity(), Double::sum);
                raterCount.merge(movieId, 1, Integer::sum);
            }
        }

        return weightedSum.keySet().stream()
                .map(id -> new Recommendation(id,
                        movieRepo.findById(id).orElseThrow().getTitle(),
                        weightedSum.get(id) / similaritySum.get(id)))
                .sorted(Comparator.comparingDouble(Recommendation::prediction).reversed())
                .toList();
    }

    /**
     * Calculate similarity using eaclidean distance.
     *
     * Takes the inverse of the average difference of the ratings fo the target user
     * and the candidate user.
     *
     * @param targetRatings    - The target user's ratings.
     * @param candidateRatings - The candidate user's ratings.
     * @return - A similarity rating.
     */
    private double euclidean(List<Double> targetRatings, List<Double> candidateRatings) {
        double sumOfSquares = 0;
        for (int i = 0; i < targetRatings.size(); i++) {
            double diff = targetRatings.get(i) - candidateRatings.get(i);
            sumOfSquares += diff * diff;
        }

        return 1 / (1 + sumOfSquares);
    }

    /**
     * Calculate a similarity rating using pearson correlation formula.
     *
     * @param targetRatings    - The target user's ratings.
     * @param candidateRatings - The candidate user's ratings.
     * @return - A similarity rating.
     */
    private double pearson(List<Double> targetRatings, List<Double> candidateRatings) {
        double meanTarget = targetRatings.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double meanCandidate = candidateRatings.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        double numerator = 0, sumSqX = 0, sumSqY = 0;
        for (int i = 0; i < targetRatings.size(); i++) {
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

    /**
     * Get the top matching users for the target user.
     * 
     * @param userId          - Target user.
     * @param numberOfResults - How many results to return.
     * @param formula         - Which formula to use.
     * @return
     */
    public List<MatchingUser> getMatchingUsersFor(Integer userId, int numberOfResults, String formula) {
        userRepo.findById(userId).orElseThrow();

        Map<Integer, Double> targetRatings = getRatingsMap(userId);
        List<Neighbour> neighbours = calculateSimilarities(userId, targetRatings, formula);

        return topNeighbours(neighbours, numberOfResults).stream()
                .map(n -> new MatchingUser(n.userId(), n.name(), n.similarity()))
                .toList();
    }
}
