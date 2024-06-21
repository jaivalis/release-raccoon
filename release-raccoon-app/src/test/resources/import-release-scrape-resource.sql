INSERT INTO RaccoonUser
    (user_id, email)
VALUES
    (100, 'user100@mail.com');


INSERT INTO Artist
    (artistId, name)
VALUES
    (100, 'existentArtist');


INSERT INTO Releases
    (releaseId, name, type)
VALUES
    (100, 'newRelease', 'ALBUM');


INSERT INTO UserArtist
    (user_id, artist_id)
VALUES
    (100, 100);

INSERT INTO ArtistRelease
    (artist_id, release_id)
VALUES
    (100, 100);
