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
 * Created on May 19, 2007
 */

package com.bigdata.rdf.store;

import java.util.Properties;

import com.bigdata.journal.ITx;
import com.bigdata.service.jini.JiniClient;

/**
 * Abstract test case that sets up and connects to a bigdata federation and
 * establishes an RDF database on that federation.
 * <p>
 * Note: The configuration options for the (meta)data services are set in their
 * respective <code>properties</code> files NOT by the System properties!
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
abstract public class AbstractDistributedTripleStoreTestCase extends AbstractDistributedBigdataFederationTestCase {

    /**
     * 
     */
    public AbstractDistributedTripleStoreTestCase() {
    }

    /**
     * @param arg0
     */
    public AbstractDistributedTripleStoreTestCase(String arg0) {
        super(arg0);
    }

    /**
     * The triple store under test.
     */
    ScaleOutTripleStore store;
    
    public Properties getProperties() {
        
        return new Properties(System.getProperties());
    
    }
    
    public void setUp() throws Exception {

        super.setUp();

        // connect to the database.
        store = new ScaleOutTripleStore(client.getFederation(), "test_",
                ITx.UNISOLATED, client.getProperties());
        
        store.create();
        
    }

    public void tearDown() throws Exception {
        
        super.tearDown();
        
    }
    
}
