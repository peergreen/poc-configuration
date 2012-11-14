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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;

import com.peergreen.configuration.api.ConfigRepository;
import com.peergreen.configuration.api.Read;
import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.Version;
import com.peergreen.configuration.api.Write;

/**
 *
 * @author Florent Benoit
 */
public class GitRepository implements ConfigRepository {

    private final GitManager gitManager;

    public GitRepository(Repository repository) {
        this.gitManager = new GitManager(repository);
    }

    @Override
    public List<Version> getVersions() throws RepositoryException {

        List<Version> versions = new ArrayList<Version>();
        List<Ref> tags = null;
        try {
            tags = gitManager.git().tagList().call();
        } catch (GitAPIException e) {
            throw new RepositoryException("Unable to get the versions", e);

        }
        for (Ref ref : tags) {
            versions.add(new GitVersion(ref));
        }

        return versions;

    }

    @Override
    public Version getProductionVersion() throws RepositoryException {
        String name = "current-version";

        Ref ref = null;
        try {
            ref = gitManager.repository().getRef(Constants.R_TAGS + name);
        } catch (IOException e) {
            throw new RepositoryException("Unable to get the production version", e);
        }

        if (ref != null && ref.isSymbolic()) {
            name = ref.getTarget().getName().substring(Constants.R_TAGS.length());
        }

        if (ref == null) {
            return null;
        }

        return new GitVersion(name);
    }

    @Override
    public void setProductionVersion(Version version) throws RepositoryException {

        // Check version exists
        if (!gitManager.existRef(version.getName())) {
            throw new RepositoryException("Want to set new version to '" + version.getName()
                    + "' but it doesn't exists.");
        }

        // Now, add the symbolic-ref for the current-version
        RefUpdate newHead = null;
        try {
            newHead = gitManager.repository().updateRef(Constants.R_TAGS + "current-version");
        } catch (IOException e) {
            throw new RepositoryException("Unable to set the production version", e);
        }

        newHead.disableRefLog();
        try {
            newHead.link(Constants.R_TAGS + version.getName());
        } catch (IOException e) {
            throw new RepositoryException("Unable to set the production version", e);
        }

    }

    @Override
    public Write init() throws RepositoryException {
        return init(null);
    }

    @Override
    public Write init(Version version) throws RepositoryException {
        return new GitWrite(this, version);
    }

    @Override
    public Read read(Version version) throws RepositoryException {
        if (version == null) {
            if (getProductionVersion() == null) {
                return null;
            }
        }
        return new GitRead(this, version);
    }

    @Override
    public Read read() throws RepositoryException {

        return read(null);
    }

    public GitManager getGitManager() {
        return gitManager;
    }

    @Override
    public boolean supportsHistory() {
        return true;
    }

}
