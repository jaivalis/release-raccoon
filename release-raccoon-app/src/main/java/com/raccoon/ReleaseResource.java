package com.raccoon;

import com.raccoon.entity.Release;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;
import io.quarkus.rest.data.panache.ResourceProperties;

@ResourceProperties(exposed = false)
public interface ReleaseResource extends PanacheEntityResource<Release, Long> {

}
