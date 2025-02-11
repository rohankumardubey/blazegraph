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
 * Created on Jun 3, 2010
 */

package com.bigdata.rdf.lexicon;

import java.util.Iterator;
import java.util.Locale;

import org.openrdf.model.Value;

import com.bigdata.rdf.model.BigdataValue;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.search.FullTextIndex;
import com.bigdata.search.IHit;

/**
 * Abstraction for the text indexer for RDF {@link Value}s allowing either the
 * built-in bigdata {@link FullTextIndex} or support for Lucene, etc.
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 * @version $Id$
 * 
 * @see AbstractTripleStore.Options#TEXT_INDEXER_CLASS
 */
public interface IValueCentricTextIndexer<A extends IHit> extends ITextIndexer<A> {
   
    /**
     * <p>
     * Add the terms to the full text index so that we can do fast lookup of the
     * corresponding term identifiers. Only literals are tokenized. Literals
     * that have a language code property are parsed using a tokenizer
     * appropriate for the specified language family. Other literals and URIs
     * are tokenized using the default {@link Locale}.
     * </p>
     * 
     * @param capacity
     *            A hint to the underlying layer about the buffer size before an
     *            incremental flush of the index.
     * @param itr
     *            Iterator visiting the terms to be indexed.
     * 
     * @todo allow registeration of datatype specific tokenizers (we already
     *       have language family based lookup).
     */
    public void index(int capacity, Iterator<BigdataValue> valuesIterator);

}
