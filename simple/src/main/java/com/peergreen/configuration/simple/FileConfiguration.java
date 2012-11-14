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

import com.peergreen.configuration.api.ConfigRepository;
import com.peergreen.configuration.api.Configuration;
import com.peergreen.configuration.api.RepositoryException;

/**
 *
 * @author Florent Benoit
 */
public class FileConfiguration implements Configuration {

    File rootDirectory = null;


    @Override
    public ConfigRepository getRepository(String name) throws RepositoryException {
        check();

        return new FileConfigRepository(new File(rootDirectory, name));
    }

    @Override
    public void setRootDirectory(File rootDirectory) throws RepositoryException {
        if (this.rootDirectory != null) {
            throw new RepositoryException("Cannot change repository directory once it has been setup");
        }
       this.rootDirectory = rootDirectory;
    }

    protected void check() throws RepositoryException {
        if (rootDirectory == null) {
            throw new RepositoryException("The root directory to store the configuration has not been set.");
        }
    }

}
