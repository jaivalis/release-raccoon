-- liquibase formatted sql

-- changeset jaivalis:2
create index if not exists ArtistName_idx on Artist (name);
