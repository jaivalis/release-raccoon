package com.raccoon.artist;

import com.raccoon.dto.PaginationParams;
import com.raccoon.user.dto.FollowedArtistsResponse;

import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.oidc.IdToken;
import io.quarkus.security.Authenticated;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import static com.raccoon.Constants.EMAIL_CLAIM;

@Path("/artists")
@Slf4j
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArtistResource {

    final ArtistsService artistsService;

    @IdToken
    JsonWebToken idToken;

    public ArtistResource(ArtistsService artistsService) {
        this.artistsService = artistsService;
    }

    @GET
    public Response getAll(@Valid @BeanParam PaginationParams pageRequest) {
        return Response.ok(
                artistsService.getArtists(pageRequest)
        ).build();
    }

    @GET
    @Path("/recommended")
    public FollowedArtistsResponse getFollowed(@BeanParam PaginationParams pageRequest) {
        final String email = idToken.getClaim(EMAIL_CLAIM);

        return artistsService.getOtherUsersFollowedArtists(pageRequest, email);
    }

}
