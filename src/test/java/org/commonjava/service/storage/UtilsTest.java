package org.commonjava.service.storage;

import org.junit.Test;

import java.time.Duration;

import static org.commonjava.service.storage.util.Utils.getDuration;
import static org.junit.Assert.assertEquals;

public class UtilsTest
{
    @Test
    public void getDurationTest()
    {
        Duration d = getDuration("pt5h" );
        assertEquals( 5, d.toHours() );

        d = getDuration("5h3m10s" );
        assertEquals( 5, d.toHours() );
        assertEquals( 5 * 60 + 3, d.toMinutes() );
        assertEquals(  ((5 * 60 + 3) * 60) + 10, d.toSeconds() );

        Duration d1 = getDuration("1d5h3m10s" );
        Duration d2 = getDuration("p1dt5h3m10s" );
        assertEquals( d1, d2 );
    }

}
