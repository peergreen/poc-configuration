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

import static org.eclipse.jgit.lib.Constants.R_HEADS;
import static org.eclipse.jgit.lib.Constants.R_NOTES;
import static org.eclipse.jgit.lib.Constants.R_REMOTES;
import static org.eclipse.jgit.lib.Constants.R_TAGS;

/**
 *
 * @author Florent Benoit
 */
public class GitUtils {

    public static String getShortName(String objectId) {
        if (objectId.startsWith(R_HEADS)) {
            return objectId.substring(R_HEADS.length());
        } else if (objectId.startsWith(R_REMOTES)) {
            return objectId.substring(R_REMOTES.length());
        } else if (objectId.startsWith(R_TAGS)) {
            return objectId.substring(R_TAGS.length());
        } else if (objectId.startsWith(R_NOTES)) {
            return objectId.substring(R_NOTES.length());
        }
        return objectId;
    }

}
