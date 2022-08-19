package org.commonjava.service.storage.util;

import java.time.Duration;
import java.util.ArrayList;
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
     * Separate dirs and files and sort them.
     * @param list
     * @return
     */
    public static String[] sort(String[] list) {
        List<String> dirs = new LinkedList<>();
        List<String> files = new LinkedList<>();
        for (int i = 0; i < list.length; i++) {
            if (list[i].endsWith("/")) {
                dirs.add( list[i] );
            } else {
                files.add( list[i] );
            }
        }
        Collections.sort(dirs);
        Collections.sort(files);
        List<String> result = new ArrayList<>();
        result.addAll(dirs);
        result.addAll(files);
        return result.toArray(new String[0]);
    }
}
