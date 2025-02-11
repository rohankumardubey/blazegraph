/*

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
 * Created on Jul 10, 2009
 */

package com.bigdata.service.jini.master;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A locator for a client task. This is just an integer in [0:N-1], where N is
 * the #of clients for the job.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class ClientLocator implements Externalizable {

    /**
     * 
     */
    private static final long serialVersionUID = 7289277874475092452L;
    
    private int clientNo;
   
    /**
     * Deserialization ctor.
     */
    public ClientLocator() {
        
    }
    
    public ClientLocator(final int clientNo) {
        
        this.clientNo = clientNo;
        
    }
    
    public int hashCode() {
        
        // hash code of int is the int.
        return clientNo;
        
    }
    
    public boolean equals(Object o) {

        return this == o
                || (o instanceof ClientLocator && clientNo == ((ClientLocator) o).clientNo);
        
    }

    public int getClientNo() {
        
        return clientNo;
        
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        
        clientNo = in.readInt();
        
    }

    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeInt(clientNo);
        
    }
    
}
