package org.commonjava.service.storage.util;

import java.time.Duration;

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
}
