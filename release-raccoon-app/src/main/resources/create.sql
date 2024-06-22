create table if not exists Artist
(
    artistId      bigint auto_increment
        primary key,
    create_date   datetime(6)  null,
    name          varchar(300) null,
    lastfmUri     varchar(255) null,
    musicbrainzId varchar(255) null,
    spotifyUri    varchar(255) null
);

create index ArtistSpotifyUri_idx
    on Artist (spotifyUri);

create table if not exists ArtistRelease
(
    artist_id  bigint not null,
    release_id bigint not null,
    primary key (artist_id, release_id)
);

create table if not exists DATABASECHANGELOG
(
    ID            varchar(255) not null,
    AUTHOR        varchar(255) not null,
    FILENAME      varchar(255) not null,
    DATEEXECUTED  datetime     not null,
    ORDEREXECUTED int          not null,
    EXECTYPE      varchar(10)  not null,
    MD5SUM        varchar(35)  null,
    DESCRIPTION   varchar(255) null,
    COMMENTS      varchar(255) null,
    TAG           varchar(255) null,
    LIQUIBASE     varchar(20)  null,
    CONTEXTS      varchar(255) null,
    LABELS        varchar(255) null,
    DEPLOYMENT_ID varchar(10)  null
);

create table if not exists DATABASECHANGELOGLOCK
(
    ID          int          not null
        primary key,
    LOCKED      tinyint(1)   not null,
    LOCKGRANTED datetime     null,
    LOCKEDBY    varchar(255) null
);

create table if not exists RaccoonUser
(
    lastNotified      date         null,
    spotifyEnabled    bit          null,
    create_date       datetime(6)  null,
    lastLastFmScrape  datetime(6)  null,
    lastSpotifyScrape datetime(6)  null,
    modify_date       datetime(6)  null,
    user_id           bigint auto_increment
        primary key,
    email             varchar(255) null,
    lastfmUsername    varchar(255) null,
    username          varchar(255) null,
    constraint UK_88ntf56dcx5rmi0y7l3whp6cd
        unique (email)
);

create index email_idx
    on RaccoonUser (email);

create table if not exists Releases
(
    releasedOn    date         null,
    releaseId     bigint auto_increment
        primary key,
    name          varchar(300) null,
    musicbrainzId varchar(255) null,
    spotifyUri    varchar(255) null,
    type          varchar(255) null
);

create index ReleaseSpotifyUri_idx
    on Releases (spotifyUri);

create table if not exists Scrape
(
    isComplete              bit         null,
    releaseCount            int         null,
    completeDate            datetime(6) null,
    create_date             datetime(6) null,
    id                      bigint auto_increment
        primary key,
    modify_date             datetime(6) null,
    releasesFromMusicbrainz bigint      null,
    releasesFromSpotify     bigint      null,
    relevantReleases        bigint      null,
    usersNotified           bigint      null
);

create table if not exists UserArtist
(
    hasNewRelease bit    null,
    weight        float  null,
    artist_id     bigint not null,
    user_id       bigint not null,
    primary key (artist_id, user_id),
    constraint FKaahjoyl914ej2xddcp3ae972v
        foreign key (artist_id) references Artist (artistId),
    constraint FKkb7vbhl8onwbnkjj9ogpnxq2u
        foreign key (user_id) references RaccoonUser (user_id)
);

create table if not exists hibernate_sequence
(
    next_val bigint null
);

