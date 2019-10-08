package org.onebusaway.alexa.handlers.event;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Context;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.User;
import com.amazon.ask.model.events.skillevents.SkillEnabledRequest;
import com.amazon.ask.model.interfaces.system.SystemState;
import com.amazon.ask.request.Predicates;
import lombok.extern.log4j.Log4j;
import org.onebusaway.alexa.storage.ObaUserEnableItem;

import java.time.Instant;
import java.util.Optional;

/**
 * Handler for AlexaSkillEvent.SkillEnabled event request.
 * (e.g. skill enable event will be sent to this handler when user enable OneBusAway skill)
 */
@Log4j
public class SkillEnableEventHandler extends EventHandler {
    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public String getEventRequestName() {
        return "AlexaSkillEvent.SkillEnabled";
    }

    /**
     * Override canHandle to decide whether the quest can be handled by request type.
     *
     * @param handlerInput
     * @return
     */
    @Override
    public boolean canHandle(HandlerInput handlerInput) {
        return handlerInput.matches(Predicates.requestType(SkillEnabledRequest.class));
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Optional<Response> handle() {
        Optional.of(handlerInput)
                .map(HandlerInput::getRequestEnvelope)
                .map(RequestEnvelope::getContext)
                .map(Context::getSystem)
                .map(SystemState::getUser)
                .map(User::getUserId).ifPresent((id) -> {
            log.info(String.format("User %s is enabling the skill, saving record from DynamoDB", id));
            obaDao.saveUserEnableData(new ObaUserEnableItem(id, Instant.now().getEpochSecond()));
        });
        return Optional.empty();
    }
}
