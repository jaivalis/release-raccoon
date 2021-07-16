package com.raccoon;

import com.raccoon.entity.Artist;
import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;

public interface ArtistResource extends PanacheEntityResource<Artist, Long> {

}
