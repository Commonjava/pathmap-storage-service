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
