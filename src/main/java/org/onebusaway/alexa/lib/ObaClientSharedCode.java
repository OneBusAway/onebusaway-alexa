/*
 * Copyright 2016-2019 Sean J. Barbeau (sjbarbeau@gmail.com),
 * Philip M. White (philip@mailworks.org)
 * Chunzhang Mo (victormocz@gmail.com)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.alexa.lib;

import lombok.NonNull;
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
import java.util.stream.Collectors;

import static org.onebusaway.alexa.util.CityUtil.NEW_YORK_REGION_ID;

/**
 * Super class for ObaUserClient and ObaClient, which contains general helper method for subclasses.
 */
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
     * @param includeExperimentalRegions true if experimental (beta) regions should be included, false if they should not
     * @return the closest region in the Regions API to the given location, or null if there are no nearby regions
     * (within 100 miles of the provided location) or a region couldn't be found.
     */
    public Optional<ObaRegion> getClosestRegion(@NonNull Location l, boolean includeExperimentalRegions) throws IOException {
        log.debug("Invoked getClosestRegion() with location " + l.toString());
        return Optional.ofNullable(RegionUtils.getClosestRegion(
                getAllRegions(includeExperimentalRegions),
                l,
                true, false)); // enforce proximity threshold
    }

    /**
     * Get all OBA regions from the Regions API ((http://regions.onebusaway.org/regions-v3.json))
     *
     * @param includeExperimentalRegions true if experimental (beta) regions should be included, false if they should not
     * @return all OBA regions
     * @throws IOException
     */
    public List<ObaRegion> getAllRegions(boolean includeExperimentalRegions) throws IOException {
        ObaRegionsResponse response = ObaRegionsRequest.newRequest().call();
        if (response.getCode() == ObaApi.OBA_OK) {
            List<ObaRegion> regions = Arrays.asList(response.getRegions());
            return regions.stream()
                    .filter(r -> (RegionUtils.isRegionUsable(r) || (r.getId() == NEW_YORK_REGION_ID && includeExperimentalRegions))
                            && (!r.getExperimental() || includeExperimentalRegions)
                            && r.getObaBaseUrl() != null)
                    .collect(Collectors.toList());
        } else {
            throw new IOException("Error getting regions");
        }
    }
}
