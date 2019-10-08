package org.onebusaway.alexa.personalization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

/**
 * Model for skill personalization information.
 */
@AllArgsConstructor
@Data
@ToString
public class PersonalizationInfo {
    /**
     * principleId will be personId when personId is available in first request, otherwise it is the userId.
     */
    private String principleId;
    /**
     * user level Id.
     */
    private String userId;
    /**
     * personId in the request, will be empty("") if there is no personId.
     */
    private String personId;
    /**
     * Flag to tell if interaction personalized or not.
     */
    private boolean isPersonalized;
}
