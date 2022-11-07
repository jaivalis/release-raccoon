INSERT INTO
    RaccoonUser
    (user_id, email)
VALUES
    (100, 'user100@mail.com');

INSERT INTO
    Artist
    (name)
VALUES
    ('led zeppeling'),
    ('Zapp Franka'),
    ('vangelio'),
    ('krs-two'),
    ('me-roy'),
    ('philip grass'),
    ('kanye east'),
    ('min Romeo'),
    ('Inner Kamoze'),
    ('Beta Blondy'),
    ('philip stone');





INSERT INTO
    RaccoonUser
    (user_id, email)
VALUES
    (200, 'user200@mail.com');

INSERT INTO
    Artist
    (artistId, name)
VALUES
    (100, 'existentArtist');

# INSERT INTO
#     Releases
#     (releaseId, name, type)
# VALUES
#     (100, 'newRelease', 'ALBUM');
INSERT INTO
    Releases
    (releaseId, name, type, releasedOn, spotifyUri)
VALUES
    (100, 'new-release-to-notify', 'ALBUM', (SUBDATE(CURDATE(), 1)), 'spotify:album:zzzzzz');

INSERT INTO
    UserArtist
    (user_id, artist_id)
VALUES
    (100, 100);

INSERT INTO
    ArtistRelease
    (artist_id, release_id)
VALUES
    (100, 100);





INSERT INTO
    Releases
    (releaseId, name, type, releasedOn, spotifyUri)
VALUES
    (300, 'new-release-to-notify', 'ALBUM', (SUBDATE(CURDATE(), 1)), 'spotify:album:xxxxx');

INSERT INTO
    RaccoonUser
    (user_id, email)
VALUES
    (300, 'user300@mail.com');

INSERT INTO
    Artist
    (artistId, name)
VALUES
    (300, 'existentArtist2');

INSERT INTO
    UserArtist
    (user_id, artist_id, hasNewRelease)
VALUES
    (300, 300, true),
    (200, 300, false);

INSERT INTO
    ArtistRelease
    (artist_id, release_id)
VALUES
    (300, 300);





INSERT INTO
    Artist
    (artistId, name, spotifyUri)
VALUES
    (400, 'existentArtist4', 'uri4');

INSERT INTO
    Releases
    (releaseId, name, type, releasedOn, spotifyUri)
VALUES
    (400, 'new-release-to-notify', 'ALBUM', (SUBDATE(CURDATE(), 1)), 'spotify:album:yyyyyy');

INSERT INTO
    RaccoonUser
    (user_id, email)
VALUES
    (400, 'user400@mail.com');

INSERT INTO
    ArtistRelease
    (artist_id, release_id)
VALUES
    (400, 400);

INSERT INTO
    UserArtist
    (user_id, artist_id, hasNewRelease)
VALUES
    (400, 400, false);
