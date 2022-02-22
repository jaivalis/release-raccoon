package com.raccoon.scraper.musicbrainz;

import com.raccoon.scraper.musicbrainz.dto.MusicbrainzReleasesResponse;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import static com.raccoon.scraper.musicbrainz.MusicbrainzService.RACCOON_USER_AGENT;

/**
 * RestClient that calls Musicbrainz API
 */
@Path("/ws/2/")
@RegisterRestClient
@ApplicationScoped
@ClientHeaderParam(name = HttpHeaders.USER_AGENT, value = RACCOON_USER_AGENT)
@ClientHeaderParam(name = HttpHeaders.CONTENT_TYPE, value = MediaType.APPLICATION_FORM_URLENCODED)
public interface MusicbrainzService {

    String RACCOON_USER_AGENT = "release-raccoon/0.0.1";

    /**
     * Performs a GET against `releases/` endpoint.
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

}
