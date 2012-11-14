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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.jgit.lib.ObjectLoader;

import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.Version;
import com.peergreen.configuration.api.VersionedResource;

/**
 *
 * @author Florent Benoit
 */
public class GitResource implements VersionedResource {

    private Version version = null;

    private final ObjectLoader objectLoader;

    private final long lastModified;

    public GitResource(ObjectLoader objectLoader, long lastModified, Version version) {
        this.objectLoader = objectLoader;
        this.lastModified = lastModified;
        this.version = version;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public InputStream openStream() throws RepositoryException {
        try {
            return objectLoader.openStream();
        } catch (IOException e) {
            throw new RepositoryException("Unable to load the stream", e);
        }
    }

    @Override
    public long lastModified() {
        return lastModified;
    }

    @Override
    public long getSize() {
        return objectLoader.getSize();
    }

}
