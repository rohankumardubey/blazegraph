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
 * Created on Oct 19, 2007
 */

package com.bigdata.rdf.store;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestCase2;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

/**
 * Runs tests for each {@link ITripleStore} implementation.
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
     */
    public static Test suite()
    {

        final TestSuite suite = new TestSuite("RDF Stores");

        /*
         * Run each of the kinds of triple stores through both their specific
         * and shared unit tests.
         */

        suite.addTest( com.bigdata.rdf.store.TestTempTripleStore.suite() );
        
        suite.addTest( com.bigdata.rdf.store.TestLocalTripleStore.suite() );

//        suite.addTest( com.bigdata.rdf.store.TestLocalTripleStoreWORM.suite() );

        suite.addTest( com.bigdata.rdf.store.TestLocalTripleStoreWithoutInlining.suite() );

        suite.addTest( com.bigdata.rdf.store.TestLocalTripleStoreWithoutStatementIdentifiers.suite() );

//        suite.addTest( com.bigdata.rdf.store.TestLocalTripleStoreWithIsolatableIndices.suite() );

        // @todo test quad store for LDS and EDS.
//        suite.addTest( com.bigdata.rdf.store.TestLocalQuadStore.suite() );

//        suite.addTest( TestScaleOutTripleStoreWithLocalDataServiceFederation.suite() );

/*
 * @todo We should run this test suite against a CI cluster on a single machine using
 * the full bigdata federation rather than EDS.
 */
//        suite.addTest(com.bigdata.rdf.store.TestScaleOutTripleStoreWithEmbeddedFederation
//                        .suite());

//        if (Boolean.parseBoolean(System.getProperty("maven.test.services.skip",
//                "false"))
//                || !JiniServicesHelper.isJiniRunning()) {
//
//            /*
//             * Test scale-out RDF database.
//             * 
//             * Note: This test suite sets up a local bigdata federation for each
//             * test. See the test suite for more information about required Java
//             * properties.
//             */
//
//            suite.addTest(com.bigdata.rdf.store.TestScaleOutTripleStoreWithJiniFederation
//                    .suite());
//
//        }

        return suite;
        
    }
    
}
