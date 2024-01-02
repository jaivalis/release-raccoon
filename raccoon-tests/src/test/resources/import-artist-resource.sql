INSERT INTO RaccoonUser
    (user_id, email)
VALUES
    (100, 'user100@mail.com'),
    (200, 'user200@mail.com'),
    (300, 'user300@mail.com');


INSERT INTO Releases
    (releaseId, name, type, releasedOn, spotifyUri)
VALUES
    (300, 'new-release-to-notify', 'ALBUM', (SUBDATE(CURDATE(), 1)), 'spotify:album:xxxxx');

INSERT INTO Artist
    (artistId, name, create_date)
VALUES
    (300, 'existentArtist2', (SUBDATE(CURDATE(), 1)));

INSERT INTO UserArtist
    (user_id, artist_id, hasNewRelease)
VALUES
    (300, 300, true),
    (200, 300, false);

INSERT INTO ArtistRelease
    (artist_id, release_id)
VALUES
    (300, 300);


INSERT INTO Artist
    (artistId, name, create_date, spotifyUri)
VALUES
    (400, 'existentArtist4', (SUBDATE(CURDATE(), 1)), 'uri4');

INSERT INTO Releases
    (releaseId, name, type, releasedOn, spotifyUri)
VALUES
    (400, 'new-release-to-notify', 'ALBUM', (SUBDATE(CURDATE(), 1)), 'spotify:album:yyyyyy');

INSERT INTO RaccoonUser
    (user_id, email)
VALUES
    (400, 'user400@mail.com');

INSERT INTO ArtistRelease
    (artist_id, release_id)
VALUES
    (400, 400);

INSERT INTO UserArtist
    (user_id, artist_id, hasNewRelease)
VALUES
    (400, 400, false);
