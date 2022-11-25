-- CREATE DATABASE IF NOT EXISTS `${DB_NAME}`;
-- GRANT ALL ON `${DB_NAME}`.* TO '${MYSQL_USER}'@'%';

CREATE USER IF NOT EXISTS 'raccoon'@'%';
CREATE DATABASE IF NOT EXISTS `raccoondb`;
GRANT ALL PRIVILEGES ON `raccoondb`.* TO 'raccoon'@'%';

use raccoondb;

create table Artist (
    artistId bigint auto_increment primary key,
    create_date timestamp,
    lastfmUri varchar(255),
    musicbrainzId varchar(255),
    name varchar(300),
    spotifyUri varchar(255),
    primary key (artistId)
);

create table ArtistRelease (
    release_id bigint not null,
    artist_id bigint not null,
    primary key (artist_id, release_id)
);

create table RaccoonUser (
    user_id bigint auto_increment primary key,
    create_date timestamp,
    email varchar(255),
    lastLastFmScrape timestamp,
    lastNotified date,
    lastSpotifyScrape timestamp,
    lastfmUsername varchar(255),
    modify_date timestamp,
    spotifyEnabled boolean,
    username varchar(255),
    primary key (user_id)
);

create table Releases (
    releaseId bigint auto_increment primary key,
    musicbrainzId varchar(255),
    name varchar(300),
    releasedOn date,
    spotifyUri varchar(255),
    type varchar(255),
    primary key (releaseId)
);

create table UserArtist (
    hasNewRelease boolean,
    weight float,
    user_id bigint not null,
    artist_id bigint not null,
    primary key (artist_id, user_id)
);
create index ArtistSpotifyUri_idx on Artist (spotifyUri);
create index email_idx on RaccoonUser (email);

alter table RaccoonUser
    add constraint UK_88ntf56dcx5rmi0y7l3whp6cd unique (email);
create index ReleaseSpotifyUri_idx on Releases (spotifyUri);

alter table ArtistRelease
    add constraint FK76o28jbj8nefc724kfiscvenk
        foreign key (release_id)
            references Releases;

alter table ArtistRelease
    add constraint FK27mlse6dudk4xs9k2hfx8p4gg
        foreign key (artist_id)
            references Artist;

alter table UserArtist
    add constraint FKkb7vbhl8onwbnkjj9ogpnxq2u
        foreign key (user_id)
            references RaccoonUser;

alter table UserArtist
    add constraint FKaahjoyl914ej2xddcp3ae972v
        foreign key (artist_id)
            references Artist;

create table hibernate_sequence
(
    next_val bigint null
);

