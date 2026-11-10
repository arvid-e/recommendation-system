CREATE TABLE ratings(
    PRIMARY KEY (user_id, movie_id),
    user_id   INT NOT NULL,
    movie_id  INT NOT NULL,
    rating    PRECISION NOT NULL 
)