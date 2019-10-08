package org.onebusaway.alexa.exception;

/**
 * OneBusAway exception will thrown when Alexa handler failed to process the request.
 */
public class OneBusAwayException extends RuntimeException {
    public OneBusAwayException() {
    }

    public OneBusAwayException(String message) {
        super(message);
    }

    public OneBusAwayException(String message, Throwable cause) {
        super(message, cause);
    }
}
