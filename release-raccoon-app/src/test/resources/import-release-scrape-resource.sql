INSERT INTO RaccoonUser
    (user_id, email)
VALUES
    (100, 'user100@mail.com');


INSERT INTO Artist
    (artistId, name, musicbrainzId, follower_count)
VALUES
    (100, 'existentArtist', '0000000000', 1),
    (200, 'another-existent-artist', 'existent-artist-musicbrainzId', 1)
;


INSERT INTO Releases
    (releaseId, name, type, releasedOn)
VALUES
    (100, 'newRelease', 'ALBUM', CURRENT_DATE - INTERVAL '3' DAY);


INSERT INTO UserArtist
    (user_id, artist_id)
VALUES
    (100, 100),
    (100, 200);

INSERT INTO ArtistRelease
    (artist_id, release_id)
VALUES
    (100, 100);
