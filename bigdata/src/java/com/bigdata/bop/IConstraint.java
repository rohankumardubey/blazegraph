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
 * Created on Jun 19, 2008
 */

package com.bigdata.bop;

import java.io.Serializable;

/**
 * An interface for specifying constraints on the allowable states of an
 * {@link IBindingSet}. For example, you can impose the constraint that two
 * variables must have distinct bindings the constraint that a binding must be
 * (or must not be) a particular value type, or that the binding must take on
 * one of a set of enumerated values.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public interface IConstraint extends BOp, Serializable {

    /**
     * Return <code>true</code> if the binding set satisfies the constraint.
     * 
     * @param bindingSet
     *            The binding set.
     * 
     * @todo rename as eval(IBindingSet)?
     */
    public boolean accept(IBindingSet bindingSet);

    /**
     * A zero length empty {@link IConstraint} array.
     */
    public IConstraint[] EMPTY = new IConstraint[0];
    
}
