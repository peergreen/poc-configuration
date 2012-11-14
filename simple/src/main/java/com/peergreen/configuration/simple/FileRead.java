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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.peergreen.configuration.api.Path;
import com.peergreen.configuration.api.Read;
import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.VersionedResource;

/**
 *
 * @author Florent Benoit
 */
public class FileRead implements Read {

    private File rootDirectory = null;

    public FileRead(File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    protected List<Path> getPaths(File directory, String prefix) {
        List<Path> paths = new ArrayList<Path>();
        File[] entries = directory.listFiles();
        for (File f : entries) {
            if (f.isFile()) {
                paths.add(new FilePath(f, prefix));
            } else {
                String newPrefix = prefix;
                if (prefix.length() > 0) {
                    newPrefix = prefix.concat("/").concat(f.getName());
                } else {
                    newPrefix = f.getName();
                }
                paths.addAll(getPaths(f, newPrefix));
            }
        }

        return paths;
    }


    @Override
    public List<Path> getPaths() {
        return getPaths(rootDirectory, "");

    }

    @Override
    public VersionedResource getResource(String path) throws RepositoryException {
        if (path == null) {
            return null;
        }
        File entry = checkAndGetResourceFile(path);
        if (entry.exists()) {
            return new FileResource(entry, new FileVersion(rootDirectory));
        }
        // not found
        return null;
    }


    protected String getFilePattern(String path) {
        return path.replace("/", File.separator);
    }

    protected File checkAndGetResourceFile(String path) throws RepositoryException {
        File repositoryRootDir;
        try {
            repositoryRootDir = rootDirectory.getCanonicalFile();
        } catch (IOException e) {
            throw new RepositoryException("Cannot get repository directory", e);
        }
        File resourceFile;
        try {
            resourceFile = new File(repositoryRootDir, getFilePattern(path)).getCanonicalFile();
        } catch (IOException e) {
            throw new RepositoryException("Cannot get file to write resource", e);
        }
        if (!resourceFile.getAbsolutePath().startsWith(repositoryRootDir.getAbsolutePath())) {
            throw new RepositoryException("Try to read a resource '" + path + "' in a different directory than the repository, this is forbidden");
        }
        return resourceFile;

    }

}

