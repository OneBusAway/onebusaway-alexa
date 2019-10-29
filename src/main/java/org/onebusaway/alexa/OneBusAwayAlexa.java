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

package org.onebusaway.alexa;

import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import org.onebusaway.alexa.config.SpringContext;

/**
 * OneBusAway skill entry point.
 */
public class OneBusAwayAlexa extends SkillStreamHandler {

    public OneBusAwayAlexa() {
        super(getSkill());
    }

    private static Skill getSkill() {
        return SpringContext.getInstance().getBean("oneBusAwaySkill", Skill.class);
    }
}
