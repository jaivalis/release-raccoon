package com.raccoon.user.settings;

import com.raccoon.entity.UserSettings;
import com.raccoon.user.settings.dto.UserSettingsDto;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.cache.NoCache;

import io.quarkus.oidc.IdToken;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static com.raccoon.Constants.EMAIL_CLAIM;

@Path("/me/settings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserSettingsResource {

    private final UserSettingsService userSettingsService;

    @Inject
    public UserSettingsResource(final UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    @IdToken
    JsonWebToken idToken;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public UserSettingsDto getUserSettings() {
        final String email = idToken.getClaim(EMAIL_CLAIM);
        return userSettingsService.getUserSettings(email);
    }

    @GET
    @NoCache
    @Produces(MediaType.TEXT_HTML)
    @Transactional
    public Response registrationCallback() {
        final String email = idToken.getClaim(EMAIL_CLAIM);

        return Response.ok(userSettingsService.renderSettingsPage(email))
                .build();
    }

    @POST
    public void setUserSettings(UserSettings userSettings) {
        final String email = idToken.getClaim(EMAIL_CLAIM);
        userSettingsService.addOrUpdateUserSetting(email, userSettings);
    }

}