package io.github.arvide.movierecommender.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.arvide.movierecommender.entity.Rating;
import io.github.arvide.movierecommender.entity.RatingId;

public interface RatingRepository extends JpaRepository<Rating, RatingId> {

}
