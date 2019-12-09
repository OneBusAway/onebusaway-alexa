package org.onebusaway.alexa.exception;

import org.junit.Test;

import java.io.IOException;

public class OneBusAwayExceptionTest {
    @Test
    public void init_OneBusAwayException_succeed() {
        new OneBusAwayException();
        new OneBusAwayException("Error");
        new OneBusAwayException("IO Error", new IOException());
    }
}
