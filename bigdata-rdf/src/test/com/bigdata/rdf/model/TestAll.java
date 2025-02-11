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
package com.bigdata.rdf.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Aggregates test suites into increasing dependency order.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class TestAll extends TestCase {

    /**
     * 
     */
    public TestAll() {
    }

    /**
     * @param arg0
     */
    public TestAll(String arg0) {
        super(arg0);
    }

	/**
	 * Returns a test that will run each of the implementation specific test
	 * suites in turn.
	 * 
	 * @todo The {@link BigdataValue} and {@link BigdataStatement}
	 *       implementation and those implementations should be tested for
	 *       Sesame 2 API compatibility.
	 */
    public static Test suite()
    {

        final TestSuite suite = new TestSuite("RDF data model");

        // value factory test suite.
        suite.addTestSuite(TestFactory.class);

        // test suite for Value.equals()
        suite.addTestSuite(TestEquals.class);

        // test suite for serialization semantics.
        suite.addTestSuite(TestBigdataValueSerialization.class);

        return suite;
        
    }
    
}
