-- Create the database and user if they don't exist
DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_catalog.pg_roles WHERE rolname = 'raccoon') THEN
            CREATE ROLE raccoon LOGIN;
        END IF;
    END
$$;

DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'raccoondb') THEN
            CREATE DATABASE raccoondb;
        END IF;
    END
$$;

-- Grant all privileges on the database to the user
GRANT ALL PRIVILEGES ON DATABASE raccoondb TO raccoon;

-- Connect to the database
\connect raccoondb;

-- Create tables
CREATE TABLE Artist (
                        artistId SERIAL PRIMARY KEY,
                        create_date TIMESTAMP,
                        lastfmUri VARCHAR(255),
                        musicbrainzId VARCHAR(255),
                        name VARCHAR(300),
                        spotifyUri VARCHAR(255)
);

CREATE TABLE ArtistRelease (
                               release_id BIGINT NOT NULL,
                               artist_id BIGINT NOT NULL,
                               PRIMARY KEY (artist_id, release_id)
);

CREATE TABLE RaccoonUser (
                             user_id SERIAL PRIMARY KEY,
                             create_date TIMESTAMP,
                             email VARCHAR(255) UNIQUE,
                             lastLastFmScrape TIMESTAMP,
                             lastNotified DATE,
                             lastSpotifyScrape TIMESTAMP,
                             lastfmUsername VARCHAR(255),
                             modify_date TIMESTAMP,
                             spotifyEnabled BOOLEAN,
                             username VARCHAR(255)
);

CREATE TABLE Releases (
                          releaseId SERIAL PRIMARY KEY,
                          musicbrainzId VARCHAR(255),
                          name VARCHAR(300),
                          releasedOn DATE,
                          spotifyUri VARCHAR(255),
                          type VARCHAR(255)
);

CREATE TABLE UserArtist (
                            hasNewRelease BOOLEAN,
                            weight FLOAT,
                            user_id BIGINT NOT NULL,
                            artist_id BIGINT NOT NULL,
                            PRIMARY KEY (artist_id, user_id)
);

-- Create indexes
CREATE INDEX ArtistSpotifyUri_idx ON Artist (spotifyUri);
CREATE INDEX email_idx ON RaccoonUser (email);
CREATE INDEX ReleaseSpotifyUri_idx ON Releases (spotifyUri);

-- Add foreign key constraints
ALTER TABLE ArtistRelease
    ADD CONSTRAINT FK_ArtistRelease_Release
        FOREIGN KEY (release_id)
            REFERENCES Releases (releaseId);

ALTER TABLE ArtistRelease
    ADD CONSTRAINT FK_ArtistRelease_Artist
        FOREIGN KEY (artist_id)
            REFERENCES Artist (artistId);

ALTER TABLE UserArtist
    ADD CONSTRAINT FK_UserArtist_User
        FOREIGN KEY (user_id)
            REFERENCES RaccoonUser (user_id);

ALTER TABLE UserArtist
    ADD CONSTRAINT FK_UserArtist_Artist
        FOREIGN KEY (artist_id)
            REFERENCES Artist (artistId);

-- Create sequence table
CREATE TABLE hibernate_sequence (
                                    next_val BIGINT
);
