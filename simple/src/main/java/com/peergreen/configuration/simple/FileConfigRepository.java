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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ow2.util.file.FileUtils;
import org.ow2.util.file.FileUtilsException;

import com.peergreen.configuration.api.ConfigRepository;
import com.peergreen.configuration.api.Read;
import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.Version;
import com.peergreen.configuration.api.Write;

/**
 * @author Florent Benoit
 */
public class FileConfigRepository implements ConfigRepository {

    private static final String WORK = "WORK";
    private static final String PRODUCTION = "PRODUCTION";

    private static final List<String> IGNORE_VERSIONS = Arrays.asList(new String[]{WORK, PRODUCTION});

    private File rootDirectory = null;
    private File workDirectory = null;
    private File productionDirectory = null;

    private Version productionVersion = null;


    public FileConfigRepository(File rootDirectory) {
        this.rootDirectory = rootDirectory;
        if (!this.rootDirectory.exists()) {
            rootDirectory.mkdirs();
        }
        this.workDirectory = new File(rootDirectory, WORK);
        this.productionDirectory = new File(rootDirectory, PRODUCTION);
        if (this.productionDirectory.exists()) {
            this.productionVersion = new FileVersion(productionDirectory);
        }

    }

    protected List<String> getVersionsNames() {
        List<String> versions = new ArrayList<String>();

        // We list directories and exclude some of them
        File[] directories = rootDirectory.listFiles();
        for (File directory : directories) {
            if (!IGNORE_VERSIONS.contains(directory.getName())) {
                versions.add(directory.getName());
            }
        }
        return versions;
    }


    @Override
    public List<Version> getVersions() {
        List<Version> versions = new ArrayList<Version>();
        for (String vName : getVersionsNames()) {
            versions.add(new FileVersion(vName));
        }
        return versions;
    }

    @Override
    public Version getProductionVersion() {
        return productionVersion;
    }


    protected void checkVersionExists(Version version, boolean checkNull) throws RepositoryException {
        if (version == null && checkNull) {
            throw new RepositoryException("Needs to specify a non-null version");
        }

        if (version != null) {
            // Check version exists
            if (!getVersionsNames().contains(version.getName())) {
                throw new RepositoryException("Version specified '" + version.getName()
                        + "' is not an available version");
            }
        }
    }

    @Override
    public void setProductionVersion(Version version) throws RepositoryException {
        checkVersionExists(version, true);
        // Copy content from given Version to production
        try {
            FileUtils.delete(productionDirectory);
            File fromDirectory = new File(rootDirectory, version.getName());
            FileUtils.copyDirectory(fromDirectory, productionDirectory);
        } catch (FileUtilsException e) {
            throw new RepositoryException("Unable to set production version to version '" + version.getName() + "'.", e);
        }

        this.productionVersion = version;
    }

    @Override
    public Read read(Version version) throws RepositoryException {
        // check version
        checkVersionExists(version, false);

        Version toGetVersion = version;

        // No view
        if (version == null) {
            if (productionVersion == null) {
                return null;
            }
            toGetVersion = productionVersion;
        }

        File versionFile = new File(rootDirectory, toGetVersion.getName());
        return new FileRead(versionFile);

    }

    @Override
    public Read read() throws RepositoryException {
        return read(null);
    }

    @Override
    public Write init() throws RepositoryException {
        return init(null);
    }

    @Override
    public Write init(Version version) throws RepositoryException {
        // check version
        checkVersionExists(version, false);

        // delete current working directory
        if (workDirectory.exists()) {
            FileUtils.delete(workDirectory);
        }
        File fromDirectory = null;

        // null : use production version
        if (version == null && productionVersion != null) {
            fromDirectory = new File(rootDirectory, productionVersion.getName());
        } else if (version != null) {
            fromDirectory = new File(rootDirectory, version.getName());
        }

        if (fromDirectory != null) {
            try {
                FileUtils.copyDirectory(fromDirectory, workDirectory);
            } catch (FileUtilsException e) {
                throw new RepositoryException("Unable to initialize a working copy from version '" + version.getName() + "'.", e);
            }
        } else {
            // just create an empty directory
            workDirectory.mkdirs();
        }

        return new FileWrite(workDirectory);
    }

    @Override
    public boolean supportsHistory() {
        return false;
    }

}
