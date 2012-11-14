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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.Resource;
import com.peergreen.configuration.api.Version;
import com.peergreen.configuration.api.Write;

/**
 *
 * @author Florent Benoit
 */
public class GitWrite extends GitRead implements Write {

    //FIXME: add synchronize on it
    private List<ObjectId> writeIdList;

    private ObjectId currentWriteId;

    private GitRead originalView = null;


    public GitWrite(GitRepository gitRepository, Version version) throws RepositoryException {
        super(gitRepository, version);
        this.originalView = new GitRead(gitRepository, version);
        this.writeIdList = new ArrayList<ObjectId>();
    }





    @Override
    public void tag(Version version) throws RepositoryException {

        if (currentWriteId == null) {
            throw new RepositoryException("Cannot tag a version, there were no changes");
        }


        // Check that the version doesn't exist
        if (getGitManager().existRef(Constants.R_TAGS + version.getName())) {
            throw new RepositoryException("Tag version '" + version.getName() + "' already exists.");
        }


        // Get commit of the current ObjectId
        RevWalk revWalk = new RevWalk(getGitManager().repository());
        RevCommit revCommit = null;
        try {
            revCommit = revWalk.parseCommit(currentWriteId);
        } catch (IOException e) {
            throw new RepositoryException("Unable to get commit from ID '" + currentWriteId + "' for production version '" + version.getName() + "'", e);
        } finally {
            revWalk.dispose();
        }

        TagCommand tagCommand = getGitManager().git().tag().setObjectId(revCommit).setName(version.getName());
        try {
            tagCommand.call();
        } catch (GitAPIException e) {
            throw new RepositoryException("Unable to set the production version with name '" + version.getName() + "'", e);
        }

    }


    protected void update(ObjectId newId) {
        // currentID available ? needs to reset the list after this commit.
        if (currentWriteId != null) {
            int toIndex = writeIdList.indexOf(currentWriteId);
            this.writeIdList = writeIdList.subList(0, toIndex + 1);
        }



        setObjectIdRevision(newId);
        this.currentWriteId = newId;
        this.writeIdList.add(newId);
    }



    @Override
    public void removeResource(String path) throws RepositoryException {
        if (path == null) {
            throw new RepositoryException("Cannot push an entry with a null path");
        }

        final String filePattern = getFilePattern(path);

        checkoutCurrentVersion();
        try {
            getGitManager().git().rm().addFilepattern(filePattern).call();
        } catch (GitAPIException e) {
            throw new RepositoryException("Cannot remove resource with the path '" + path + "'.", e);
        }

        commit(filePattern, "Remove the selected file '" + filePattern + "'.");
    }


    protected void checkoutCurrentVersion() throws RepositoryException {
        // if already stored something, reuse previous id
        ObjectId objectId = null;
        if (currentWriteId == null) {

        // get id from version
            objectId = getObjectIdRevision();
        } else {
            objectId = currentWriteId;
        }

        // get commit for this version
        RevCommit revCommitCheckout = getGitManager().getCommitForId(objectId);

        // Checkout the wanted version
        try {
            getGitManager().git().branchCreate().setStartPoint(revCommitCheckout).setForce(true).setName("master").call();
        } catch (GitAPIException e) {
            throw new RepositoryException("Cannot checkout the current version as master", e);
        }


        try {
            getGitManager().git().checkout().setName("master").call();
        } catch (GitAPIException e) {
            throw new RepositoryException("Cannot checkout the current version as master", e);
        }

    }


    protected void commit(String filePattern, String message) throws RepositoryException {

        // check if there are changes ?
        Status status = null;
        try {
            status = getGitManager().git().status().call();
        } catch (GitAPIException e) {
            throw new RepositoryException("Cannot commit the file pattern '" + filePattern + "' with message '" + message + ".", e);
        }

        // no changes, so do not commit
        if (status.isClean()) {
            return;
        }

        // Commit it
        RevCommit revCommit = null;
        try {
            revCommit = getGitManager().git().commit()
            .setCommitter("Peergreen Config Repository", "configrepository@peergreen.com")
            .setMessage(message)
            .setOnly(filePattern)
            .call();
        } catch (GitAPIException e) {
            throw new RepositoryException("Cannot commit the file pattern '" + filePattern + "' with message '" + message + ".", e);
        }

        // Update the writeId
        update(revCommit.getId());

    }


    @Override
    public void pushResource(String path, Resource resource) throws RepositoryException {
        if (path == null) {
            throw new RepositoryException("Cannot push an entry with a null path");
        }

            if (resource == null) {
                throw new RepositoryException("Cannot push to the path '" + path + "' with a null resource.");
            }

        final String filePattern = getFilePattern(path);

        checkoutCurrentVersion();


        // Dump the content
        InputStream inputStream = resource.openStream();

        // Check resource is not in upper/outside directories
        File writingFile = checkAndGetResourceFile(path);

        dumpFile(inputStream, writingFile);
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RepositoryException("Cannot write the stream in the file '" + filePattern + "'", e);
        }

        //TODO : check if in the path before adding ?
        // Add to the index if not yet existing
        try {
            getGitManager().git().add().addFilepattern(filePattern).call();
        } catch (GitAPIException e) {
            throw new RepositoryException("Cannot push entry with the pattern '" + filePattern + "'", e);
        }


        commit(filePattern, "New content for '" + filePattern + "'.");

    }


    /**
     * Size of the buffer.
     */
    private static final int BUFFER_SIZE = 4096;

    //FIXME : Replace with fileutils
    protected void dumpFile(InputStream inputStream, File file) {

        file.getParentFile().mkdirs();

        // File output
        FileOutputStream out = null;
        try {
          out = new FileOutputStream(file);
        int n = 0;
            // buffer
            byte[] buffer = new byte[BUFFER_SIZE];
            n = inputStream.read(buffer);
            while (n > 0) {
                out.write(buffer, 0, n);
                n = inputStream.read(buffer);
            }
            // Flush
            out.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }




    @Override
    public void resetChanges(String path) throws RepositoryException {

        final String filePattern = getFilePattern(path);

        // get commit for the production version on which we're based
        ObjectId objectId = originalView.getObjectIdRevision();//getGitManager().getObjectidForVersion(getVersion());
        RevCommit revCommitCheckout = getGitManager().getCommitForId(objectId);


        // we will perform a checkout only for a given path
        try {
            getGitManager().git().checkout().setStartPoint(revCommitCheckout).addPath(filePattern).call();
        } catch (GitAPIException e) {
            throw new RepositoryException("Cannot reset changes on the pattern path '" + filePattern + "'", e);
        }


        commit(filePattern, "reset changes on the file '" + filePattern + "'.");
    }


    @Override
    public void resetChanges() throws RepositoryException {
        // Cancel all changes
        currentWriteId = originalView.getObjectIdRevision();//getGitManager().getObjectidForVersion(getVersion());
        setObjectIdRevision(currentWriteId);
        writeIdList.clear();
        // go back to the production version of our origin
        checkoutCurrentVersion();
    }


    @Override
    public boolean isUndoable() {
        return currentWriteId != null && writeIdList.size() > 0 && writeIdList.indexOf(currentWriteId) > 0;
    }


    @Override
    public boolean undo() throws RepositoryException {
        if( isUndoable()) {
            int currentIndex =  writeIdList.indexOf(currentWriteId);
            currentIndex--;
            if (currentIndex < 0) {
                throw new IllegalStateException("External access already undo operations");
            }
            currentWriteId = writeIdList.get(currentIndex);
            setObjectIdRevision(currentWriteId);
            checkoutCurrentVersion();
            return true;
        }
        return false;
    }


    @Override
    public boolean redo() throws RepositoryException {
        if(isRedoable()) {
            int currentIndex =  writeIdList.indexOf(currentWriteId);
            currentIndex++;
            if (currentIndex > writeIdList.size() - 1) {
                throw new IllegalStateException("External access already redo operations");
            }
            currentWriteId = writeIdList.get(currentIndex);
            setObjectIdRevision(currentWriteId);
            checkoutCurrentVersion();
            return true;
        }
        return false;
    }


    @Override
    public boolean isRedoable() {
        return currentWriteId != null && writeIdList.size() > 0 && (writeIdList.indexOf(currentWriteId) < writeIdList.size() - 1);
    }


}
