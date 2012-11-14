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
@Test(groups="TestInitRepository")
public class TestInitRepository extends AbsTest {

    private final ConfigRepository repository;

    public TestInitRepository(ConfigRepository repository) {
        this.repository = repository;
    }

    // No versions at startup
    @Test
    public void checkVersions()  throws RepositoryException {
        Assert.assertEquals(repository.getVersions().size(), 0);
    }

    // No production version
    @Test
    public void checkProductionVersion() throws RepositoryException {
        Assert.assertNull(repository.getProductionVersion());
    }

    @Test(expectedExceptions=RepositoryException.class)
    public void checkSetProductionVersionInvalidVersion() throws RepositoryException {
        Version illegalVersion = new DummyVersion("UNKNOWN_VERSION");
        repository.setProductionVersion(illegalVersion);
    }


    @Test(expectedExceptions=RepositoryException.class)
    public void checkReadInvalidVersion() throws RepositoryException {
        Version illegalVersion = new DummyVersion("UNKNOWN_VERSION");
        repository.read(illegalVersion);
    }


    @Test
    public void checkDefaultRead() throws RepositoryException {
        // Should not work as there is no production version yet
        Read read = repository.read();
        Assert.assertNull(read);
    }


    @Test
    public void checkNullRead() throws RepositoryException {
        // Should not work as there is no production version yet
        Read read = repository.read(null);
        Assert.assertNull(read);
    }


    @Test(expectedExceptions=RepositoryException.class)
    public void checkWriteInvalidVersion() throws RepositoryException {
        Version illegalVersion = new DummyVersion("UNKNOWN_VERSION");
        // version doesn't exist, this should fail
        repository.init(illegalVersion);
    }

    @Test
    public void checkWriteNullVersion() throws RepositoryException {
        // init with empty
        Write writeView = repository.init(null);
        Assert.assertNotNull(writeView);
    }



}
