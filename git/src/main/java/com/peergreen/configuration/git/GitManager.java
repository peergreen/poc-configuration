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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.Version;

/**
 *
 * @author Florent Benoit
 */
public class GitManager {

    private final Git git;

    private final Repository repository;

    public GitManager(Repository repository) {
        this.repository = repository;
        this.git = new Git(repository);
    }

    public Repository repository() {
        return repository;
    }

    public Git git() {
        return git;
    }

    public boolean existRef(String refName) throws RepositoryException {
        Ref checkRef = null;
        try {
            checkRef = repository.getRef(refName);
        } catch (IOException e) {
            throw new RepositoryException("Cannot check if the reference '" + refName + "' exists.", e);
        }

        return checkRef != null;

    }

    public ObjectId getHead() throws RepositoryException {
        try {
            return repository.resolve(Constants.HEAD);
        } catch (IOException e) {
            throw new RepositoryException("Cannot get HEAD reference", e);
        }
    }

    public ObjectId getObjectidForVersion(Version version) throws RepositoryException {
        Ref ref = null;

        try {
            ref = repository.getRef(version.getName());
        } catch (IOException e) {
            throw new RepositoryException("Cannot get revision for the given version '" + version.getName() + "'", e);
        }
        if (ref != null) {
            ObjectId id = ref.getPeeledObjectId();
            if (id == null) {
                id = ref.getObjectId();
            }

            return id;
        }
        return null;
    }

    public RevCommit getCommitForId(ObjectId objectId) throws RepositoryException {

        RevWalk walk = new RevWalk(repository());

        RevCommit revCommitVersion = null;
        try {
            revCommitVersion = walk.parseCommit(objectId);
        } catch (IOException e) {
            throw new RepositoryException("Cannot get commit for objectId '" + objectId + "'", e);
        } finally {
            walk.dispose();
        }

        return revCommitVersion;

    }

}
