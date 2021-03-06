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

import com.peergreen.configuration.api.Path;


/**
 *
 * @author Florent Benoit
 */
public class FilePath implements Path {

    private final String name;

    private final long lastModified;

    private final long size;

    public FilePath(File entry, String prefix) {
        this(prefix, entry.getName(), entry.length(), entry.lastModified());
    }

    public FilePath(String name, long size, long lastModified) {
        this(null, name, size, lastModified);
    }


    public FilePath(String prefix, String name, long size, long lastModified) {
        if (prefix != null && prefix.length() > 0) {
            this.name = prefix.concat("/").concat(name);
        } else {
            this.name = name;
        }
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
