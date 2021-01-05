package com.raccoon;

import com.raccoon.entity.Release;
import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;

public interface ReleaseResource extends PanacheEntityResource<Release, Long> {

}
