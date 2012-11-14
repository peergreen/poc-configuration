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

package com.peergreen.configuration.git;

import com.peergreen.configuration.api.Path;

/**
 *
 * @author Florent Benoit
 */
public class GitPath implements Path {

    private final String name;

    private final long lastModified;

    private final long size;

    public GitPath(String name, long size, long lastModified) {
        this.name = name;
        this.size = size;
        this.lastModified = lastModified;
    }


    @Override
    public String name() {
        return name;
    }


    @Override
    public long lastModified() {
        return lastModified;
    }


    @Override
    public long size() {
        return size;
    }




}
