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

import org.ow2.util.file.FileUtils;
import org.ow2.util.file.FileUtilsException;

import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.Resource;
import com.peergreen.configuration.api.Version;
import com.peergreen.configuration.api.Write;

/**
 *
 * @author Florent Benoit
 */
public class FileWrite extends FileRead implements Write {

    public FileWrite(File rootDirectory) {
        super(rootDirectory);
    }


    @Override
    public void pushResource(String path, Resource resource) throws RepositoryException {
        if (path == null) {
            throw new RepositoryException("Cannot push an entry with a null path");
        }


        if (resource == null) {
            throw new RepositoryException("Cannot push to the path '" + path + "' with a null resource.");
        }

        File entry = checkAndGetResourceFile(path);
        entry.getParentFile().mkdirs();

        try {
            FileUtils.dump(resource.openStream(), entry);
        } catch (FileUtilsException e) {
            throw new RepositoryException("Unable to write resource to path '" + path + "'.", e);
        }
    }

    @Override
    public void removeResource(String path) throws RepositoryException {
        if (path == null) {
            throw new RepositoryException("Cannot push an entry with a null path");
        }

        File entry = new File(getRootDirectory(), getFilePattern(path));
        entry.delete();

    }

    @Override
    public void tag(Version version) throws RepositoryException {

        File tagDirectory =new File(getRootDirectory().getParentFile(), version.getName());
        if (tagDirectory.exists()) {
            throw new RepositoryException("Version '" + version.getName() + "' alread exists'");
        }

        //get parent
        try {
            FileUtils.copyDirectory(getRootDirectory(), tagDirectory);
        } catch (FileUtilsException e) {
            throw new RepositoryException("Unable to tag the version '" + version.getName() + "'.", e);
        }
    }

    @Override
    public void resetChanges(String path) {
        throw new IllegalStateException("Not implemented as not supporting history");
    }

    @Override
    public void resetChanges() {
        throw new IllegalStateException("Not implemented as not supporting history");
    }

    @Override
    public boolean isUndoable() {
        throw new IllegalStateException("Not implemented as not supporting history");
    }

    @Override
    public boolean undo() {
        throw new IllegalStateException("Not implemented as not supporting history");
    }

    @Override
    public boolean isRedoable() {
        throw new IllegalStateException("Not implemented as not supporting history");
    }

    @Override
    public boolean redo() {
        throw new IllegalStateException("Not implemented as not supporting history");
    }

}
