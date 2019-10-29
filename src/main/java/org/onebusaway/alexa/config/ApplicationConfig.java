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

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.builder.StandardSkillBuilder;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * OneBusAway skill Spring config, which contains all handlers and skill ID filter.
 */
@PropertySource("classpath:/onebusaway.properties")
@ComponentScan(basePackages = {"org.onebusaway.alexa.config"})
@Component
@Import(
        HandlerConfig.class
)
@Log4j
public class ApplicationConfig {
    /**
     * Spring config for all Handlers.
     */
    @Inject
    private HandlerConfig handlerConfig;

    /**
     * Devo Skill Id.
     */
    @Value("${" + ObaProperty.APP_SKILL_ID_DEVELOPMENT + ":}")
    private String devoSkillId;

    /**
     * Prod Skill Id.
     */
    @Value("${" + ObaProperty.APP_SKILL_ID_PRODUCTION + ":}")
    private String prodSkillId;

    /**
     * OneBusAway skill.
     */
    @Bean
    public Skill oneBusAwaySkill() {
        StandardSkillBuilder standardSkillBuilder = Skills.standard()
                .addRequestHandlers(
                        // Intent handlers.
                        handlerConfig.launchRequestHandler(),
                        handlerConfig.cancelIntentHandler(),
                        handlerConfig.disableClockTimeHandler(),
                        handlerConfig.disableExperimentalRegionsHandler(),
                        handlerConfig.enableClockTimeHandler(),
                        handlerConfig.enableExperimentalRegionsHandler(),
                        handlerConfig.getArrivalsIntentHandler(),
                        handlerConfig.getCityIntentHandler(),
                        handlerConfig.getStopNumberIntentHandler(),
                        handlerConfig.helpIntentHandler(),
                        handlerConfig.noIntentHandler(),
                        handlerConfig.repeatIntentHandler(),
                        handlerConfig.setCityIntentHandler(),
                        handlerConfig.setRouteFilterIntentHandler(),
                        handlerConfig.setStopNumberIntentHandler(),
                        handlerConfig.yesIntentHandler(),
                        handlerConfig.stopIntentHandler(),
                        // Event handlers.
                        handlerConfig.skillEnableEventHandler(),
                        handlerConfig.skillDisableEventHandler(),
                        // NOTE unknown request handler has to be at last
                        // to handle recognized request because canHandle always return true.
                        handlerConfig.unknownRequestHandler())
                .addExceptionHandler(handlerConfig.genericExceptionHandler());
        if (StringUtils.isNotBlank(prodSkillId)) {
            log.info("Using prod skill Id " + prodSkillId);
            standardSkillBuilder.withSkillId(prodSkillId);
        } else if (StringUtils.isNotBlank(devoSkillId)) {
            log.info("Using devo skill Id " + devoSkillId);
            standardSkillBuilder.withSkillId(devoSkillId);
        } else {
            log.info("Build the skill without skill Id filter since no skill Id provided in property config file.");
        }
        return standardSkillBuilder.build();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
