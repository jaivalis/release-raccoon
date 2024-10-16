INSERT INTO RaccoonUser
    (user_id, email, lastNotified)
VALUES
    (200, 'user200@mail.com', null),
    (300, 'user300@mail.com', null),
    (400, 'user400@mail.com', CURRENT_DATE - INTERVAL '1' DAY),
    (500, 'user500@mail.com', CURRENT_DATE - INTERVAL '1' DAY);

INSERT INTO UserSettings
    (user_id, unsubscribed, notifyIntervalDays)
VALUES
    (300, 'false', '1'),
    (400, 'false', '7'),
    (500, 'true', '1');

INSERT INTO Artist
    (artistId, name)
VALUES
    (300, 'existentArtist2');


INSERT INTO Releases
    (releaseId, name, type, releasedOn, spotifyUri)
VALUES
    (300, 'new-release-to-notify', 'ALBUM', CURRENT_DATE - INTERVAL '1' DAY, 'spotify:album:zzzzzz');


INSERT INTO ArtistRelease
    (artist_id, release_id)
VALUES
    (300, 300);


INSERT INTO UserArtist
    (user_id, artist_id, hasNewRelease)
VALUES
    (300, 300, true),
    (400, 300, true),
    (500, 300, true),
    (200, 300, false);
