package org.onebusaway.alexa.util;


import com.amazon.ask.attributes.AttributesManager;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.storage.ObaUserDataItem;

import java.util.Map;
import java.util.Optional;

import static org.onebusaway.alexa.constant.SessionAttribute.ANNOUNCED_FEATURES_V1_1_0;
import static org.onebusaway.alexa.constant.SessionAttribute.ANNOUNCED_INTRODUCTION;
import static org.onebusaway.alexa.constant.SessionAttribute.CITY_NAME;
import static org.onebusaway.alexa.constant.SessionAttribute.CLOCK_TIME;
import static org.onebusaway.alexa.constant.SessionAttribute.EXPERIMENTAL_REGIONS;
import static org.onebusaway.alexa.constant.SessionAttribute.LAST_ACCESS_TIME;
import static org.onebusaway.alexa.constant.SessionAttribute.OBA_BASE_URL;
import static org.onebusaway.alexa.constant.SessionAttribute.PREVIOUS_RESPONSE;
import static org.onebusaway.alexa.constant.SessionAttribute.REGION_ID;
import static org.onebusaway.alexa.constant.SessionAttribute.REGION_NAME;
import static org.onebusaway.alexa.constant.SessionAttribute.STOP_ID;
import static org.onebusaway.alexa.constant.SessionAttribute.TIME_ZONE;

/**
 * Utilities that help manage the session state.
 */
@Log4j
public class SessionUtil {
    /**
     * Populates the provided session with persisted user data, if the session attribute is empty and if user data
     * exists.  If user data does not exist, it populates the session with default values for preferences.
     *
     * @param attributesManager
     * @param userData
     */
    public static void populateAttributes(AttributesManager attributesManager, Optional<ObaUserDataItem> userData) {
        Map<String, Object> alexaSession = attributesManager.getSessionAttributes();
        if (!userData.isPresent()) {
            // There is no user data to populate the session with - assign defaults and return
            alexaSession.put(CLOCK_TIME, 0);
            alexaSession.put(ANNOUNCED_INTRODUCTION, 0);
            alexaSession.put(ANNOUNCED_FEATURES_V1_1_0, 0);
            alexaSession.put(EXPERIMENTAL_REGIONS, false);
            return;
        }
        if (alexaSession.get(CITY_NAME) == null) {
            alexaSession.put(CITY_NAME, userData.get().getCity());
        }
        if (alexaSession.get(STOP_ID) == null) {
            alexaSession.put(STOP_ID, userData.get().getStopId());
        }
        if (alexaSession.get(REGION_ID) == null) {
            alexaSession.put(REGION_ID, userData.get().getRegionId());
        }
        if (alexaSession.get(REGION_NAME) == null) {
            alexaSession.put(REGION_NAME, userData.get().getRegionName());
        }
        if (alexaSession.get(OBA_BASE_URL) == null) {
            alexaSession.put(OBA_BASE_URL, userData.get().getObaBaseUrl());
        }
        if (alexaSession.get(PREVIOUS_RESPONSE) == null) {
            alexaSession.put(PREVIOUS_RESPONSE, userData.get().getPreviousResponse());
        }
        if (alexaSession.get(LAST_ACCESS_TIME) == null) {
            alexaSession.put(LAST_ACCESS_TIME, userData.get().getLastAccessTime());
        }
        if (alexaSession.get(CLOCK_TIME) == null) {
            alexaSession.put(CLOCK_TIME, userData.get().getSpeakClockTime());
        }
        if (alexaSession.get(TIME_ZONE) == null) {
            alexaSession.put(TIME_ZONE, userData.get().getTimeZone());
        }
        if (alexaSession.get(ANNOUNCED_INTRODUCTION) == null) {
            alexaSession.put(ANNOUNCED_INTRODUCTION, userData.get().getAnnouncedIntroduction());
        }
        if (alexaSession.get(ANNOUNCED_FEATURES_V1_1_0) == null) {
            alexaSession.put(ANNOUNCED_FEATURES_V1_1_0, userData.get().getAnnouncedFeaturesv1_1_0());
        }
        if (alexaSession.get(EXPERIMENTAL_REGIONS) == null) {
            alexaSession.put(EXPERIMENTAL_REGIONS, userData.get().isExperimentalRegions());
        }
        attributesManager.setSessionAttributes(alexaSession);
    }

    /**
     * Add or update(if attribute exist) the Alexa session attribute.
     *
     * @param attributesManager
     * @param key
     * @param value
     */
    public static void addOrUpdateSessionAttribute(AttributesManager attributesManager, String key, Object value) {
        Optional.ofNullable(attributesManager)
                .map(AttributesManager::getSessionAttributes)
                .ifPresent((alexaSession) -> {
                    log.info(String.format("Add session attribute %s %s", key, value));
                    alexaSession.put(key, value);
                    attributesManager.setSessionAttributes(alexaSession);
                });
    }

    /**
     * Method to get the attribute from Alexa session.
     *
     * @param attributesManager
     * @param key
     * @param clazz             return type
     * @return Alexa session attribute
     */
    public static <T> T getSessionAttribute(AttributesManager attributesManager, String key, Class<T> clazz) {
        if (clazz == null) {
            log.error(String.format("No type passed in for key %s", key));
            return null;
        }
        Map<String, Object> alexaSession = attributesManager.getSessionAttributes();
        try {
            T sessionAttribute = clazz.cast(alexaSession.get(key));
            log.info(String.format("Get session attribute %s %s", key, sessionAttribute));
            return sessionAttribute;
        } catch (ClassCastException e) {
            log.error(String.format("Failed to cast attribute %s to %s", key, clazz.getTypeName()));
            return null;
        }
    }

    /**
     * Method to get the attribute from Alexa session, will return the default value when failed to cast to specific type.
     *
     * @param attributesManager
     * @param key
     * @param clazz
     * @param defaultValue
     * @return
     */
    public static <T> T getSessionAttribute(AttributesManager attributesManager, String key, Class<T> clazz, T defaultValue) {
        if (clazz == null) {
            log.error(String.format("No type passed in for key %s", key));
            return defaultValue;
        }
        Map<String, Object> alexaSession = attributesManager.getSessionAttributes();
        try {
            T sessionAttribute = clazz.cast(alexaSession.get(key));
            log.info(String.format("Get session attribute %s %s", key, sessionAttribute));
            if (sessionAttribute == null) {
                sessionAttribute = defaultValue;
                log.info(String.format("Returns default value %s", defaultValue));
            }
            return sessionAttribute;
        } catch (ClassCastException e) {
            log.error(String.format("Failed to cast attribute %s to %s", key, clazz.getTypeName()));
            return defaultValue;
        }
    }
}
