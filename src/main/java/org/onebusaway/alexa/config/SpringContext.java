package org.onebusaway.alexa.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Spring application context singleton.
 */
public class SpringContext {
    private static ApplicationContext INSTANCE;

    /**
     * Get application context.
     * @return
     */
    public static ApplicationContext getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AnnotationConfigApplicationContext(ApplicationConfig.class);
        }
        return INSTANCE;
    }

    private SpringContext() {
    }
}
