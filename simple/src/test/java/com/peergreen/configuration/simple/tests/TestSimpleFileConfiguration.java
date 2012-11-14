/**
 * Copyright 2012 Peergreen S.A.S.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.peergreen.configuration.simple.tests;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Factory;

import com.peergreen.configuration.api.Configuration;
import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.test.TestConfiguration;
import com.peergreen.configuration.simple.FileConfiguration;


/**
 * Test the simple implementation of the configuration service.
 * @author Florent Benoit
 */
public class TestSimpleFileConfiguration {

    @Factory
    public Object[] createTest() throws RepositoryException {
        List<Object> lists = new ArrayList<Object>();

        Configuration configuration = new FileConfiguration();

        lists.add(new TestConfiguration(configuration));

        return lists.toArray(new Object[lists.size()]);
     }
}
