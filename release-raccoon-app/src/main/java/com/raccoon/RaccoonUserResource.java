package com.raccoon;

import com.raccoon.entity.RaccoonUser;

import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;

public interface RaccoonUserResource extends PanacheEntityResource<RaccoonUser, Long> {

}
