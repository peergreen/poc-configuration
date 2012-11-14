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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

import org.testng.Assert;

import com.peergreen.configuration.api.Path;
import com.peergreen.configuration.api.Read;
import com.peergreen.configuration.api.RepositoryException;
import com.peergreen.configuration.api.VersionedResource;
import com.peergreen.configuration.api.Write;

/**
 *
 * @author Florent Benoit
 */
public abstract class AbsTest {

    protected Path foundPath(List<Path> paths, String pathName) {

        for (Path path : paths) {
            if (path.name().equals(pathName)) {
                return path;
            }
        }
        return null;

    }

    protected String getContent(Read read, String path) throws IOException, RepositoryException {
        VersionedResource getResource = read.getResource(path);
        Assert.assertNotNull(getResource);

        // Check content
        String content = readContent(getResource.openStream());

        return content;
    }

    protected String readContent(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(inputStreamReader);
            String read = br.readLine();

            while (read != null) {
                if (sb.length() != 0) {
                    sb.append("\n");
                }
                sb.append(read);

                read = br.readLine();

            }
        } finally {
            inputStreamReader.close();
            is.close();
        }
        return sb.toString();

    }

    protected void checkEntry(Write write, String path) throws IOException, RepositoryException {
        checkEntry(write, path, null);
    }

    protected void checkEntry(Write write, String path, String content) throws IOException, RepositoryException {
        // Prepare content
        String newContent = null;
        if (content == null) {
            UUID uuid = UUID.randomUUID();
            newContent = "entry" + uuid.toString() + "\n" + "line" + uuid.toString();
        } else {
            newContent = content;
        }

        // Write resource
        long now = System.currentTimeMillis();
        write.pushResource(path, new DummyContentResource(newContent));

        // Get resource
        VersionedResource getResource = write.getResource(path);
        Assert.assertNotNull(getResource);

        // Check content
        String insertedLine = readContent(getResource.openStream());
        Assert.assertEquals(insertedLine, newContent);

        // Check size
        Assert.assertEquals(newContent.getBytes().length, getResource.getSize());

        // should have be done in less than one second
        long lastModified = getResource.lastModified();
        Assert.assertTrue(Math.abs(lastModified - now) < 1000);

        // check new path is here
        Path newPath = foundPath(write.getPaths(), path);
        Assert.assertNotNull(newPath);

    }
}
