INSERT INTO RaccoonUser
    (user_id, email)
VALUES
    (100, 'user100@mail.com');

INSERT INTO Artist
    (artistId, name)
VALUES
    (100, 'led zeppeling'),
    (200, 'Zapp Franka'),
    (300, 'vangelio'),
    (400, 'krs-two'),
    (500, 'me-roy'),
    (600, 'philip grass'),
    (700, 'kanye east'),
    (800, 'min Romeo'),
    (900, 'Inner Kamoze'),
    (101, 'Beta Blondy'),
    (102, 'philip stone');

INSERT INTO UserArtist
    (user_id, artist_id, hasNewRelease)
VALUES
    (100, 100, false);
