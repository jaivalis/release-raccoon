package com.raccoon.scrape.dto;

import java.time.LocalDate;

public record ReleaseDto(String name,
                         String type,
                         String spotifyUri,
                         String musicbrainzId,
                         LocalDate releasedOn) {
}
