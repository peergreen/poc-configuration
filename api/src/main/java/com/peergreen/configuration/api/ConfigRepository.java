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

package com.peergreen.configuration.api;

import java.util.List;

/**
 *
 * @author Florent Benoit
 */
public interface ConfigRepository {

    /**
     * List versions
     *
     * @return
     */
    List<Version> getVersions() throws RepositoryException;

    /**
     * Version used in production ?
     *
     * @return
     */
    Version getProductionVersion() throws RepositoryException;

    /**
     *
     * @param newVersion
     * @throws RepositoryException
     */
    void setProductionVersion(Version newVersion) throws RepositoryException;

    /**
     * Gets read access from a specific version
     *
     * @param version
     * @return
     */
    Read read(Version version) throws RepositoryException;

    /**
     * Gets read access to the production view
     *
     * @return
     */
    Read read() throws RepositoryException;

    /**
     * Allows to create a new version Based on the current production version
     * (if there is one) or from the initial empty repository.
     */
    Write init() throws RepositoryException;

    /**
     * New developments will be based from this version
     *
     * @param version
     * @return
     */
    Write init(Version version) throws RepositoryException;

    /**
     * true if repository manages history
     *
     * @return
     */
    boolean supportsHistory();

}
