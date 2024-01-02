INSERT INTO Artist
    (artistId, name, create_date, spotifyUri)
VALUES
    (500, 'existed before the scrape', (SUBDATE(CURDATE(), 7)), 'uri500'),
    (501, 'also existed before the scrape', (SUBDATE(CURDATE(), 7)), 'uri501');

INSERT INTO Releases
    (releaseId, name, type, releasedOn, spotifyUri)
VALUES
    (500, 'newRelease', 'ALBUM', (SUBDATE(CURDATE(), 1)), 'spotify:album:500');

INSERT INTO ArtistRelease
    (artist_id, release_id)
VALUES
    (500, 500);

INSERT INTO RaccoonUser
    (user_id, email)
VALUES
    (500, 'user500@mail.com'),
    (501, 'user501@mail.com');

INSERT INTO UserArtist
    (user_id, artist_id, hasNewRelease, weight)
VALUES
    (500, 501, false, 0.69),
    (501, 501, false, 0.5);

