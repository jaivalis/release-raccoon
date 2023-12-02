INSERT INTO
    RaccoonUser
    (user_id, email)
VALUES
    (200, 'user200@mail.com');

INSERT INTO
    Artist
    (artistId, name, create_date)
VALUES
    (100, 'existentArtist', (SUBDATE(CURDATE(), 1)));

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


