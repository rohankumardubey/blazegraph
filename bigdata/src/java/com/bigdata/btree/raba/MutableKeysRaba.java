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
 * Created on Mar 6, 2008
 */

package com.bigdata.btree.raba;

/**
 * Flyweight implementation for wrapping a <code>byte[][]</code> with fromIndex
 * and toIndex.
 * 
 * @see MutableKeyBuffer
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class MutableKeysRaba extends AbstractRaba implements IRaba {

    /**
     * No - this view is mutable.
     */
    @Override
    public boolean isReadOnly() {

        return false;

    }

    /**
     * Yes.
     */
    @Override
    final public boolean isKeys() {
    
        return true;
        
    }

    /**
     * Create a view of a byte[][]. All elements in the array are visible in the
     * view.
     * 
     * @param a
     *            The backing byte[][].
     */
    public MutableKeysRaba(final byte[][] a) {

        this(0/* fromIndex */, a.length/* toIndex */, a.length/* capacity */, a);

    }

    /**
     * Create a view of a <code>byte[][]</code> slice. The slice will include
     * only those elements between the fromIndex and the toIndex. The capacity
     * will be the #of elements. {@link #isFull()} will report <code>true</code>
     * .
     * 
     * @param fromIndex
     *            The index of the first visible in the view (inclusive lower
     *            bound).
     * @param toIndex
     *            The index of the first element beyond the view (exclusive
     *            upper bound). If toIndex == fromIndex then the view is empty.
     * @param a
     *            The backing byte[][].
     */
    public MutableKeysRaba(final int fromIndex, final int toIndex,
            final byte[][] a) {

        this(fromIndex, toIndex, a.length - fromIndex, a);

    }

    /**
     * Create a view from a slice of a byte[][].
     * 
     * @param fromIndex
     *            The index of the first element in the byte[][] which is
     *            visible in the view (inclusive lower bound).
     * @param toIndex
     *            The index of the first element in the byte[][] beyond the view
     *            (exclusive upper bound).
     * @param capacity
     *            The #of elements which may be used in the view.
     * @param a
     *            The backing byte[][].
     */
    public MutableKeysRaba(final int fromIndex, final int toIndex,
            final int capacity, final byte[][] a) {

        super(fromIndex, toIndex, capacity, a);

    }

    @Override
    public MutableKeysRaba resize(final int n) {

        return (MutableKeysRaba) super.resize(n);

    }

}
