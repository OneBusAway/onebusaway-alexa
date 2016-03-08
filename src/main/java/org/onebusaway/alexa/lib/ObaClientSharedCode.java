package org.onebusaway.alexa.lib;

import lombok.extern.log4j.Log4j;
import org.onebusaway.io.client.elements.ObaRegion;
import org.onebusaway.io.client.request.ObaRegionsRequest;
import org.onebusaway.io.client.util.RegionUtils;
import org.onebusaway.location.Location;

import java.util.ArrayList;
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
    public Optional<ObaRegion> getClosestRegion(Location l) {
        log.debug("Invoked getClosestRegion() with location " + l.toString());
        return Optional.ofNullable(RegionUtils.getClosestRegion(
                // FIXME: update `onebusaway-client-library` to be satisfied with List<>
                new ArrayList<>(getAllRegions()),
                l,
                true)); // enforce proximity threshold
    }

    public List<ObaRegion> getAllRegions() {
        return Arrays.asList(ObaRegionsRequest.newRequest().call().getRegions());
    }
}
