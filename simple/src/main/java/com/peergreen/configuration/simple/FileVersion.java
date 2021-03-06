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

package com.peergreen.configuration.simple;

import java.io.File;

import com.peergreen.configuration.api.Version;

/**
 *
 * @author Florent Benoit
 */
public class FileVersion implements Version{


    private final String name;


    public FileVersion(File file) {
        this(file.getName());
    }



    public FileVersion(String name) {
        this.name = name;
    }


    @Override
    public String getName() {
        return name;
    }

}
