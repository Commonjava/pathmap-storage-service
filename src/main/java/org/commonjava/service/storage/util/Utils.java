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
}
