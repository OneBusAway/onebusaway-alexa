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

package org.onebusaway.alexa.config;

import com.amazon.ask.dispatcher.exception.ExceptionHandler;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import org.onebusaway.alexa.handlers.GenericExceptionHandler;
import org.onebusaway.alexa.handlers.UnknownRequestHandler;
import org.onebusaway.alexa.handlers.event.EventHandler;
import org.onebusaway.alexa.handlers.event.SkillDisableEventHandler;
import org.onebusaway.alexa.handlers.event.SkillEnableEventHandler;
import org.onebusaway.alexa.handlers.intent.CancelIntentHandler;
import org.onebusaway.alexa.handlers.intent.DisableClockTimeHandler;
import org.onebusaway.alexa.handlers.intent.DisableExperimentalRegionsHandler;
import org.onebusaway.alexa.handlers.intent.EnableClockTimeHandler;
import org.onebusaway.alexa.handlers.intent.EnableExperimentalRegionsHandler;
import org.onebusaway.alexa.handlers.intent.GetArrivalsIntentHandler;
import org.onebusaway.alexa.handlers.intent.GetCityIntentHandler;
import org.onebusaway.alexa.handlers.intent.GetStopNumberIntentHandler;
import org.onebusaway.alexa.handlers.intent.HelpIntentHandler;
import org.onebusaway.alexa.handlers.intent.IntentHandler;
import org.onebusaway.alexa.handlers.intent.LaunchRequestHandler;
import org.onebusaway.alexa.handlers.intent.NoIntentHandler;
import org.onebusaway.alexa.handlers.intent.RepeatIntentHandler;
import org.onebusaway.alexa.handlers.intent.SetCityIntentHandler;
import org.onebusaway.alexa.handlers.intent.SetRouteFilterIntentHandler;
import org.onebusaway.alexa.handlers.intent.SetStopNumberIntentHandler;
import org.onebusaway.alexa.handlers.intent.StopIntentHandler;
import org.onebusaway.alexa.handlers.intent.YesIntentHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring config for all Alexa request Handlers.
 */
@Configuration
public class HandlerConfig {
    /**
     * Handler for AMAZON.CancelIntent intent.
     *
     * @return
     */
    @Bean
    public IntentHandler cancelIntentHandler() {
        return new CancelIntentHandler();
    }

    /**
     * Handler for DisableClockTime intent.
     *
     * @return
     */
    @Bean
    public IntentHandler disableClockTimeHandler() {
        return new DisableClockTimeHandler();
    }

    /**
     * Handler for DisableExperimentalRegions intent.
     *
     * @return
     */
    @Bean
    public IntentHandler disableExperimentalRegionsHandler() {
        return new DisableExperimentalRegionsHandler();
    }

    /**
     * Handler for EnableClockTime intent.
     *
     * @return
     */
    @Bean
    public IntentHandler enableClockTimeHandler() {
        return new EnableClockTimeHandler();
    }

    /**
     * Handler for EnableExperimentalRegions intent.
     *
     * @return
     */
    @Bean
    public IntentHandler enableExperimentalRegionsHandler() {
        return new EnableExperimentalRegionsHandler();
    }

    /**
     * Handler for GetArrivalsIntent intent.
     *
     * @return
     */
    @Bean
    public IntentHandler getArrivalsIntentHandler() {
        return new GetArrivalsIntentHandler();
    }

    /**
     * Handler for GetCityIntent intent.
     *
     * @return
     */
    @Bean
    public IntentHandler getCityIntentHandler() {
        return new GetCityIntentHandler();
    }

    /**
     * Handler for GetStopNumber Intent.
     *
     * @return
     */
    @Bean
    public IntentHandler getStopNumberIntentHandler() {
        return new GetStopNumberIntentHandler();
    }

    /**
     * Handler for Help intent.
     *
     * @return
     */
    @Bean
    public IntentHandler helpIntentHandler() {
        return new HelpIntentHandler();
    }

    /**
     * Handler for Launch request.
     *
     * @return
     */
    @Bean
    public IntentHandler launchRequestHandler() {
        return new LaunchRequestHandler();
    }

    /**
     * Handler for No intent.
     *
     * @return
     */
    @Bean
    public IntentHandler noIntentHandler() {
        return new NoIntentHandler();
    }

    /**
     * Handler for Repeat intent.
     *
     * @return
     */
    @Bean
    public IntentHandler repeatIntentHandler() {
        return new RepeatIntentHandler();
    }

    /**
     * Handler for SetCity intent.
     *
     * @return
     */
    @Bean
    public IntentHandler setCityIntentHandler() {
        return new SetCityIntentHandler();
    }

    /**
     * Handler for SetRouteFilter intent.
     *
     * @return
     */
    @Bean
    public IntentHandler setRouteFilterIntentHandler() {
        return new SetRouteFilterIntentHandler();
    }

    /**
     * Handler for SetStopNumber intent.
     *
     * @return
     */
    @Bean
    public IntentHandler setStopNumberIntentHandler() {
        return new SetStopNumberIntentHandler();
    }

    /**
     * Handler for Yes intent.
     *
     * @return
     */
    @Bean
    public IntentHandler yesIntentHandler() {
        return new YesIntentHandler();
    }

    /**
     * Handler for Stop intent.
     *
     * @return
     */
    @Bean
    public IntentHandler stopIntentHandler() {
        return new StopIntentHandler();
    }

    /**
     * Handler for Unknown intent.
     *
     * @return
     */
    @Bean
    public RequestHandler unknownRequestHandler() {
        return new UnknownRequestHandler();
    }

    /**
     * Handler for SkillEnabled event.
     *
     * @return
     */
    @Bean
    public EventHandler skillEnableEventHandler() {
        return new SkillEnableEventHandler();
    }

    /**
     * Handler for SkillDisabled event.
     *
     * @return
     */
    @Bean
    public EventHandler skillDisableEventHandler() {
        return new SkillDisableEventHandler();
    }

    /**
     * Handler for OneBusAway exceptions.
     *
     * @return
     */
    @Bean
    public ExceptionHandler genericExceptionHandler() {
        return new GenericExceptionHandler();
    }
}
