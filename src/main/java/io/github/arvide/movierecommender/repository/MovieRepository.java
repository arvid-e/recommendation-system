package io.github.arvide.movierecommender.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.arvide.movierecommender.entity.Movie;;

public interface MovieRepository extends JpaRepository<Movie, UUID> {

}
