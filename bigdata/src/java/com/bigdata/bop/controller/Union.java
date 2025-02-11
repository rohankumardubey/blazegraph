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
 * Created on Aug 18, 2010
 */

package com.bigdata.bop.controller;

import java.util.Map;

import com.bigdata.bop.BOp;
import com.bigdata.bop.IBindingSet;
import com.bigdata.bop.NV;

/**
 * UNION()[maxParallel(default all); subqueries=ops]
 * 
 * <pre>
 * UNION([],{subqueries=[a,b,c]})
 * </pre>
 * 
 * Will run the subqueries <i>a</i>, <i>b</i>, and <i>c</i> in parallel for each
 * source {@link IBindingSet}. The output of those subqueries will be routed to
 * the UNION operator (their parent).
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class Union extends AbstractSubqueryOp {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Deep copy constructor.
     * 
     * @param op
     */
    public Union(final Union op) {
        super(op);
    }

    /**
     * Shallow copy constructor.
     * 
     * @param args
     * @param annotations
     */
    public Union(final BOp[] args, final Map<String, Object> annotations) {

        super(args, annotations);

//        if (args.length < 2)
//            throw new IllegalArgumentException();

    }

    public Union(final BOp[] args, NV... annotations) {

        this(args, NV.asMap(annotations));
        
    }

}
