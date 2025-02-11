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
 * Created on Mar 10, 2012
 */

package com.bigdata.rdf.sparql.ast;

import java.util.Map;

import com.bigdata.bop.BOp;

/**
 * The CLEAR operation removes all the triples in the specified graph(s) in the
 * Graph Store.
 * 
 * <pre>
 * CLEAR ( SILENT )? (GRAPH IRIref | DEFAULT | NAMED | ALL | GRAPHS | SOLUTIONS | SOLUTIONS %VARNAME)
 * </pre>
 * 
 * Note: Bigdata does not support empty graphs, so DROP and CLEAR have identical
 * semantics.
 * 
 * @see http://www.w3.org/TR/sparql11-update/#clear
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class ClearGraph extends DropGraph {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ClearGraph() {
        
        super(UpdateType.Clear);
        
    }

    /**
     * @param op
     */
    public ClearGraph(final ClearGraph op) {
        
        super(op);
        
    }

    /**
     * @param args
     * @param anns
     */
    public ClearGraph(final BOp[] args, final Map<String, Object> anns) {
        
        super(args, anns);
        
    }

}
