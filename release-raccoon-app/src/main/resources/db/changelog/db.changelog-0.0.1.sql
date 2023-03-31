--liquibase formatted sql

--changeset jaivalis:1
create table if not exists raccoondb.Scrape
(
    id                      bigint auto_increment primary key,
    completeDate            datetime null,
    create_date             datetime null,
    isComplete              bit      null,
    modify_date             datetime null,
    releaseCount            int      null,
    releasesFromMusicbrainz bigint   null,
    releasesFromSpotify     bigint   null,
    relevantReleases        bigint   null,
    usersNotified           bigint   null
);
