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
 * Created on Nov 16, 2006
 */

package com.bigdata.btree;

import com.bigdata.cache.HardReferenceQueueEvictionListener;
import com.bigdata.cache.IHardReferenceQueue;

/**
 * Interface to handle evictions of nodes or leaves from the hard reference
 * queue. The listener is responsible for decrementing the
 * {@link AbstractNode#referenceCount} and must write a dirty node or leaf onto
 * the store when their reference counter reaches zero(0).
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public interface IEvictionListener extends
        HardReferenceQueueEvictionListener<PO> {

    public void evicted(IHardReferenceQueue<PO> cache, PO ref);

}
