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
 * Created on Jul 9, 2008
 */

package com.bigdata.relation.locator;

import com.bigdata.btree.IIndex;

/**
 * A locatable resource. Resources have a unique namespace and can be resolved
 * and a view materialized using an {@link IResourceLocator}. There is a
 * timestamp associated with the resource - the timestamp is used to request
 * {@link IIndex} views for the resource. There is a presumption that resources
 * are essentially logical "index containers".
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 * 
 * @param T
 *            The generic [T]ype of the locatable resource.
 */
public interface ILocatableResource<T> {

	/**
	 * Deferred initialization method is automatically invoked when the resource
	 * is materialized by the {@link IResourceLocator}. The implementation is
	 * encouraged to strengthen the return type.
	 */
	public ILocatableResource<T> init();
	
    /**
     * The identifying namespace.
     */
    public String getNamespace();
    
    /**
     * The timestamp associated with the view of the resource.
     */
    public long getTimestamp();

    /**
     * The identifier for the containing resource.
     * 
     * @return The identifier of the containing resource -or- <code>null</code>
     *         if there is no containing resource.
     */
    public String getContainerNamespace();
    
}
