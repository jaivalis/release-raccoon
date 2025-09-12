INSERT INTO RaccoonUser
    (user_id, email)
VALUES
    (100, 'user100@mail.com');

INSERT INTO Artist
    (artistId, name, follower_count)
VALUES
    (100, 'led zeppeling', 1),
    (200, 'Zapp Franka', 0),
    (300, 'vangelio', 0),
    (400, 'krs-two', 0),
    (500, 'me-roy', 0),
    (600, 'philip grass', 0),
    (700, 'kanye east', 0),
    (800, 'min Romeo', 0),
    (900, 'Inner Kamoze', 0),
    (101, 'Beta Blondy', 0),
    (102, 'philip stone', 0)
;

INSERT INTO UserArtist
    (user_id, artist_id, hasNewRelease)
VALUES
    (100, 100, false);
