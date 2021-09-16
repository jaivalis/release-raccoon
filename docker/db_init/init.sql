-- CREATE DATABASE IF NOT EXISTS `${DB_NAME}`;
-- GRANT ALL ON `${DB_NAME}`.* TO '${MYSQL_USER}'@'%';

CREATE USER IF NOT EXISTS 'raccoon'@'%';
CREATE DATABASE IF NOT EXISTS `raccoondb`;
GRANT ALL PRIVILEGES ON `raccoondb`.* TO 'raccoon'@'%';

use raccoondb;

create table Artist
(
    artistId   bigint auto_increment
        primary key,
    lastfmUri  varchar(255) null,
    name       varchar(255) null,
    spotifyUri varchar(255) null
);

create index IDXaoa6nrkwpys16f2yvoqiw8nk6
    on Artist (spotifyUri);

create table Releases
(
    releaseId  bigint       not null
        primary key,
    name       varchar(255) null,
    releasedOn date         null,
    spotifyUri varchar(255) null,
    type       varchar(255) null,
    constraint UK_qe08hf3mmcyhlore01g4f5vsd
        unique (spotifyUri)
);

create table ArtistRelease
(
    release_id bigint not null,
    artist_id  bigint not null,
    primary key (artist_id, release_id),
    constraint FK27mlse6dudk4xs9k2hfx8p4gg
        foreign key (artist_id) references Artist (artistId),
    constraint FK76o28jbj8nefc724kfiscvenk
        foreign key (release_id) references Releases (releaseId)
);

create index IDXqe08hf3mmcyhlore01g4f5vsd
    on Releases (spotifyUri);

create table User
(
    id                bigint       not null
        primary key,
    create_date       datetime     null,
    email             varchar(255) null,
    lastLastFmScrape  datetime     null,
    lastNotified      date         null,
    lastSpotifyScrape datetime     null,
    lastfmUsername    varchar(255) null,
    modify_date       datetime     null,
    spotifyEnabled    bit          null,
    username          varchar(255) null,
    constraint UK_e6gkqunxajvyxl5uctpl2vl2p
        unique (email)
);

create index IDXe6gkqunxajvyxl5uctpl2vl2p
    on User (email);

create table UserArtist
(
    hasNewRelease bit    null,
    weight        float  null,
    user_id       bigint not null,
    artist_id     bigint not null,
    primary key (artist_id, user_id),
    constraint FKaahjoyl914ej2xddcp3ae972v
        foreign key (artist_id) references Artist (artistId),
    constraint FKhmmh9wxt9occrvvc58w4ror7m
        foreign key (user_id) references User (id)
);

create table hibernate_sequence
(
    next_val bigint null
);

