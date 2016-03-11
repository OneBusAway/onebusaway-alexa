package org.onebusaway.alexa.lib;

import com.amazon.speech.speechlet.SpeechletException;
import lombok.extern.log4j.Log4j;
import org.onebusaway.io.client.ObaApi;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.request.ObaRegionsRequest;
import org.onebusaway.io.client.request.ObaRegionsResponse;
import org.onebusaway.io.client.util.RegionUtils;
import org.onebusaway.location.Location;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Log4j
public abstract class ObaClientSharedCode {

    /**
     * Get the closest region to a given location.  Regions come from the Regions API
     * (http://regions.onebusaway.org/regions-v3.json), which is a centralized directory of all known
     * OneBusAway servers.  Each region has it's own OBA REST API endpoint
     * (http://developer.onebusaway.org/modules/onebusaway-application-modules/current/api/where/index.html),
     * which we will then use to get stop and arrival data for that region.
     *
     * @param l geographic location used to search for nearby regions
     * @return the closest region in the Regions API to the given location, or null if there are no nearby regions
     * (within 100 miles of the provided location) or a region couldn't be found.
     */
    public Optional<ObaRegion> getClosestRegion(Location l) throws IOException {
        log.debug("Invoked getClosestRegion() with location " + l.toString());
        return Optional.ofNullable(RegionUtils.getClosestRegion(
                getAllRegions(),
                l,
                true)); // enforce proximity threshold
    }

    /**
     * Get all OBA regions from the Regions API ((http://regions.onebusaway.org/regions-v3.json))
     * @return all OBA regions
     * @throws SpeechletException
     */
    public List<ObaRegion> getAllRegions() throws IOException {
        ObaRegionsResponse response = ObaRegionsRequest.newRequest().call();
        if (response.getCode() == ObaApi.OBA_OK) {
            return Arrays.asList(response.getRegions());
        } else {
            throw new IOException("Error getting regions");
        }
    }
}
