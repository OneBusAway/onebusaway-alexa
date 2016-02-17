/*
 * Copyright 2016 Philip M. White (philip@mailworks.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.alexa.lib;

import lombok.extern.log4j.Log4j;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@Log4j
public class ObaAgencies {
    private Map<String, String> agencies;

    public ObaAgencies(InputStream is) {
        Yaml yaml = new Yaml(new SafeConstructor());
        Object yamlData = yaml.load(is);
        agencies = (Map<String, String>) yamlData;
    }

    public Optional<String> agencyForCity(String city) {
        if (agencies.containsKey(city)) {
            return Optional.of(agencies.get(city));
        }
        else {
            return Optional.empty();
        }
    }
}
