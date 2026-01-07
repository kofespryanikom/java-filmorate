MERGE INTO rating (rating_id, rating) VALUES
    (1, 'G'),
    (2, 'PG'),
    (3, 'PG_13'),
    (4, 'R'),
    (5, 'NC_17');

MERGE INTO genres (genre_id, genre) VALUES
    (1, 'COMEDY'),
    (2, 'DRAMA'),
    (3, 'CARTOON'),
    (4, 'THRILLER'),
    (5, 'DOCUMENTARY'),
    (6, 'ACTION_MOVIE');