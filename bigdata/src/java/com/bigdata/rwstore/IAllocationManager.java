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
package com.bigdata.rwstore;

import com.bigdata.rawstore.IAllocationContext;

public interface IAllocationManager {

    /**
     * Indicates that the allocation context will no longer be used and that the
     * allocations made within the context should be discarded. The allocations
     * associated with the context are discarded, as are any deletes made within
     * the scope of that allocation context. The allocators associated with the
     * allocation context are return to the global list of available allocators.
     * 
     * @param context
     *            The application object which serves as the allocation context.
     */
    public void abortContext(IAllocationContext context);

    /**
     * Indicates that the allocation context will no longer be used, but that
     * the allocations made within the context should be preserved. The
     * allocations associated with the context are propagated to the parent
     * allocation context. The {@link IStore} is the top-level parent of
     * allocation contexts. The allocators associated with the allocation
     * context are return to the global list of available allocators.
     * 
     * @param context
     *            The application object which serves as the allocation context.
     */
    public void detachContext(IAllocationContext context);

    /**
     * Notify the {@link IStore} that an object will serve as an allocation
     * context and ensures that session protection will be active while the
     * context is registered. Contexts are implicitly registered on first use.
     * 
     * @param context
     *            The application object which serves as the allocation context.
     */
    public void registerContext(IAllocationContext context);

}
