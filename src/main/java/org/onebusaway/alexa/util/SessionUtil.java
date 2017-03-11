package org.onebusaway.alexa.util;

import com.amazon.speech.speechlet.Session;
import org.onebusaway.alexa.SessionAttribute;
import org.onebusaway.alexa.storage.ObaUserDataItem;

import static org.onebusaway.alexa.SessionAttribute.*;

/**
 * Utilities that help manage the session state
 */
public class SessionUtil {

    public enum OnboardState {Fresh, OnlyCity}

    /**
     * Return the current AskState from the current session
     *
     * @param session
     * @return the current AskState from the current session
     */
    public static SessionAttribute.AskState getAskState(Session session) {
        SessionAttribute.AskState askState = SessionAttribute.AskState.NONE;
        String savedAskState = (String) session.getAttribute(ASK_STATE);
        if (savedAskState != null) {
            askState = SessionAttribute.AskState.valueOf(savedAskState);
        }
        return askState;
    }

    /**
     * Returns the current onboard state for the given session - OnboardState.Fresh if no city or stop have been given,
     * or OnboardState.OnlyCity if only the city has been provided (but not the stop)
     *
     * @param session
     * @return the current onboard state for the given session - OnboardState.Fresh if no city or stop have been given,
     * or OnboardState.OnlyCity if only the city has been provided (but not the stop)
     */
    public static OnboardState getOnboardState(Session session) {
        if (session.getAttribute(CITY_NAME) != null) {
            return OnboardState.OnlyCity;
        } else
            return OnboardState.Fresh;
    }

    /**
     * Populates the provided session with persisted user data, if the session attribute is empty
     *
     * @param session
     */
    public static void populateAttributes(Session session, ObaUserDataItem userData) {
        if (session.getAttribute(CITY_NAME) == null) {
            session.setAttribute(CITY_NAME, userData.getCity());
        }
        if (session.getAttribute(STOP_ID) == null) {
            session.setAttribute(STOP_ID, userData.getStopId());
        }
        if (session.getAttribute(REGION_ID) == null) {
            session.setAttribute(REGION_ID, userData.getRegionId());
        }
        if (session.getAttribute(REGION_NAME) == null) {
            session.setAttribute(REGION_NAME, userData.getRegionName());
        }
        if (session.getAttribute(OBA_BASE_URL) == null) {
            session.setAttribute(OBA_BASE_URL, userData.getObaBaseUrl());
        }
        if (session.getAttribute(PREVIOUS_RESPONSE) == null) {
            session.setAttribute(PREVIOUS_RESPONSE, userData.getPreviousResponse());
        }
        if (session.getAttribute(LAST_ACCESS_TIME) == null) {
            session.setAttribute(LAST_ACCESS_TIME, userData.getLastAccessTime());
        }
        if (session.getAttribute(CLOCK_TIME) == null) {
            session.setAttribute(CLOCK_TIME, userData.getSpeakClockTime());
        }
        if (session.getAttribute(TIME_ZONE) == null) {
            session.setAttribute(TIME_ZONE, userData.getTimeZone());
        }
        if (session.getAttribute(ANNOUNCED_INTRODUCTION) == null) {
            session.setAttribute(ANNOUNCED_INTRODUCTION, userData.getTimeZone());
        }
        if (session.getAttribute(ANNOUNCED_FEATURES_V1_1_0) == null) {
            session.setAttribute(ANNOUNCED_FEATURES_V1_1_0, userData.getTimeZone());
        }
    }
}
