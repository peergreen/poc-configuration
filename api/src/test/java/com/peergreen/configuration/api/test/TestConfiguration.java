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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.ow2.util.file.FileUtils;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.peergreen.configuration.api.ConfigRepository;
import com.peergreen.configuration.api.Configuration;
import com.peergreen.configuration.api.RepositoryException;

/**
 *
 * @author Florent Benoit
 */
public class TestConfiguration {

    private final Configuration configuration;

    private ConfigRepository repository;


    public TestConfiguration(Configuration configuration) throws RepositoryException {
        this.configuration = configuration;
        configureStoreDirectory();
        initTestRepository();
    }


    public void configureStoreDirectory() throws RepositoryException {
        File rootDirectory =  new File("target", TestConfiguration.class.getName());
        if (rootDirectory.exists()) {
            boolean deleted = FileUtils.delete(rootDirectory);
            if (!deleted) {
                throw new IllegalStateException("Cannot delete directory '" + rootDirectory + "'");
            }
        }
        // Sets the root directory
        configuration.setRootDirectory(rootDirectory);



    }

    public void initTestRepository() throws RepositoryException {
        // Create two repositories
        this.repository = configuration.getRepository("repository1");

    }


    @Test(expectedExceptions=RepositoryException.class)
    public void testChangeRootDirectory() throws RepositoryException {
        configuration.setRootDirectory(new File("target", TestConfiguration.class.getName().concat("new")));
        Assert.fail("Shouldn't be able to change the root directory");
    }



    @Factory()
    public Object[] createTestFromScratch() throws RepositoryException {
        List<Object> lists = new ArrayList<Object>();

        Assert.assertNotNull(repository);


        // First test with empty repository
        lists.add(new TestInitRepository(repository));

        // Now try to populate the repository
        lists.add(new TestPopulateRepository(repository, repository.init()));

        // Now try to reuse the repository
        lists.add(new TestReuseRepository(repository));

        // Try history on repository
        if (repository.supportsHistory()) {
            lists.add(new TestHistoryRepository(configuration.getRepository("history")));
        } else {
            lists.add(new TestHistoryNotSupportedRepository(configuration.getRepository("nohistory")));
        }


        return lists.toArray(new Object[lists.size()]);
     }




}
