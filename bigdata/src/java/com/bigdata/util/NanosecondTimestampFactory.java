/**

Copyright (C) SYSTAP, LLC 2006-2015.  All rights reserved.

Contact:
     SYSTAP, LLC
     2501 Calvert ST NW #106
     Washington, DC 20008
     licenses@systap.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
/*
 * Created on Oct 26, 2006
 */

package com.bigdata.util;

/**
 * Timestamp factory class with no more than nanosecond resolution - values
 * produced by this class MUST NOT be persisted.
 * <p>
 * Note: There are several problems with {@link System#nanoTime()} to date and
 * the values MUST NOT be persisted. The underlying problem is that the epoch
 * MAY (and in practice does) change from VM instance to VM instance, often when
 * the machine is rebooted. For this reason, nano time can appear to "go
 * backward" rendering it unsuitable for placing timestamps on commit records.
 * This means that we do not have access to time-based method with more than
 * millisecond resolution of creating "distinctions" for transaction
 * identifiers.
 * <p>
 * Note: Nano time could be made to work in a robust nano time service as long
 * as the base time for the service is adjusted on service start or rollover to
 * never go backward.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class NanosecondTimestampFactory {

    static long lastNanoTime = System.nanoTime();

    /**
     * Generates a timestamp with nanosecond precision that is guarenteed to be
     * distinct from the last timestamp generated by this method within the same
     * VM instance.
     * 
     * @return A timestamp with nanosecond precision that MUST NOT be persisted.
     */
    public static long nextNanoTime() {

        final int limit = 1000;

        int i = 0;
        
        long nanoTime;
        
        do {
            
            nanoTime = System.nanoTime();
            
            if( i++ >= limit ) throw new AssertionError();
            
        } while( nanoTime == lastNanoTime );
        
        if(nanoTime<lastNanoTime) {

            throw new AssertionError("Nano time goes backward: lastNanoTime="
                    + lastNanoTime + ", nanoTime=" + nanoTime);
            
        }
        
        lastNanoTime = nanoTime;
        
        return nanoTime;
    
    }

}
