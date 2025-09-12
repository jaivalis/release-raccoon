-- liquibase formatted sql

-- changeset jaivalis:add_follower_count_to_artist
-- Add follower_count column to Artist table
ALTER TABLE Artist ADD COLUMN IF NOT EXISTS follower_count INTEGER NOT NULL DEFAULT 0;

-- Create index for efficient sorting and filtering by follower count
CREATE INDEX IF NOT EXISTS ArtistFollowerCount_idx ON Artist(follower_count DESC);

-- changeset jaivalis:populate_initial_follower_counts
-- Populate initial follower counts based on existing UserArtist relationships
UPDATE Artist a
SET follower_count = (
    SELECT COUNT(*)
    FROM UserArtist ua
    WHERE ua.artist_id = a.artistId
)
WHERE EXISTS (
    SELECT 1 FROM UserArtist ua WHERE ua.artist_id = a.artistId
);