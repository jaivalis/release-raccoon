package com.raccoon.index;

import com.raccoon.entity.repository.ArtistRepository;
import com.raccoon.entity.repository.ReleaseRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;

import static com.raccoon.templatedata.TemplateLoader.INDEX_TEMPLATE_ID;

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
