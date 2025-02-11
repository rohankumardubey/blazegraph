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
 * Created on Nov 5, 2007
 */

package com.bigdata.rdf.spo;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;

import com.bigdata.rdf.model.StatementEnum;
import com.bigdata.relation.accesspath.IElementFilter;
import com.bigdata.relation.rule.eval.ISolution;

/**
 * Filter matches only {@link StatementEnum#Inferred} statements.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class InferredSPOFilter<E extends ISPO> extends SPOFilter<ISPO> {

    /**
     * 
     */
    private static final long serialVersionUID = -7026008442779748082L;
    
    /**
     * Shared instance.
     */
    static public final transient IElementFilter<ISPO> INSTANCE = new InferredSPOFilter<ISPO>();

    /**
     * De-serialization ctor.
     */
    private InferredSPOFilter() {

        super();
        
    }

    public boolean isValid(Object o) {
        
        if (!canAccept(o)) {
            
            return true;
            
        }
        
        return accept((ISPO) o);
        
    }

    private boolean accept(final ISPO o) {
        
        final ISPO spo = (ISPO) o;
        
        return spo.getStatementType() == StatementEnum.Inferred;

    }


    /**
     * Imposes the canonicalizing mapping during object de-serialization.
     */
    private Object readResolve() throws ObjectStreamException {
        
        return INSTANCE;
        
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {

        // NOP - stateless.
        
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {

        // NOP - stateless.

    }

}
