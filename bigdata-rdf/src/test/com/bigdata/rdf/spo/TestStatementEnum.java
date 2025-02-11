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
 * Created on Oct 22, 2007
 */

package com.bigdata.rdf.spo;

import junit.framework.TestCase;

import com.bigdata.io.ByteArrayBuffer;
import com.bigdata.rawstore.IRawStore;
import com.bigdata.rdf.model.StatementEnum;

/**
 * Test suite for {@link StatementEnum}.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class TestStatementEnum extends TestCase {

    /**
     * 
     */
    public TestStatementEnum() {
    }

    /**
     * @param arg0
     */
    public TestStatementEnum(String arg0) {
        super(arg0);
    }

    public void test_max() {
        
        assertEquals(StatementEnum.Explicit, StatementEnum.max(
                StatementEnum.Axiom, StatementEnum.Explicit));

        assertEquals(StatementEnum.Explicit, StatementEnum.max(
                StatementEnum.Inferred, StatementEnum.Explicit));

        assertEquals(StatementEnum.Axiom, StatementEnum.max(
                StatementEnum.Inferred, StatementEnum.Axiom));

        assertEquals(StatementEnum.Inferred, StatementEnum.max(
                StatementEnum.Inferred, StatementEnum.Inferred));
        
    }

}
