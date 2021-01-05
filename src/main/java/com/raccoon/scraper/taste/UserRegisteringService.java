package com.raccoon.scraper.taste;

import com.raccoon.dto.RegisterUserRequest;
import com.raccoon.entity.User;
import lombok.val;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
public class UserRegisteringService {

    @Inject
    TasteScrapers tasteScrapers;

    public User register(RegisterUserRequest request) {

        for (val scraper : tasteScrapers) {
            scraper.scrapeTaste(request.getLastfmUsername(), Optional.empty());
        }

        return null;
    }

}
