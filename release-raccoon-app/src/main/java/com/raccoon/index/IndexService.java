package com.raccoon.index;

import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.ReleaseRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.qute.Template;

@ApplicationScoped
public class IndexService {

    ArtistRepository artistRepository;
    ReleaseRepository releaseRepository;

    @Inject
    Template index;

    @Inject
    public IndexService(final ArtistRepository artistRepository,
                        final ReleaseRepository releaseRepository,
                        final Template index) {
        this.artistRepository = artistRepository;
        this.releaseRepository = releaseRepository;
        this.index = index;
    }

    public String getTemplateInstance() {
        return index.data(
                "artistCount", String.valueOf(artistRepository.count()),
                "releaseCount", String.valueOf(releaseRepository.count())
        ).render();
    }

}
