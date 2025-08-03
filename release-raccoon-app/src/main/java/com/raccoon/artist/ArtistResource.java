package com.raccoon.artist;

import com.raccoon.dto.PaginationParams;
import com.raccoon.user.dto.FollowedArtistsResponse;

import io.quarkus.oidc.UserInfo;
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

@Path("/artists")
@Slf4j
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArtistResource {

    final ArtistsService artistsService;
    final UserInfo userInfo;

    public ArtistResource(final ArtistsService artistsService, final UserInfo userInfo) {
        this.artistsService = artistsService;
        this.userInfo = userInfo;
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
        final String email = userInfo.getEmail();;

        return artistsService.getOtherUsersFollowedArtists(pageRequest, email);
    }

}
