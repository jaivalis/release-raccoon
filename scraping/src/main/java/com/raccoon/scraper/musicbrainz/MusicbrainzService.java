package com.raccoon.scraper.musicbrainz;

import com.raccoon.scraper.musicbrainz.dto.MusicbrainzArtistsResponse;
import com.raccoon.scraper.musicbrainz.dto.MusicbrainzReleasesResponse;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import static com.raccoon.scraper.musicbrainz.MusicbrainzService.RACCOON_USER_AGENT;

/**
 * RestClient that calls Musicbrainz API.
 *
 * Calls to Musicbrainz should be throttled to 50 per second, otherwise 503 are thrown.
 */
@Path("/ws/2/")
@RegisterRestClient
@ApplicationScoped
@ClientHeaderParam(name = HttpHeaders.USER_AGENT, value = RACCOON_USER_AGENT)
@ClientHeaderParam(name = HttpHeaders.CONTENT_TYPE, value = MediaType.APPLICATION_FORM_URLENCODED)
public interface MusicbrainzService {

    String RACCOON_USER_AGENT = "release-raccoon/0.0.1 ( releaseraccoon@gmail.com )";

    /**
     * Performs a GET against `release/` endpoint.
     * Example query:
     *      https://musicbrainz.org/ws/2/release/?query=date%3A%282022%5C-02%5C-13%29&fmt=json&limit=100&offset=0
     *
     * @param query the search query
     * @param format format of results
     * @param limit An integer value defining how many entries should be returned. Only values between 1 and 100 (both inclusive) are allowed. If not given, this defaults to 25.
     * @param offset Return search results starting at a given offset. Used for paging through more than one page of results.
     * @return MusicbrainzReleasesResponse object
     */
    @GET
    @Path("release/")
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    MusicbrainzReleasesResponse getReleasesByQuery(@QueryParam("query") String query,
                                                   @QueryParam("fmt") String format,
                                                   @QueryParam("limit") String limit,
                                                   @QueryParam("offset") String offset);

    /**
     * Performs a GET against `artist/` endpoint.
     * Example query:
     *      https://musicbrainz.org/ws/2/artist/?query=name%3A%28<artistName>%29&fmt=json&limit=100&offset=0
     *
     * @param query the search query
     * @param format format of results
     * @param limit An integer value defining how many entries should be returned. Only values between 1 and 100 (both inclusive) are allowed. If not given, this defaults to 25.
     * @param offset Return search results starting at a given offset. Used for paging through more than one page of results.
     * @return MusicbrainzReleasesResponse object
     */
    @GET
    @Path("artist/")
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    MusicbrainzArtistsResponse getArtistsByQuery(@QueryParam("query") String query,
                                                 @QueryParam("fmt") String format,
                                                 @QueryParam("limit") String limit,
                                                 @QueryParam("offset") String offset);

}
