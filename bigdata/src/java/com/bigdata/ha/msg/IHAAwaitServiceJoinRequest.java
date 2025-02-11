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
package com.bigdata.ha.msg;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Message requesting that the leader await the visibility of a join by the
 * specified service.
 */
public interface IHAAwaitServiceJoinRequest extends IHAMessage {

    /**
     * The {@link UUID} of the service whose service join will be awaited.
     */
    UUID getServiceUUID();

    /**
     * How long to wait for the service join to become visible.
     */
    long getTimeout();

    /**
     * The unit for the timeout.
     */
    TimeUnit getUnit();
    
}
