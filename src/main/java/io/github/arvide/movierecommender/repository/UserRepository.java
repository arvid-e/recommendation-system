package io.github.arvide.movierecommender.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.github.arvide.movierecommender.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("""
            select distinct u from User u
            join Rating r on r.userId = u.id
            where r.movieId in (
                select r2.movieId from Rating r2 where r2.userId = :userId
            )
            and u.id <> :userId
            """)
    Optional<List<User>> findUsersSharingMovieWith(Integer userId);
}
