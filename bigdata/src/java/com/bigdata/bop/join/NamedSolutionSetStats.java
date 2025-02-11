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
 * Created on Jan 30, 2012
 */

package com.bigdata.bop.join;

import com.bigdata.bop.engine.BOpStats;
import com.bigdata.counters.CAT;

/**
 * Adds reporting for the size of the named solution set.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class NamedSolutionSetStats extends BOpStats {

    private static final long serialVersionUID = 1L;

    public final CAT solutionSetSize = new CAT();

    @Override
    public void add(final BOpStats o) {

        super.add(o);

        if (o instanceof NamedSolutionSetStats) {

            final NamedSolutionSetStats t = (NamedSolutionSetStats) o;

            solutionSetSize.add(t.solutionSetSize.get());

        }

    }

    @Override
    protected void toString(final StringBuilder sb) {
        
        super.toString(sb);

        sb.append(",solutionSetSize=" + solutionSetSize.get());
        
    }

}
