package com.raccoon.scraper.taste;

import com.raccoon.dto.RegisterUserRequest;
import com.raccoon.entity.Artist;
import com.raccoon.entity.User;
import com.raccoon.entity.UserArtist;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.MutablePair;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.raccoon.entity.User.*;

@Slf4j
@ApplicationScoped
public class UserRegisteringService {

    @Inject
    TasteScrapers tasteScrapers;

    @Transactional
    public User register(RegisterUserRequest request) {
        Optional<User> existing = findbyEmailOptional(request.getEmail());
        if (existing.isPresent()) {
            log.info("User with email {} exists.", request.getEmail());
            return existing.get();
        }

        return registerNewUser(request.getEmail(), request.getLastfmUsername());
    }

    private User registerNewUser(final String email, final String username) {
        final Collection<MutablePair<Artist, Float>> aggregateTaste = new ArrayList<>();
        for (val scraper : tasteScrapers) {
            // this needs to somehow smartly aggregate the artists.
            aggregateTaste.addAll(scraper.scrapeTaste(username, Optional.empty()));
        }

        final User user = new User();
        user.setEmail(email);
        user.setLastfmUsername(username);
        user.setArtists(
                normalizeWeights(aggregateTaste).stream().map(pair -> {
                    UserArtist userArtist = new UserArtist();
                    userArtist.setUser(user);
                    userArtist.setArtist(pair.left);
                    userArtist.setWeight(pair.right);
                    return userArtist;
                }).collect(Collectors.toSet())
        );
        persist(user);

        return user;
    }

    private Collection<MutablePair<Artist, Float>> normalizeWeights(Collection<MutablePair<Artist, Float>> taste) {
        float max = 0;
        for (val pair : taste) {
            max = Math.max(max, pair.getRight());
        }
        if (max == 0) {
            return Collections.emptyList();
        }
        final float maxWeight = max;
        taste.forEach(pair -> pair.setRight(pair.right / maxWeight));
        return taste;
    }
}
