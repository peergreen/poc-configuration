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
import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.peergreen.configuration.api.ConfigRepository;
import com.peergreen.configuration.api.Path;
import com.peergreen.configuration.api.Read;
import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.Version;
import com.peergreen.configuration.api.VersionedResource;
import com.peergreen.configuration.api.Write;

/**
 *
 * @author Florent Benoit
 */
@Test(groups="TestPopulateRepository", dependsOnGroups="TestInitRepository")
public class TestPopulateRepository extends AbsTest {

    private final ConfigRepository repository;

    private final Write write;

    private Version tagVersion = null;

    public TestPopulateRepository(ConfigRepository repository, Write write) {
        this.repository = repository;
        this.write = write;
    }

    @Test
    public void insertFirstEntry() throws IOException, RepositoryException {
        checkEntry(write, "test-entry.xml");
    }


    @Test(dependsOnMethods="insertFirstEntry")
    public void insertAnotherEntry() throws IOException, RepositoryException {
        checkEntry(write, "test-entry2.xml");
    }

    @Test(dependsOnMethods="insertAnotherEntry")
    public void insertSamePathEntry() throws IOException, RepositoryException {
        checkEntry(write, "test-entry.xml");
        checkEntry(write, "test-entry2.xml");
    }

    @Test(dependsOnMethods="insertSamePathEntry", expectedExceptions=RepositoryException.class)
    public void insertNullPathEntry() throws RepositoryException {
        write.pushResource(null, new DummyContentResource("dummy"));
    }


    @Test(dependsOnMethods="insertNullPathEntry", expectedExceptions=RepositoryException.class)
    public void insertNullResourceEntry() throws RepositoryException {
        write.pushResource("toto", null);
    }

    @Test(dependsOnMethods="insertNullResourceEntry", expectedExceptions=RepositoryException.class)
    public void deleteNullEntry() throws RepositoryException {
        write.removeResource(null);
    }

    @Test(dependsOnMethods="deleteNullEntry")
    public void tagANewVersion() throws RepositoryException {
        this.tagVersion = new DummyVersion(UUID.randomUUID().toString());
        int currentVersions = repository.getVersions().size();
        write.tag(tagVersion);
        int afterVersions = repository.getVersions().size();
        Assert.assertTrue(afterVersions == (currentVersions + 1));
    }

    @Test(dependsOnMethods="tagANewVersion", expectedExceptions=RepositoryException.class)
    public void tryToTagAgain() throws RepositoryException {
        this.tagVersion = new DummyVersion(UUID.randomUUID().toString());
        int currentVersions = repository.getVersions().size();
        write.tag(tagVersion);
        int afterVersions = repository.getVersions().size();
        Assert.assertTrue(afterVersions == (currentVersions + 1));

        // Perform a new tag
        write.tag(tagVersion);

        Assert.fail("Writing a tag twice is not allowed");

    }

    @Test(dependsOnMethods="tagANewVersion")
    public void setProductionVersion() throws RepositoryException {
        Version oldProductionVersion = repository.getProductionVersion();

        // set new production version
        repository.setProductionVersion(tagVersion);

        // Check that it is using the right version
        Version newProductionVersion = repository.getProductionVersion();
        if (oldProductionVersion != null) {
            Assert.assertNotEquals(newProductionVersion.getName(), oldProductionVersion.getName());
        }
        Assert.assertEquals(tagVersion.getName(), newProductionVersion.getName());

    }

    // check read
    @Test(dependsOnMethods="setProductionVersion")
    public void checkReadAccess() throws RepositoryException, IOException {
        Read readProduction = repository.read();
        Read readTag = repository.read(tagVersion);

        VersionedResource resProd = readProduction.getResource("test-entry.xml");
        VersionedResource resTag = readTag.getResource("test-entry.xml");
        Assert.assertNotNull(resProd);
        Assert.assertNotNull(resTag);

        String contentProd = readContent(resProd.openStream());
        String contentTag = readContent(resTag.openStream());

        Assert.assertEquals(contentProd,  contentTag);

    }

    @Test(dependsOnMethods="checkReadAccess")
    public void checkAddRemoveResource() throws IOException, RepositoryException {
        String path = "will-be-destroyed.xml";
        checkEntry(write, path);
        VersionedResource pathResource = write.getResource(path);
        Assert.assertNotNull(pathResource);


        write.removeResource(path);
        pathResource = write.getResource(path);
        // check path is gone
        Path newPath = foundPath(write.getPaths(), path);
        Assert.assertNull(newPath);
        Assert.assertNull(pathResource);

    }

    @Test
    public void checkEntryWithSlashes() throws IOException, RepositoryException {
        String path = "entry/with/slashes/entry.xml";
        checkEntry(write, path);
    }

    @Test
    public void checkEntryWithBackSlashes() throws IOException, RepositoryException {
        String path = "entry\\with\\backslashes\\entry.xml";
        checkEntry(write, path);
    }

    @Test(expectedExceptions=RepositoryException.class)
    public void checkPushRelativePathNotAuthorized() throws RepositoryException {
        // Write resource
        write.pushResource("../before-content.xml", new DummyContentResource("content"));
        Assert.fail("shouldn't be able to write in super directories");
    }

    @Test(dependsOnMethods="checkPushRelativePathNotAuthorized", expectedExceptions=RepositoryException.class)
    public void checkGetRelativePathNotAuthorized() throws RepositoryException {
        // read
        VersionedResource resource = write.getResource("../before-content.xml");
        Assert.fail("Shouldn't be able to read upper level, resource = " + resource);
    }


}
