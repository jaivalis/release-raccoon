INSERT INTO RaccoonUser
    (user_id, email)
VALUES
    (200, 'user200@mail.com'),
    (300, 'user300@mail.com');


INSERT INTO Artist
    (artistId, name)
VALUES
    (300, 'existentArtist2');


INSERT INTO Releases
    (releaseId, name, type, releasedOn, spotifyUri)
VALUES
    (300, 'new-release-to-notify', 'ALBUM', (SUBDATE(CURDATE(), 1)), 'spotify:album:zzzzzz');


INSERT INTO ArtistRelease
(artist_id, release_id)
VALUES
    (300, 300);


INSERT INTO UserArtist
    (user_id, artist_id, hasNewRelease)
VALUES
    (300, 300, true),
    (200, 300, false);
