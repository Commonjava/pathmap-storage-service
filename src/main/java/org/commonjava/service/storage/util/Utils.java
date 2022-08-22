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
