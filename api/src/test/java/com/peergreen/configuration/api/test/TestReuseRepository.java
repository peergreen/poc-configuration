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
import java.util.List;
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.peergreen.configuration.api.ConfigRepository;
import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.Version;
import com.peergreen.configuration.api.Write;

/**
 *
 * @author Florent Benoit
 */
@Test(groups="TestReuseRepository", dependsOnGroups="TestPopulateRepository")
public class TestReuseRepository extends AbsTest {

    private final ConfigRepository repository;

    private Version checkoutVersion = null;

    private Write writeView = null;

    public TestReuseRepository(ConfigRepository repository) {
        this.repository = repository;
    }


    @Test
    public void getAVersion() throws RepositoryException {
        List<Version> versions = repository.getVersions();
        Assert.assertTrue(versions.size() > 0);

        // Checkout the first version
        checkoutVersion = versions.get(0);
    }


    @Test(dependsOnMethods="getAVersion")
    public void checkoutSpecificVersion() throws RepositoryException {
       this.writeView = repository.init(checkoutVersion);
    }

    @Test(dependsOnMethods="checkoutSpecificVersion")
    public void addAlreadyExistingEntries() throws IOException, RepositoryException {
        checkEntry(writeView, "test-entry.xml");
        checkEntry(writeView, "test-entry2.xml");

    }


    @Test(dependsOnMethods="addAlreadyExistingEntries")
    public void addNewEntries() throws IOException, RepositoryException {
        checkEntry(writeView, "test-entry3.xml");
        checkEntry(writeView, "test-entry4.xml");

    }

    @Test(dependsOnMethods="addNewEntries")
    public void addTags() throws IOException, RepositoryException {
        // Tag a version
        Version tagVersion = new DummyVersion(UUID.randomUUID().toString());
        writeView.tag(tagVersion);

        // start from the first version
        Write writeView2 = repository.init(checkoutVersion);
        Version tagVersion2 = new DummyVersion(UUID.randomUUID().toString());
        checkEntry(writeView2, "test-entry3.xml");
        checkEntry(writeView2, "test-entry4.xml");
        writeView2.tag(tagVersion2);
        repository.setProductionVersion(tagVersion2);

    }



}
