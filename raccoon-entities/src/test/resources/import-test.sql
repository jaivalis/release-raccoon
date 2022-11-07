# create database raccoondb;
# use raccoondb;
#
# create table Artist
# (
#     artistId      bigint auto_increment
#         primary key,
#     name          varchar(300)               null,
#     spotifyUri    varchar(255)               null,
#     lastfmUri     varchar(255)               null,
#     create_date   datetime default curdate() null,
#     musicbrainzId varchar(255)               null
# )
#     engine = InnoDB;
#
# create index IDXaoa6nrkwpys16f2yvoqiw8nk6
#     on Artist (spotifyUri);
#
# create table Releases
# (
#     releaseId     bigint       not null
#         primary key,
#     name          varchar(255) null,
#     releasedOn    date         null,
#     spotifyUri    varchar(255) null,
#     type          varchar(255) null,
#     musicbrainzId varchar(255) null,
#     constraint UK_qe08hf3mmcyhlore01g4f5vsd
#         unique (spotifyUri)
# )
#     engine = InnoDB;
#
# create table ArtistRelease
# (
#     release_id bigint not null,
#     artist_id  bigint not null,
#     primary key (artist_id, release_id),
#     constraint FK27mlse6dudk4xs9k2hfx8p4gg
#         foreign key (artist_id) references Artist (artistId),
#     constraint FK76o28jbj8nefc724kfiscvenk
#         foreign key (release_id) references Releases (releaseId)
# )
#     engine = InnoDB;
#
# create index IDXqe08hf3mmcyhlore01g4f5vsd
#     on Releases (spotifyUri);
#
# create table RaccoonUser
# (
#     id                bigint       not null
#         primary key,
#     create_date       datetime     null,
#     email             varchar(255) null,
#     lastLastFmScrape  datetime     null,
#     lastNotified      date         null,
#     lastSpotifyScrape datetime     null,
#     lastfmUsername    varchar(255) null,
#     modify_date       datetime     null,
#     spotifyEnabled    bit          null,
#     username          varchar(255) null,
#     constraint UK_e6gkqunxajvyxl5uctpl2vl2p
#         unique (email)
# )
#     engine = InnoDB;
#
# create index IDXe6gkqunxajvyxl5uctpl2vl2p
#     on RaccoonUser (email);
#
# create table UserArtist
# (
#     hasNewRelease bit    null,
#     weight        float  null,
#     user_id       bigint not null,
#     artist_id     bigint not null,
#     primary key (artist_id, user_id),
#     constraint FKaahjoyl914ej2xddcp3ae972v
#         foreign key (artist_id) references Artist (artistId),
#     constraint FKhmmh9wxt9occrvvc58w4ror7m
#         foreign key (user_id) references RaccoonUser (id)
# )
#     engine = InnoDB;
#
# create table flyway_schema_history
# (
#     installed_rank int                                   not null
#         primary key,
#     version        varchar(50)                           null,
#     description    varchar(200)                          not null,
#     type           varchar(20)                           not null,
#     script         varchar(1000)                         not null,
#     checksum       int                                   null,
#     installed_by   varchar(100)                          not null,
#     installed_on   timestamp default current_timestamp() not null,
#     execution_time int                                   not null,
#     success        tinyint(1)                            not null
# )
#     engine = InnoDB;
#
# create index flyway_schema_history_s_idx
#     on flyway_schema_history (success);
#
# create table hibernate_sequence
# (
#     next_val bigint null
# )
#     engine = InnoDB;
#
