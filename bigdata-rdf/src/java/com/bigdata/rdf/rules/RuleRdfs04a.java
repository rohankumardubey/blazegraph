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
 * Created on Oct 29, 2007
 */

package com.bigdata.rdf.rules;

import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.bigdata.rdf.spo.SPOPredicate;
import com.bigdata.rdf.vocab.Vocabulary;

/**
 * rdfs4a:
 * 
 * <pre>
 * (?u ?a ?x) -&gt; (?u rdf:type rdfs:Resource)
 * </pre>
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 */
public class RuleRdfs04a extends AbstractRuleDistinctTermScan {

    /**
     * 
     */
    private static final long serialVersionUID = 6070683089054966426L;

    public RuleRdfs04a(String relationName,Vocabulary vocab) {

            super(  "rdfs04a",//
                    new SPOPredicate(relationName,var("u"), vocab.getConstant(RDF.TYPE), vocab.getConstant(RDFS.RESOURCE)), //
                    new SPOPredicate[] { //
                        new SPOPredicate(relationName,var("u"), var("a"), var("x"))//
                    },//
                    null // constraints
                    );

        }

}
