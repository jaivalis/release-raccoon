package com.raccoon.release.dto;

import com.raccoon.entity.Scrape;

import java.util.List;

public record ReleaseScrapeResponse(Scrape scrape, List<ReleaseDto> releases) {}
