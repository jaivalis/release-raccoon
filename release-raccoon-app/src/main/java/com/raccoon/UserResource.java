package com.raccoon;

import com.raccoon.entity.User;
import io.quarkus.hibernate.orm.rest.data.panache.PanacheEntityResource;

public interface UserResource extends PanacheEntityResource<User, Long> {

}
