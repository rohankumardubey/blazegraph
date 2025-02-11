/**
Copyright (C) SYSTAP, LLC 2011.  All rights reserved.

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

package com.bigdata.rdf.sail;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.repository.sail.SailTupleQuery;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.Sail;
import org.openrdf.sail.memory.MemoryStore;

import com.bigdata.journal.BufferMode;
import com.bigdata.rdf.axioms.NoAxioms;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.BigdataSailRepository;
import com.bigdata.rdf.vocab.NoVocabulary;

/**
 * Unit test template for use in submission of bugs.
 * <p>
 * This test case will delegate to an underlying backing store.  You can
 * specify this store via a JVM property as follows:
 * <code>-DtestClass=com.bigdata.rdf.sail.TestBigdataSailWithQuads</code>
 * <p>
 * There are three possible configurations for the testClass:
 * <ul>
 * <li>com.bigdata.rdf.sail.TestBigdataSailWithQuads (quads mode)</li>
 * <li>com.bigdata.rdf.sail.TestBigdataSailWithoutSids (triples mode)</li>
 * <li>com.bigdata.rdf.sail.TestBigdataSailWithSids (SIDs mode)</li>
 * </ul>
 * <p>
 * The default for triples and SIDs mode is for inference with truth maintenance
 * to be on.  If you would like to turn off inference, make sure to do so in
 * {@link #getProperties()}.
 *  
 * @author <a href="mailto:mrpersonick@users.sourceforge.net">Mike Personick</a>
 * @version $Id$
 */
public class TestMillisecondPrecisionForInlineDateTimes extends QuadsTestCase {

    private static final Logger log = Logger
            .getLogger(TestMillisecondPrecisionForInlineDateTimes.class);

    /**
     * Please set your database properties here, except for your journal file,
     * please DO NOT SPECIFY A JOURNAL FILE. 
     */
    @Override
    public Properties getProperties() {
        
        final Properties props = super.getProperties();

        /*
         * For example, here is a set of five properties that turns off
         * inference, truth maintenance, and the free text index.
         */
        props.setProperty(BigdataSail.Options.AXIOMS_CLASS, NoAxioms.class.getName());
        props.setProperty(BigdataSail.Options.VOCABULARY_CLASS, NoVocabulary.class.getName());
        props.setProperty(BigdataSail.Options.TRUTH_MAINTENANCE, "false");
        props.setProperty(BigdataSail.Options.JUSTIFY, "false");
        props.setProperty(BigdataSail.Options.TEXT_INDEX, "false");
		props.setProperty(BigdataSail.Options.INLINE_DATE_TIMES, "true");
		props.setProperty(BigdataSail.Options.INLINE_DATE_TIMES_TIMEZONE, "GMT");

		// No disk file.
        props.setProperty(com.bigdata.journal.Options.BUFFER_MODE,
                BufferMode.Transient.toString());
		
        return props;
        
    }

    public TestMillisecondPrecisionForInlineDateTimes() {
    }

    public TestMillisecondPrecisionForInlineDateTimes(String arg0) {
        super(arg0);
    }
    
    public void testBug() throws Exception {
    	
    	/*
    	 * We use an in-memory Sesame store as our point of reference.  This
    	 * will supply the "correct" answer to the query (below).
    	 */
        Sail sesameSail = null;
        
        /*
         * The bigdata store, backed by a temporary journal file.
         */
	  	BigdataSail bigdataSail = null;
	  	
	  	try {
	  	
	  	    sesameSail = new MemoryStore();
	  	    
	  	    bigdataSail = getSail();
	  	    
	        /*
	         * Data file containing the data demonstrating your bug.
	         */
	        final String data = "TestMillisecondPrecisionForInlineDateTimes.ttl";
	        final String baseURI = "";
	        final RDFFormat format = RDFFormat.TURTLE;
	        
	        /*
	         * Query(ies) demonstrating your bug.
	         */
	        final String query =
//	          "prefix bd: <"+BD.NAMESPACE+"> " +
	            "prefix rdf: <"+RDF.NAMESPACE+"> " +
	            "prefix rdfs: <"+RDFS.NAMESPACE+"> " +

	                "SELECT DISTINCT ?ar WHERE {\n"
	                + "  FILTER(?datePub>=\"2011-03-08T08:48:27.003\"^^<http://www.w3.org/2001/XMLSchema#dateTime>).\n"
	                + "  ?ar <os:prop/analysis/datePublished> ?datePub\n"
	                + "} ORDER BY DESC(?datePub)";
	        
	  		sesameSail.initialize();
	  		bigdataSail.initialize();
	  		
  			final Repository sesameRepo = new SailRepository(sesameSail);
  			final BigdataSailRepository bigdataRepo = new BigdataSailRepository(bigdataSail);
  			
	  		{ // load the data into the Sesame store
	  			
	  			final RepositoryConnection cxn = sesameRepo.getConnection();
	  			try {
	  				cxn.setAutoCommit(false);
	  				cxn.add(getClass().getResourceAsStream(data), baseURI, format);
	  				cxn.commit();
	  			} finally {
	  				cxn.close();
	  			}
	  			
	  		}
	  		
	  		{ // load the data into the bigdata store
	  			
	  			final RepositoryConnection cxn = bigdataRepo.getConnection();
	  			try {
	  				cxn.setAutoCommit(false);
	  				cxn.add(getClass().getResourceAsStream(data), baseURI, format);
	  				cxn.commit();
	  			} finally {
	  				cxn.close();
	  			}
	  			
	  		}
	  		
            final Collection<BindingSet> answer = new LinkedList<BindingSet>();
            
            /*
             * Here is how you manually build the answer set, but please make
             * sure you answer truly is correct if you choose to do it this way.

//            answer.add(createBindingSet(
//            		new BindingImpl("neType", vf.createURI("http://example/class/Location"))
//            		));
//            answer.add(createBindingSet(
//            		new BindingImpl("neType", vf.createURI("http://example/class/Person"))
//            		));

             */

	  		/*
	  		 * Run the problem query using the Sesame store to gather the
	  		 * correct results.
	  		 */
            { 
	  			final RepositoryConnection cxn = sesameRepo.getConnection();
	  			try {
		            final SailTupleQuery tupleQuery = (SailTupleQuery)
		                cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
		            tupleQuery.setIncludeInferred(false /* includeInferred */);
	            	final TupleQueryResult result = tupleQuery.evaluate();
	            	
	            	if (log.isInfoEnabled()) {
	            		log.info("sesame results:");
	            		if (!result.hasNext()) {
	            			log.info("no results.");
	            		}
	            	}
	            	
	                while (result.hasNext()) {
	                	final BindingSet bs = result.next();
	                	answer.add(bs);
		            	if (log.isInfoEnabled())
		            		log.info(bs);
		            }
	  			} finally {
	  				cxn.close();
	  			}
            }
                
            /*
             * Run the problem query using the bigdata store and then compare
             * the answer.
             */
            final RepositoryConnection cxn = bigdataRepo.getReadOnlyConnection();
  			try {
	            final SailTupleQuery tupleQuery = (SailTupleQuery)
	                cxn.prepareTupleQuery(QueryLanguage.SPARQL, query);
	            tupleQuery.setIncludeInferred(false /* includeInferred */);
            	
	            if (log.isInfoEnabled()) {
		            final TupleQueryResult result = tupleQuery.evaluate();
            		log.info("bigdata results:");
            		if (!result.hasNext()) {
            			log.info("no results.");
            		}
	                while (result.hasNext()) {
	            		log.info(result.next());
		            }
	            }
	            
	            final TupleQueryResult result = tupleQuery.evaluate();
            	compare(result, answer);
            	
  			} finally {
  				cxn.close();
  			}
          
        } finally {

            if (sesameSail != null)
                sesameSail.shutDown();
            
            if (bigdataSail != null)
                bigdataSail.__tearDownUnitTest();
            
        }

    }

}
