package org.onebusaway.alexa;

import lombok.AllArgsConstructor;

/**
 * Enumerations for OneBusAway session attributes
 */
@AllArgsConstructor
public enum SessionAttributes {
    CITY_NAME("cityName"),
    REGION_ID("regionId"),
    REGION_NAME("regionName"),
    OBA_BASE_URL("obaBaseUrl");

    private final String attribute;

    @Override
    public String toString() {
        return attribute;
    }
}
