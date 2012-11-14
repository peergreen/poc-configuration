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

import org.junit.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.peergreen.configuration.api.ConfigRepository;
import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.Write;

/**
 *
 * @author Florent Benoit
 */
public class TestHistoryNotSupportedRepository extends AbsTest {

    private final ConfigRepository repository;

    private Write writeView;

    public TestHistoryNotSupportedRepository(ConfigRepository repository) {
        this.repository = repository;

    }

    @BeforeTest
    public void init() throws RepositoryException {
        this.writeView = repository.init();
        Assert.assertFalse(repository.supportsHistory());
    }


    @Test(expectedExceptions=IllegalStateException.class)
    public void checkRedoable() throws RepositoryException {
        writeView.isRedoable();
    }

    @Test(expectedExceptions=IllegalStateException.class)
    public void checkUndoable() throws RepositoryException {
        writeView.isUndoable();
    }

    @Test(expectedExceptions=IllegalStateException.class)
    public void checkReset() throws RepositoryException {
        writeView.resetChanges();
    }

    @Test(expectedExceptions=IllegalStateException.class)
    public void checkResetPath() throws RepositoryException {
        writeView.resetChanges("toto.xml");
    }

    @Test(expectedExceptions=IllegalStateException.class)
    public void checkUndo() throws RepositoryException {
        writeView.undo();
    }

    @Test(expectedExceptions=IllegalStateException.class)
    public void checkRedo() throws RepositoryException {
        writeView.redo();
    }
}