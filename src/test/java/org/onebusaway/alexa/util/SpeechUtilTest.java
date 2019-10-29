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

package org.onebusaway.alexa.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SpeechUtilTest {
    private static final String TEST_ADDRESS_1 = "131st Street & 2nd Avenue";
    private static final String EXPECTES_ADDRESS_1 = "131st Street and 2nd Avenue";
    private static final String TEST_ADDRESS_2 = "131st Street + 2nd Avenue";
    private static final String EXPECTES_ADDRESS_2 = "131st Street and 2nd Avenue";
    private static final String TEST_ADDRESS_3 = "131st Street @ 2nd Avenue";
    private static final String EXPECTES_ADDRESS_3 = "131st Street at 2nd Avenue";
    private static final String TEST_ADDRESS_4 = "131st Street !#$%^ 2nd Avenue";
    private static final String EXPECTES_ADDRESS_4 = "131st Street       2nd Avenue";


    @Test
    public void replaceSpecialCharactersFromAddress_withAmpersandInString_replacedWithAnd() {
        String result = SpeechUtil.replaceSpecialCharactersFromAddress(TEST_ADDRESS_1);
        assertEquals(EXPECTES_ADDRESS_1, result);
    }

    @Test
    public void replaceSpecialCharactersFromAddress_withPlusInString_replacedWithAnd() {
        String result = SpeechUtil.replaceSpecialCharactersFromAddress(TEST_ADDRESS_2);
        assertEquals(EXPECTES_ADDRESS_2, result);
    }

    @Test
    public void replaceSpecialCharactersFromAddress_withAtInString_replacedWithAt() {
        String result = SpeechUtil.replaceSpecialCharactersFromAddress(TEST_ADDRESS_3);
        assertEquals(EXPECTES_ADDRESS_3, result);
    }

    @Test
    public void replaceSpecialCharactersFromAddress_withOtherSpecialCharactersInString_replacedWithWhitespace() {
        String result = SpeechUtil.replaceSpecialCharactersFromAddress(TEST_ADDRESS_4);
        assertEquals(EXPECTES_ADDRESS_4, result);
    }
}
