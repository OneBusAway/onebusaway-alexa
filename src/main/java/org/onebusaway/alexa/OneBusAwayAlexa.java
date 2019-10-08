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
