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

package com.peergreen.configuration.api.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.peergreen.configuration.api.ConfigRepository;
import com.peergreen.configuration.api.Read;
import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.Version;
import com.peergreen.configuration.api.Write;

/**
 *
 * @author Florent Benoit
 */
@Test
public class TestHistoryRepository extends AbsTest {

    private static final String FILE1 = "history-file1.xml";
    private static final String FILE2 = "history-file2.xml";
    private static final String FILE3 = "history-file3.xml";
    private static final String FILE4 = "history-file4.xml";


    private final ConfigRepository repository;

    private Read originalView = null;

    private Version startVersion = null;

    private int version = 1;

    public TestHistoryRepository(ConfigRepository repository) {
        this.repository = repository;
    }

    @Test
    public void populate() throws IOException, RepositoryException {
        Write initWrite = repository.init();
        this.startVersion = new DummyVersion("V" + version++);
        // Add content and tag this version
        checkEntry(initWrite, FILE1, "history1");
        checkEntry(initWrite, FILE2, "history2");
        checkEntry(initWrite, FILE3, "history3");

        initWrite.tag(startVersion);
        repository.setProductionVersion(startVersion);
        originalView = repository.read();

    }

    @Test(dependsOnMethods="populate")
    public void checkResetOnePath() throws IOException, RepositoryException {
        // write on files
        Write myView = repository.init(startVersion);

        // add entry
        checkEntry(myView, FILE1, "history1a");

        // check different content
        String newContent = getContent(myView, FILE1);
        String oldContent = getContent(originalView, FILE1);

        // Content not the same
        Assert.assertNotEquals(newContent, oldContent);

        // now check that we can revert one item
        myView.resetChanges(FILE1);
        // update content
        newContent = getContent(myView, FILE1);

        // check that we've now the same content than before
        Assert.assertEquals(oldContent, newContent, "History is not working");
    }



    @Test(dependsOnMethods="checkResetOnePath")
    public void checkResetManyPaths() throws IOException, RepositoryException {
        // write on files
        Write myView = repository.init(startVersion);

        // add entry
        checkEntry(myView, FILE1, "history1a");
        checkEntry(myView, FILE2, "history2a");

        // check different content
        String newContent1 = getContent(myView, FILE1);
        String oldContent1 = getContent(originalView, FILE1);
        String newContent2 = getContent(myView, FILE2);
        String oldContent2 = getContent(originalView, FILE2);
        String newContent3 = getContent(myView, FILE3);
        String oldContent3 = getContent(originalView, FILE3);


        // Content not the same
        Assert.assertNotEquals(newContent1, oldContent1);
        Assert.assertNotEquals(newContent2, oldContent2);

        // FILE 3 not modified, should be the same
        Assert.assertEquals(newContent3, oldContent3);


        // now check that we can revert one item
        myView.resetChanges();
        // update content
        newContent1 = getContent(myView, FILE1);
        newContent2 = getContent(myView, FILE2);
        newContent3 = getContent(myView, FILE3);

        // check that we've now the same content than before
        Assert.assertEquals(oldContent1, newContent1, "History is not working");
        Assert.assertEquals(oldContent2, newContent2, "History is not working");
        Assert.assertEquals(oldContent3, newContent3, "History is not working");
    }


    /**
     * Simple : we commit <LOOP> times, undo <LOOP> times and redo <LOOP> times
     * @throws RepositoryException
     */
    @Test(dependsOnMethods="checkResetManyPaths")
    public void checkUndoReDo() throws IOException, RepositoryException {
        Write myView = repository.init(startVersion);

        int LOOP = 10;

        List<String> contentList = new ArrayList<String>();

        // create LOOP changes
        for (int i = 1; i <= LOOP; i++) {
            String content = "history3.0." + i;
            contentList.add(content);
            checkEntry(myView, FILE4, content);
        }

        // Ok, let's see if we can undo and until when ?
        int currentLoop = 1;
        while (myView.isUndoable() && currentLoop < LOOP) {
            // it's undoable so try to revert
            boolean undo  = myView.undo();
            // this should be done without problems
            Assert.assertTrue(undo);

            // Check content
            String undoContent = getContent(myView, FILE4);

            Assert.assertEquals(contentList.get(LOOP - currentLoop - 1), undoContent);
            currentLoop++;
        }

        // we can't undo anymore
        Assert.assertFalse(myView.isUndoable());
        Assert.assertEquals(10, currentLoop);

        // Now we'll try to redo
        currentLoop = 1;
        while (myView.isRedoable() && currentLoop < LOOP) {
            // it's redoable so try to redot
            boolean redo  = myView.redo();
            // this should be done without problems
            Assert.assertTrue(redo);

            // Check content
            String redoContent = getContent(myView, FILE4);

            Assert.assertEquals(contentList.get(currentLoop), redoContent);
            currentLoop++;
        }

        // we can't redo anymore
        Assert.assertFalse(myView.isRedoable());
        Assert.assertEquals(10, currentLoop);

        myView.tag(new DummyVersion("vSimpleUndoRedo"));


    }

    /**
     * We commit 10 times, undo 5 times and commit a new time.
     * We shouldn't be able to redo "lost" commits as we've committed.
     * @throws IOException
     * @throws RepositoryException
     */
    @Test(dependsOnMethods="checkUndoReDo")
    public void checkComplexUndoReDo() throws IOException, RepositoryException {
        Write myView = repository.init(startVersion);

        int LOOP = 10;
        int UNDOLOOP_BEFORECOMMIT = 5;
        int NEWHISTORY_LOOP = LOOP - UNDOLOOP_BEFORECOMMIT;

        List<String> contentList = new ArrayList<String>();
        List<String> resetContentList = new ArrayList<String>();

        // create LOOP changes
        for (int i = 1; i <= LOOP; i++) {
            String content = "history3.0." + i;
            contentList.add(content);
            if (i <= UNDOLOOP_BEFORECOMMIT) {
                resetContentList.add(content);
            }
            checkEntry(myView, FILE4, content);
        }

        // Ok, let's undo 5 times
        int currentLoop;
        for (currentLoop =1; currentLoop <= LOOP - UNDOLOOP_BEFORECOMMIT; currentLoop++) {
            Assert.assertTrue(myView.isUndoable());

            // it's undoable so try to revert
            boolean undo  = myView.undo();
            // this should be done without problems
            Assert.assertTrue(undo);

            // Check content
            String undoContent = getContent(myView, FILE4);

            Assert.assertEquals(contentList.get(LOOP - currentLoop - 1), undoContent);
        }

        // we can undo anymore
        Assert.assertTrue(myView.isUndoable());
        // loop 5 times
        Assert.assertEquals(UNDOLOOP_BEFORECOMMIT + 1, currentLoop);

        // Now we'll commit
        String newMajorCommitContent = "history3.1.0";
        resetContentList.add(newMajorCommitContent);
        checkEntry(myView, FILE4, newMajorCommitContent);

        // Ensure that redoing is not possible
        Assert.assertFalse(myView.isRedoable());

        // But that we can still go back and redo this new history

        // Ok, let's see if we can undo and until when ?
        currentLoop = 1;
        while (myView.isUndoable() && currentLoop <= NEWHISTORY_LOOP) {
            // it's undoable so try to revert
            boolean undo  = myView.undo();
            // this should be done without problems
            Assert.assertTrue(undo);

            // Check content
            String undoContent = getContent(myView, FILE4);

            Assert.assertEquals(resetContentList.get(NEWHISTORY_LOOP - currentLoop ), undoContent);
            currentLoop++;
        }

        // we can't undo anymore
        Assert.assertFalse(myView.isUndoable());
        Assert.assertEquals(NEWHISTORY_LOOP + 1, currentLoop);

        // Now we'll try to redo
        currentLoop = 1;
        while (myView.isRedoable() && currentLoop < NEWHISTORY_LOOP + 1) {
            // it's redoable so try to redo
            boolean redo  = myView.redo();
            // this should be done without problems
            Assert.assertTrue(redo);

            // Check content
            String redoContent = getContent(myView, FILE4);

            Assert.assertEquals(resetContentList.get(currentLoop), redoContent);
            currentLoop++;
        }

        // we can't redo anymore
        Assert.assertFalse(myView.isRedoable());
        Assert.assertEquals(NEWHISTORY_LOOP + 1, currentLoop);






        myView.tag(new DummyVersion("vComplexUndoRedo"));


    }



    // Perform several tag from the same root version
    @Test(dependsOnMethods="checkUndoReDo")
    public void checkTagOnPreviousBranches() throws IOException, RepositoryException {
        for (int i = 1; i <= 10; i++) {
            // write on files
            Write myView = repository.init(startVersion);

            // add entry
            checkEntry(myView, FILE1, "history1b" + i);
            checkEntry(myView, FILE2, "history2b" + i);

            myView.tag(new DummyVersion("v1.0." + i));
        }
    }



}
