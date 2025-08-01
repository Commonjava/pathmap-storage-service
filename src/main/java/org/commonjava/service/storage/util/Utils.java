/**
 * Copyright (C) 2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.service.storage.util;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

public class Utils {
    public static Duration getDuration( String timeout )
    {
        String lowerCased = timeout.toLowerCase();
        if ( !lowerCased.contains("p") ) // simplified format
        {
            StringBuilder sb = new StringBuilder("p");
            if ( lowerCased.contains("d") )
            {
                String[] tokens = lowerCased.split("d", 2 );
                String days = tokens[0];
                sb.append( days + "d" );
                if ( tokens.length >= 2 )
                {
                    String time = tokens[1];
                    sb.append( "t" + time );
                }
            }
            else
            {
                sb.append( "t" + lowerCased );
            }
            timeout = sb.toString();
        }
        Duration d = Duration.parse(timeout);
        return d;
    }

    /**
     * @param list strings to sort out
     */
    public static String[] sort(String[] list) {
        List<String> ret = new LinkedList<>();
        for (int i = 0; i < list.length; i++) {
            ret.add( list[i] );
        }
        Collections.sort(ret);
        return ret.toArray(new String[0]);
    }

    /**
     * Returns the depth (number of slashes) in the path, ignoring root.
     */
    public static int depth(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return 0;
        }
        String normalized = path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;
        int depth = 0;
        for (char c : normalized.toCharArray()) {
            if (c == '/') depth++;
        }
        return depth;
    }

    /**
     * Returns the parent path of a given path, or null if at root.
     */
    public static String getParentPath(String path) {
        if (path == null || path.isEmpty() || path.equals("/")) {
            return null;
        }
        String normalized = path.endsWith("/") && path.length() > 1 ? path.substring(0, path.length() - 1) : path;
        int lastSlashIndex = normalized.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return null;
        }
        return normalized.substring(0, lastSlashIndex);
    }

    /**
     * Given a collection of folder paths, returns a set of all those folders and their ancestors, 
     * up to but not including root.
     */
    public static Set<String> getAllCandidates(Collection<String> paths) {
        Set<String> allCandidates = new HashSet<>();
        if (paths != null) {
            for (String path : paths) {
                String current = path;
                while (current != null && !current.isEmpty() && !current.equals("/")) {
                    allCandidates.add(current);
                    current = getParentPath(current);
                }
            }
        }
        return allCandidates;
    }

    /**
     * Normalize a folder path for deletion: ensures a trailing slash (except for root).
     * This helps avoid issues with path handling in the underlying storage system.
     */
    public static String normalizeFolderPath(String path) {
        if (path == null || path.isEmpty() || "/".equals(path)) {
            return "/";
        }
        // Add trailing slashes
        String normalized = path;
        while (!normalized.endsWith("/")) {
            normalized = path + "/";
        }
        return normalized;
    }
}
