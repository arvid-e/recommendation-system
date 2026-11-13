CREATE TABLE ratings(
    user_id   INT NOT NULL,
    movie_id  INT NOT NULL,
    rating    DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (user_id, movie_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (movie_id) REFERENCES movies(id)
)