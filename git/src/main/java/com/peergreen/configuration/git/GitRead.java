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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.peergreen.configuration.api.ConfigRepository;
import com.peergreen.configuration.api.Path;
import com.peergreen.configuration.api.Read;
import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.Version;
import com.peergreen.configuration.api.VersionedResource;

/**
 *
 * @author Florent Benoit
 */
public class GitRead implements Read {

    private final ConfigRepository gitRepository;

    private final GitManager gitManager;

    private ObjectId objectIdRevision;

    private final Version version;



    public GitRead(GitRepository gitRepository, Version version) throws RepositoryException {
        this.gitRepository = gitRepository;
        this.gitManager = gitRepository.getGitManager();
        if (version == null ) {
            version = gitRepository.getProductionVersion();
        }
        if (version == null ) {
            // No production version, needs to get the master branch
            this.objectIdRevision = gitManager.getHead();
        } else {
            ObjectId revision = gitManager.getObjectidForVersion(version);
            if (revision == null) {
                throw new RepositoryException("Unable to initialize a repository with an invalid version '" + version.getName() + "'");
            }
            this.objectIdRevision = revision;
        }
        this.version = version;
    }


    public GitRead(GitRepository gitRepository) throws RepositoryException {
        this(gitRepository, gitRepository.getProductionVersion());
    }



    protected File checkAndGetResourceFile(String path) throws RepositoryException {
        File repositoryRootDir;
        try {
            repositoryRootDir = getGitManager().repository().getWorkTree().getCanonicalFile();
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


    @Override
    public VersionedResource getResource(String path) throws RepositoryException {

        // Check resource is not in upper/outside directories
        checkAndGetResourceFile(path);


        RevWalk walk = new RevWalk(gitManager.repository());

        RevCommit revCommitVersion = null;
        try {
            revCommitVersion = walk.parseCommit(objectIdRevision);
        } catch (IOException e) {
            throw new RepositoryException("Unable to get the resource for the given path '" + path + "'", e);
        }


        int lastModified = revCommitVersion.getCommitTime();

        TreeWalk treeWalk = null;
        try {
            treeWalk = TreeWalk.forPath(gitManager.repository(), path, revCommitVersion.getTree());
        } catch (IOException e) {
            throw new RepositoryException("Unable to get the resource for the given path '" + path + "'", e);
        }

        // Path not found
        if (treeWalk == null) {
            return null;
        }

        ObjectId foundId = treeWalk.getObjectId(0);


        ObjectLoader objectLoader = null;
        try {
            objectLoader = gitManager.repository().open(foundId);
        } catch (IOException e) {
            throw new RepositoryException("Unable to get the resource for the given path '" + path + "'", e);
        } finally {
            walk.dispose();
        }

        return new GitResource(objectLoader, 1000L * lastModified, version);

    }

    public ObjectId getObjectIdRevision() {
        return objectIdRevision;
    }


    public void setObjectIdRevision(ObjectId objectIdRevision) {
        this.objectIdRevision = objectIdRevision;
    }


    @Override
    public List<Path> getPaths() throws RepositoryException {

        List<Path> paths = new ArrayList<Path>();

        TreeWalk treeWalk = new TreeWalk(gitManager.repository());
        treeWalk.setRecursive(true);

        RevWalk revWalk = new RevWalk(gitManager.repository());

        try {

            treeWalk.addTree(revWalk.parseTree(objectIdRevision));

            while (treeWalk.next()) {
                String path = treeWalk.getPathString();
                VersionedResource resource = getResource(path);

                paths.add(new GitPath(path, resource.getSize(), resource.lastModified()));
            }
        } catch (IOException e) {
            throw new RepositoryException("Unable to get the paths", e);
        } finally {
            revWalk.dispose();
        }

        return paths;

    }


    protected String getFilePattern(String path) {
        return path.replace("/", File.separator);
    }

    public ConfigRepository getGitRepository() {
        return gitRepository;
    }

    public GitManager getGitManager() {
        return gitManager;
    }

    public Version getVersion() {
        return version;
    }
}
