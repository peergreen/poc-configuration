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

import org.eclipse.jgit.lib.Ref;

import com.peergreen.configuration.api.Version;

/**
 *
 * @author Florent Benoit
 */
public class GitVersion implements Version {

    private final String name;

    public GitVersion(Ref ref) {
        this(GitUtils.getShortName(ref.getName()));
    }

    public GitVersion(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
