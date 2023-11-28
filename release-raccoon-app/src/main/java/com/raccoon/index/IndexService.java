package com.raccoon.index;

import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.ReleaseRepository;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import static com.raccoon.templatedata.QuteTemplateLoader.INDEX_TEMPLATE_ID;

@ApplicationScoped
public class IndexService {

    ArtistRepository artistRepository;
    ReleaseRepository releaseRepository;
    Template index;

    @Inject
    public IndexService(final ArtistRepository artistRepository,
                        final ReleaseRepository releaseRepository,
                        final Engine engine) {
        this.artistRepository = artistRepository;
        this.releaseRepository = releaseRepository;
        this.index = engine.getTemplate(INDEX_TEMPLATE_ID);
    }

    public String getTemplateInstance() {
        return index.data(
                "artistCount", String.valueOf(artistRepository.count()),
                "releaseCount", String.valueOf(releaseRepository.count())
        ).render();
    }

}
