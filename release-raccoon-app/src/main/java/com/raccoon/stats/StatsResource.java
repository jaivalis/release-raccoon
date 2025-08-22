package com.raccoon.stats;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/stats")
@Produces(MediaType.APPLICATION_JSON)
public class StatsResource {

    private final StatsService statsService;

    @Inject
    public StatsResource(final StatsService statsService) {
        this.statsService = statsService;
    }

    @GET
    @Path("/releases/count")
    public Response getReleaseCount() {
        long count = statsService.getReleaseCount();
        return Response.ok(new CountResponse(count)).build();
    }

    @GET
    @Path("/artists/count")
    public Response getArtistCount() {
        long count = statsService.getArtistCount();
        return Response.ok(new CountResponse(count)).build();
    }

    @GET
    public Response getAllStats() {
        StatsService.StatsResponse stats = statsService.getAllStats();
        return Response.ok(stats).build();
    }

    public record CountResponse(long count) {}

}