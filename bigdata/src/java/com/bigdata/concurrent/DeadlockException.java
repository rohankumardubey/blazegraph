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
package com.bigdata.concurrent;

/**
 * An instance of this exception is thrown when the lock requests of two or more
 * transactions form a deadlock. The exeception is thrown in the thread of each
 * transaction which is aborted to prevent deadlock.
 * 
 * @author thompsonbry
 */
public class DeadlockException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7698816681497973630L;

	private DeadlockException() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param message
     */
    public DeadlockException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public DeadlockException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public DeadlockException(Throwable cause) {
        super(cause);
    }

}
