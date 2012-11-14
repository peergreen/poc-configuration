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

import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.storage.file.FileRepository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import com.peergreen.configuration.api.ConfigRepository;
import com.peergreen.configuration.api.Configuration;
import com.peergreen.configuration.api.RepositoryException;

/**
 *
 * @author Florent Benoit
 */
public class GitConfiguration implements Configuration {

    private File rootDirectory = null;

    @Override
    public ConfigRepository getRepository(String name) throws RepositoryException {
        check();

        // Sets the git directory from the given repository name
        File repositoryDir = new File(rootDirectory, name);
        File gitDir = new File(repositoryDir, Constants.DOT_GIT);

        // Find the git directory
        FileRepository fileRepository = null;
        try {
            fileRepository = new FileRepositoryBuilder() //
                    .setGitDir(gitDir) // --git-dir if supplied, no-op if null
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir().build();
        } catch (IOException e) {
            throw new RepositoryException("Unable to find a repository for the path '" + name + "'.", e);
        }

        // do not exist yet on the filesystem, create all the directories
        if (!gitDir.exists()) {
            try {
                fileRepository.create();
            } catch (IOException e) {
                throw new RepositoryException("Cannot create repository", e);
            }

            // Create the first commit in order to initiate the repository.
            Git git = new Git(fileRepository);
            CommitCommand commit = git.commit();
            try {
                commit.setMessage("Initial setup for the git configuration of '" + name + "'").call();
            } catch (GitAPIException e) {
                throw new RepositoryException("Cannot init the git repository '" + name + "'", e);
            }

        }

        return new GitRepository(fileRepository);

    }

    @Override
    public void setRootDirectory(File directory) throws RepositoryException {
        if (rootDirectory != null) {
            throw new RepositoryException("Cannot change repository directory once it has been setup");
        }
        this.rootDirectory = directory;
    }

    protected void check() throws RepositoryException {
        if (rootDirectory == null) {
            throw new RepositoryException("The root directory to store the configuration has not been set.");
        }
    }

}
