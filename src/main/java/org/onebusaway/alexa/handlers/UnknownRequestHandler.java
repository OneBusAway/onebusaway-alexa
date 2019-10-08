package org.onebusaway.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.helper.PromptHelper;

import javax.inject.Inject;
import java.util.Optional;

import static org.onebusaway.alexa.constant.Prompt.UNKNOWN_INTENT_MESSAGE;

/**
 * Handler for unrecognized intent request.
 */
@Log4j
public class UnknownRequestHandler implements RequestHandler {
    @Inject
    private PromptHelper promptHelper;


    /**
     * Always returns true for intent that hasn't been processed.
     * @param handlerInput
     * @return
     */
    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return true;
    }

    /**
     * Handle request that hasn't been handled by all handlers in OneBusAway.
     * @param handlerInput
     * @return
     */
    @Override
    public Optional<Response> handle(HandlerInput handlerInput) {
        log.error("Unable to handle unrecognized Alexa request");
        log.info(handlerInput.getRequestEnvelopeJson());
        return promptHelper.getResponse(UNKNOWN_INTENT_MESSAGE);
    }
}