INSERT INTO RaccoonUser
    (user_id, email)
VALUES
    (100, 'user100@mail.com');


INSERT INTO Artist
    (artistId, name, musicbrainzId)
VALUES
    (100, 'existentArtist', '0000000000'),
    (200, 'another-existent-artist', 'existent-artist-musicbrainzId')
;


INSERT INTO Releases
    (releaseId, name, type)
VALUES
    (100, 'newRelease', 'ALBUM');


INSERT INTO UserArtist
    (user_id, artist_id)
VALUES
    (100, 100),
    (100, 200);

INSERT INTO ArtistRelease
    (artist_id, release_id)
VALUES
    (100, 100);
