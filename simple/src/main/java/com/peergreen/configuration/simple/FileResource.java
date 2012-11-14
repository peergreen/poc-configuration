package com.peergreen.configuration.simple;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.Version;
import com.peergreen.configuration.api.VersionedResource;

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

/**
 *
 * @author Florent Benoit
 */
public class FileResource implements VersionedResource {

    private Version version = null;

    private final File file;


    public FileResource(File file, Version version) {
        this.file = file;
        this.version = version;
    }

    @Override
    public Version getVersion() {
        return version;
    }



    @Override
    public InputStream openStream() throws RepositoryException {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RepositoryException("Cannot open stream", e);
            }
    }



    @Override
    public long lastModified() {
       return file.lastModified();
    }



    @Override
    public long getSize() {
        return file.length();
    }

}
