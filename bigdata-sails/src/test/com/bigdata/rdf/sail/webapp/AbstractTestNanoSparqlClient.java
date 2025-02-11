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
package com.bigdata.rdf.sail.webapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.server.Server;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
import org.openrdf.rio.helpers.StatementCollector;

import com.bigdata.BigdataStatics;
import com.bigdata.journal.IIndexManager;
import com.bigdata.journal.ITx;
import com.bigdata.rdf.sail.BigdataSail;
import com.bigdata.rdf.sail.CreateKBTask;
import com.bigdata.rdf.sail.DestroyKBTask;
import com.bigdata.rdf.sail.webapp.client.HttpClientConfigurator;
import com.bigdata.rdf.sail.webapp.client.IPreparedGraphQuery;
import com.bigdata.rdf.sail.webapp.client.IPreparedTupleQuery;
import com.bigdata.rdf.sail.webapp.client.RemoteRepository.AddOp;
import com.bigdata.rdf.sail.webapp.client.RemoteRepository.RemoveOp;
import com.bigdata.rdf.sail.webapp.client.RemoteRepositoryManager;
import com.bigdata.rdf.store.AbstractTripleStore;
import com.bigdata.rdf.store.BD;
import com.bigdata.rdf.task.AbstractApiTask;
import com.bigdata.util.InnerCause;
import com.bigdata.util.config.NicUtil;

/**
 * Proxied test suite.
 *
 * @param <S>
 */
public abstract class AbstractTestNanoSparqlClient<S extends IIndexManager> extends ProxyTestCase<S> {

    /**
     * The path used to resolve resources in this package when they are being
     * uploaded to the {@link NanoSparqlServer}.
     */
    protected static final String packagePath = "bigdata-sails/src/test/com/bigdata/rdf/sail/webapp/";

	/**
	 * A jetty {@link Server} running a {@link NanoSparqlServer} instance.
	 */
	protected Server m_fixture;

	/**
	 * The namespace of the {@link AbstractTripleStore} instance against which
	 * the test is running. A unique namespace is used for each test run, but
	 * the namespace is based on the test name.
	 */
	protected String namespace;
	
    /**
     * The {@link ClientConnectionManager} for the {@link HttpClient} used by
     * the {@link RemoteRepository}. This is used when we tear down the
     * {@link RemoteRepository}.
     */
	// private ClientConnectionManager m_cm;
	
    /**
     * The http client.
     */
    protected HttpClient m_client;

    /**
     * The client-API wrapper to the NSS.
     */
    protected RemoteRepositoryManager m_repo;

    /**
     * The effective {@link NanoSparqlServer} http end point (including the
     * ContextPath).
     */
	protected String m_serviceURL;

    /**
     * The URL of the root of the web application server. This does NOT include
     * the ContextPath for the webapp.
     * 
     * <pre>
     * http://localhost:8080 -- root URL
     * http://localhost:8080/bigdata -- webapp URL (includes "/bigdata" context path.
     * </pre>
     */
	protected String m_rootURL;
	
//	/**
//	 * The request path for the REST API under test.
//	 */
//	final protected static String requestPath = "/sparql";

	public AbstractTestNanoSparqlClient() {
		
	}

	public AbstractTestNanoSparqlClient(final String name) {

		super(name);

	}

   private AbstractTripleStore createTripleStore(
         final IIndexManager indexManager, final String namespace,
         final Properties properties) throws InterruptedException,
         ExecutionException {

		if(log.isInfoEnabled())
			log.info("KB namespace=" + namespace);

      AbstractApiTask.submitApiTask(indexManager, new CreateKBTask(namespace,
            properties)).get();
		
        if(log.isInfoEnabled())
        	log.info("Created tripleStore: " + namespace);

        /**
         * Return a view of the new KB to the caller.
         * 
         * Note: The caller MUST NOT attempt to modify this KB view outside of
         * the group commit mechanisms. Therefore I am now returning a read-only
         * view.
         */
      final AbstractTripleStore tripleStore = (AbstractTripleStore) indexManager
            .getResourceLocator().locate(namespace, ITx.READ_COMMITTED);

      assert tripleStore != null;

      return tripleStore;
      
    }

	private void dropTripleStore(final IIndexManager indexManager,
			final String namespace) throws InterruptedException, ExecutionException {

		if(log.isInfoEnabled())
			log.info("KB namespace=" + namespace);

      try {
         AbstractApiTask.submitApiTask(indexManager,
               new DestroyKBTask(namespace)).get();
      } catch (Exception ex) {
         if (InnerCause.isInnerCause(ex, DatasetNotFoundException.class)) {
            if (log.isInfoEnabled())
               log.info("namespace does not exist: " + namespace);
         }
		}

	}
	
	/**
	 * The {@link TestMode} that is in effect.
	 */
	private TestMode testMode = null;
	
	/**
	 * The {@link TestMode} that is in effect.
	 */
	protected TestMode getTestMode() {
		return testMode;
	}

	protected Server newFixture(final String lnamespace) throws Exception {

	   final IIndexManager indexManager = getIndexManager();
		
		final Properties properties = getProperties();

		// Create the triple store instance.
        final AbstractTripleStore tripleStore = createTripleStore(indexManager,
        		lnamespace, properties);
        
        if (tripleStore.isStatementIdentifiers()) {
			testMode = TestMode.sids;
        } else if (tripleStore.isQuads()) {
            testMode = TestMode.quads;
        } else {
            testMode = TestMode.triples;
        }
		
        final Map<String, String> initParams = new LinkedHashMap<String, String>();
        {

            initParams.put(ConfigParams.NAMESPACE, lnamespace);

            initParams.put(ConfigParams.CREATE, "false");
            
        }
        // Start server for that kb instance.
        final Server fixture = NanoSparqlServer.newInstance(0/* port */,
                indexManager, initParams);

        fixture.start();
		
        return fixture;
	}
	
	@Override
	public void setUp() throws Exception {
	    
		super.setUp();

		if (log.isTraceEnabled())
			log.trace("Setting up test:" + getName());
		
//		final Properties properties = getProperties();

		// guaranteed distinct namespace for the KB instance.
		namespace = getName() + UUID.randomUUID();
		
		m_fixture = newFixture(namespace);

//		final IIndexManager m_indexManager = getIndexManager();
//		
//		// Create the triple store instance.
//        final AbstractTripleStore tripleStore = createTripleStore(m_indexManager,
//                namespace, properties);
//        
//        if (tripleStore.isStatementIdentifiers()) {
//            testMode = TestMode.sids;
//        } else if (tripleStore.isQuads()) {
//            testMode = TestMode.quads;
//        } else {
//            testMode = TestMode.triples;
//        }
//		
//        final Map<String, String> initParams = new LinkedHashMap<String, String>();
//        {
//
//            initParams.put(ConfigParams.NAMESPACE, namespace);
//
//            initParams.put(ConfigParams.CREATE, "false");
//            
//        }
//        // Start server for that kb instance.
//        m_fixture = NanoSparqlServer.newInstance(0/* port */,
//                m_indexManager, initParams);
//
//        m_fixture.start();

		final int port = NanoSparqlServer.getLocalPort(m_fixture);

		// log.info("Getting host address");

        final String hostAddr = NicUtil.getIpAddress("default.nic", "default",
                true/* loopbackOk */);

        if (hostAddr == null) {

            fail("Could not identify network address for this host.");

        }

        m_rootURL = new URL("http", hostAddr, port, ""/* contextPath */
        ).toExternalForm();

        m_serviceURL = new URL("http", hostAddr, port,
                BigdataStatics.getContextPath()).toExternalForm();

        if (log.isInfoEnabled())
            log.info("Setup done: \nname=" + getName() + "\nnamespace="
                    + namespace + "\nrootURL=" + m_rootURL + "\nserviceURL="
                    + m_serviceURL);

        /*
         * Ensure that the client follows redirects using a standard policy.
         * 
         * Note: This is necessary for tests of the webapp structure since the
         * container may respond with a redirect (302) to the location of the
         * webapp when the client requests the root URL.
         */

       	m_client = HttpClientConfigurator.getInstance().newInstance();
        
        m_repo = new RemoteRepositoryManager(m_serviceURL, m_client,
                getIndexManager().getExecutorService());

		if (log.isInfoEnabled())
			log.info("Setup Active Threads: " + Thread.activeCount());
	
	}

    @Override
	public void tearDown() throws Exception {

		if (log.isTraceEnabled())
			log.trace("tearing down test: " + getName());

		if (m_fixture != null) {

			m_fixture.stop();

			m_fixture = null;

		}

		final IIndexManager m_indexManager = getIndexManager();
		
		if (m_indexManager != null && namespace != null) {

			dropTripleStore(m_indexManager, namespace);

		}
		
//		m_indexManager = null;

		namespace = null;
        
        m_rootURL = null;
		m_serviceURL = null;
		
//        if (m_cm != null) {
//            m_cm.shutdown();
//            m_cm = null;
//        }
		
		log.info("Connection Shutdown Check");
		
        m_repo.close();
        m_client.stop();
        
        m_repo = null;
        m_client = null;
        
        log.info("tear down done");

        super.tearDown();
        
        final int nthreads = Thread.activeCount();

		if (log.isInfoEnabled())
			log.info("Teardown Active Threads: " + nthreads);
        
        if (nthreads > 300) {
        	log.error("High residual thread count: " + nthreads);
        }
	}

    /**
    * Returns a view of the triple store using the sail interface.
    * 
    * FIXME DO NOT CIRCUMVENT! Use the REST API throughout this test suite.
    */
    @Deprecated
    protected BigdataSail getSail() {

		final AbstractTripleStore tripleStore = (AbstractTripleStore) getIndexManager()
				.getResourceLocator().locate(namespace, ITx.UNISOLATED);

        return new BigdataSail(tripleStore);

    }

//	protected String getStreamContents(final InputStream inputStream)
//            throws IOException {
//
//        final Reader rdr = new InputStreamReader(inputStream);
//		
//	    final StringBuffer sb = new StringBuffer();
//		
//	    final char[] buf = new char[512];
//	    
//		while (true) {
//		
//		    final int rdlen = rdr.read(buf);
//			
//		    if (rdlen == -1)
//				break;
//			
//		    sb.append(buf, 0, rdlen);
//		    
//		}
//		
//		return sb.toString();
//
//	}

	/**
	 * Counts the #of results in a SPARQL result set.
	 * 
	 * @param result
	 *            The connection from which to read the results.
	 * 
	 * @return The #of results.
	 * 
	 * @throws Exception
	 *             If anything goes wrong.
	 */
	protected long countResults(final TupleQueryResult result) throws Exception {

    	long count = 0;
    	
    	while(result.hasNext()) {
    		
    		result.next();
    		
    		count++;
    		
    	}
    	
    	result.close();
    	
    	return count;
    	
	}

	/**
	 * Counts the #of results in a SPARQL result set.
	 * 
	 * @param result
	 *            The connection from which to read the results.
	 * 
	 * @return The #of results.
	 * 
	 * @throws Exception
	 *             If anything goes wrong.
	 */
	protected long countResults(final GraphQueryResult result) throws Exception {

    	long count = 0;
    	
    	while(result.hasNext()) {
    		
    		result.next();
    		
    		count++;
    		
    	}
    	
    	result.close();
    	
    	return count;
    	
	}

    /**
     * Generates some statements and serializes them using the specified
     * {@link RDFFormat}.
     * 
     * @param ntriples
     *            The #of statements to generate.
     * @param format
     *            The format.
     * 
     * @return the serialized statements.
     */
    protected byte[] genNTRIPLES(final int ntriples, final RDFFormat format)
            throws RDFHandlerException {

        final Graph g = genNTRIPLES2(ntriples);
        
        final RDFWriterFactory writerFactory = RDFWriterRegistry.getInstance()
                .get(format);

        if (writerFactory == null)
            fail("RDFWriterFactory not found: format=" + format);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final RDFWriter writer = writerFactory.getWriter(baos);

        writer.startRDF();

        for (Statement stmt : g) {

            writer.handleStatement(stmt);

        }

        writer.endRDF();

        return baos.toByteArray();
        
    }
    
    protected Graph genNTRIPLES2(final int ntriples)
			throws RDFHandlerException {

		final Graph g = new GraphImpl();

		final ValueFactory f = new ValueFactoryImpl();

		final URI s = f.createURI("http://www.bigdata.org/b");

		final URI rdfType = f
				.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

		for (int i = 0; i < ntriples; i++) {

			final URI o = f.createURI("http://www.bigdata.org/c#" + i);

			g.add(s, rdfType, o);

		}
		
		return g;

	}

//    /**
//     * "ASK" query using POST with an empty KB.
//     */
//    public void test_POST_ASK() throws Exception {
//        
//        final String queryStr = "ASK where {?s ?p ?o}";
//
//        final QueryOptions opts = new QueryOptions();
//        opts.serviceURL = m_serviceURL;
//        opts.queryStr = queryStr;
//        opts.method = "POST";
//
//        opts.acceptHeader = BooleanQueryResultFormat.SPARQL.getDefaultMIMEType();
//        assertEquals(false, askResults(doSparqlQuery(opts, requestPath)));
//
//        opts.acceptHeader = BooleanQueryResultFormat.TEXT.getDefaultMIMEType();
//        assertEquals(false, askResults(doSparqlQuery(opts, requestPath)));
//        
//    }

    /**
     * Generate and return a very large literal.
     */
    protected Literal getVeryLargeLiteral() {

        final int len = 1024000;

        final StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++) {

            sb.append(Character.toChars('A' + (i % 26)));

        }
        
        return new LiteralImpl(sb.toString());
        
    }

    protected long countAll() throws Exception {
    	
    	return getSail().getDatabase().getExplicitStatementCount(null);
    	
//    	final RemoteRepository repo = new RemoteRepository(m_serviceURL);
//    	
//    	final String countQuery = "select * where {?s ?p ?o}";
//    	
//		final TupleQuery query = repo.prepareTupleQuery(countQuery);
//		
//		return countResults(query.evaluate());
    	
    }
    
    /**
     * Test helps PUTs some data, verifies that it is visible, DELETEs the data,
     * and then verifies that it is gone.
     * 
     * @param format
     *            The interchange format.
     */
    protected void doDeleteWithPostTest(final RDFFormat format) throws Exception {

//        final String queryStr = "select * where {?s ?p ?o}";

//        final QueryOptions opts = new QueryOptions();
//        opts.serviceURL = m_serviceURL;
//        opts.queryStr = queryStr;
//        opts.method = "POST";

        doInsertWithBodyTest("POST", 23, /*requestPath,*/ format);

//        assertEquals(23, countResults(doSparqlQuery(opts, requestPath)));
        assertEquals(23, countAll());

        doDeleteWithBody(/*requestPath,*/ 23, format);

        // No solutions (assuming a told triple kb or quads kb w/o axioms).
//        assertEquals(0, countResults(doSparqlQuery(opts, requestPath)));
        assertEquals(0, countAll());
        
    }

	protected long doDeleteWithQuery(/*final String servlet, */final String query) throws Exception {
		
//		HttpURLConnection conn = null;
//		try {
//
//			final URL url = new URL(m_serviceURL + servlet + "?query="
//					+ URLEncoder.encode(query, "UTF-8"));
//			conn = (HttpURLConnection) url.openConnection();
//			conn.setRequestMethod("DELETE");
//			conn.setDoOutput(true);
//			conn.setDoInput(true);
//			conn.setUseCaches(false);
//			conn.setReadTimeout(0);
//
//			conn.connect();
//
//			if (log.isInfoEnabled())
//				log.info(conn.getResponseMessage());
//
//			final int rc = conn.getResponseCode();
//			
//			if (rc < 200 || rc >= 300) {
//				throw new IOException(conn.getResponseMessage());
//			}
//
//		} catch (Throwable t) {
//			// clean up the connection resources
//			if (conn != null)
//				conn.disconnect();
//			throw new RuntimeException(t);
//		}
		
//		final RemoteRepository repo = new RemoteRepository(m_serviceURL);
		
		final RemoveOp remove = new RemoveOp(query);
		
		return m_repo.remove(remove);
		
    }

    protected long doDeleteWithAccessPath(//
//            final String servlet,//
            final URI s,//
            final URI p,//
            final Value o,//
            final URI... c//
            ) throws Exception {
    	
//        HttpURLConnection conn = null;
//        try {
//
//            final LinkedHashMap<String, String[]> requestParams = new LinkedHashMap<String, String[]>();
//
//            if (s != null)
//                requestParams.put("s",
//                        new String[] { EncodeDecodeValue.encodeValue(s) });
//            
//            if (p != null)
//                requestParams.put("p",
//                        new String[] { EncodeDecodeValue.encodeValue(p) });
//            
//            if (o != null)
//                requestParams.put("o",
//                        new String[] { EncodeDecodeValue.encodeValue(o) });
//            
//            if (c != null)
//                requestParams.put("c",
//                        new String[] { EncodeDecodeValue.encodeValue(c) });
//
//            final StringBuilder urlString = new StringBuilder();
//            urlString.append(m_serviceURL).append(servlet);
//            addQueryParams(urlString, requestParams);
//
//            final URL url = new URL(urlString.toString());
//            conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("DELETE");
//            conn.setDoOutput(false);
//            conn.setDoInput(true);
//            conn.setUseCaches(false);
//            conn.setReadTimeout(0);
//
//            conn.connect();
//
////            if (log.isInfoEnabled())
////                log.info(conn.getResponseMessage());
//
//            final int rc = conn.getResponseCode();
//            
//            if (rc < 200 || rc >= 300) {
//                throw new IOException(conn.getResponseMessage());
//            }
//
//            return getMutationResult(conn);
//            
//        } catch (Throwable t) {
//            // clean up the connection resources
//            if (conn != null)
//                conn.disconnect();
//            throw new RuntimeException(t);
//        }
    	
//    	final RemoteRepository repo = new RemoteRepository(m_serviceURL);
    	
    	final RemoveOp remove = new RemoveOp(s, p, o, c);
    	
    	return m_repo.remove(remove);
    	
    }

    protected void doDeleteWithBody(
            /* final String servlet, */final int ntriples,
            final RDFFormat format) throws Exception {

//        HttpURLConnection conn = null;
//		try {
//
//            final URL url = new URL(m_serviceURL + servlet + "?delete");
//            conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("POST");
//			conn.setDoOutput(true);
//			conn.setDoInput(true);
//			conn.setUseCaches(false);
//			conn.setReadTimeout(0);
//
//            conn
//                    .setRequestProperty("Content-Type", format
//                            .getDefaultMIMEType());

            final byte[] data = genNTRIPLES(ntriples, format);
			
//            conn.setRequestProperty("Content-Length", ""
//                    + Integer.toString(data.length));
//
//			final OutputStream os = conn.getOutputStream();
//			try {
//			    os.write(data);
//				os.flush();
//			} finally {
//				os.close();
//			}
//
//			if (log.isInfoEnabled())
//				log.info(conn.getResponseMessage());
//
//			final int rc = conn.getResponseCode();
//			
//			if (rc < 200 || rc >= 300) {
//				throw new IOException(conn.getResponseMessage());
//			}
//
//		} catch (Throwable t) {
//			// clean up the connection resources
//			if (conn != null)
//				conn.disconnect();
//			throw new RuntimeException(t);
//		}
//
//        // Verify the mutation count.
//        assertEquals(ntriples, getMutationResult(conn).mutationCount);

//            final RemoteRepository repo = new RemoteRepository(m_serviceURL);
            
            final RemoveOp remove = new RemoveOp(data, format);
            
            assertEquals(ntriples, m_repo.remove(remove));
            
    }

	/**
	 * Test of POST w/ BODY having data to be loaded.
	 */
    protected void doInsertWithBodyTest(final String method, final int ntriples,
            /*final String servlet,*/ final RDFFormat format) throws Exception {
        
        final byte[] data = genNTRIPLES(ntriples, format);
//        final File file = File.createTempFile("bigdata-testnssclient", ".data");
        /*
         * Only for testing. Clients should use AddOp(File, RDFFormat).
         */
        final AddOp add = new AddOp(data, format);
        assertEquals(ntriples, m_repo.add(add));
		
		// Verify the expected #of statements in the store.
		{
			final String queryStr = "select * where {?s ?p ?o}";

			final IPreparedTupleQuery query = m_repo.prepareTupleQuery(queryStr);
			
			assertEquals(ntriples, countResults(query.evaluate()));
			
		}

    }

    /**
     * Insert a resource into the {@link NanoSparqlServer}.  This is used to
     * load resources in the test package into the server.
     */
    protected long doInsertbyURL(final String method, final String resource)
         throws Exception {

        final String uri = new File(resource).toURI().toString();

        final AddOp add = new AddOp(uri);

        return m_repo.add(add);

    }

    /**
     * Read the contents of a file.
     * 
     * @param file
     *            The file.
     * @return It's contents.
     */
    protected static String readFromFile(final File file) throws IOException {

        final LineNumberReader r = new LineNumberReader(new FileReader(file));

        try {

            final StringBuilder sb = new StringBuilder();

            String s;
            while ((s = r.readLine()) != null) {

                if (r.getLineNumber() > 1)
                    sb.append("\n");

                sb.append(s);

            }

            return sb.toString();

        } finally {

            r.close();

        }

    }
    
    protected static Graph readGraphFromFile(final File file) throws RDFParseException, RDFHandlerException, IOException {
        
        final RDFFormat format = RDFFormat.forFileName(file.getName());
        
        final RDFParserFactory rdfParserFactory = RDFParserRegistry
                .getInstance().get(format);

        if (rdfParserFactory == null) {
            throw new RuntimeException("Parser not found: file=" + file
                    + ", format=" + format);
        }

        final RDFParser rdfParser = rdfParserFactory
                .getParser();

        rdfParser.setValueFactory(new ValueFactoryImpl());

        rdfParser.setVerifyData(true);

        rdfParser.setStopAtFirstError(true);

        rdfParser
                .setDatatypeHandling(RDFParser.DatatypeHandling.IGNORE);

        final StatementCollector rdfHandler = new StatementCollector();
        
        rdfParser.setRDFHandler(rdfHandler);

        /*
         * Run the parser, which will cause statements to be
         * inserted.
         */

        final FileReader r = new FileReader(file);
        try {
            rdfParser.parse(r, file.toURI().toString()/* baseURL */);
        } finally {
            r.close();
        }
        
        final Graph g = new GraphImpl();
        
        g.addAll(rdfHandler.getStatements());

        return g;

    }
    
    /**
     * Write a graph on a buffer suitable for sending as an HTTP request body.
     * 
     * @param format
     *            The RDF Format to use.
     * @param g
     *            The graph.
     *            
     * @return The serialized data.
     * 
     * @throws RDFHandlerException
     */
    static protected byte[] writeOnBuffer(final RDFFormat format, final Graph g)
            throws RDFHandlerException {

        final RDFWriterFactory writerFactory = RDFWriterRegistry.getInstance()
                .get(format);

        if (writerFactory == null)
            fail("RDFWriterFactory not found: format=" + format);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final RDFWriter writer = writerFactory.getWriter(baos);

        writer.startRDF();

        for (Statement stmt : g) {

            writer.handleStatement(stmt);

        }

        writer.endRDF();

        return baos.toByteArray();

    }

    /**
     * Load and return a graph from a resource.
     * 
     * @param resource
     *            The resource.
     * 
     * @return The graph.
     */
    protected Graph loadGraphFromResource(final String resource)
            throws RDFParseException, RDFHandlerException, IOException {

//        final RDFFormat rdfFormat = RDFFormat.forFileName(resource);

        final Graph g = readGraphFromFile(new File(resource));

        return g;

    }
    
    /**
     * Reads a resource and sends it using an INSERT with BODY request to be
     * loaded into the database.
     * 
     * @param method
     * @param servlet
     * @param resource
     * @return
     * @throws Exception
     */
    protected long doInsertByBody(final String method,
            /*final String servlet,*/ final RDFFormat rdfFormat, final Graph g,
            final URI defaultContext) throws Exception {

        final byte[] wireData = writeOnBuffer(rdfFormat, g);

//        HttpURLConnection conn = null;
//        try {
//
//            final URL url = new URL(m_serviceURL
//                    + servlet
//                    + (defaultContext == null ? ""
//                            : ("?context-uri=" + URLEncoder.encode(
//                                    defaultContext.stringValue(), "UTF-8"))));
//            conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod(method);
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//            conn.setUseCaches(false);
//            conn.setReadTimeout(0);
//
//            conn.setRequestProperty("Content-Type",
//                    rdfFormat.getDefaultMIMEType());
//
//            final byte[] data = wireData;
//
//            conn.setRequestProperty("Content-Length",
//                    Integer.toString(data.length));
//
//            final OutputStream os = conn.getOutputStream();
//            try {
//                os.write(data);
//                os.flush();
//            } finally {
//                os.close();
//            }
//            // conn.connect();
//
//            final int rc = conn.getResponseCode();
//
//            if (log.isInfoEnabled()) {
//                log.info("*** RESPONSE: " + rc + " for " + method);
//                // log.info("*** RESPONSE: " + getResponseBody(conn));
//            }
//
//            if (rc < 200 || rc >= 300) {
//
//                throw new IOException(conn.getResponseMessage());
//
//            }
//
//            return getMutationResult(conn);
//
//        } catch (Throwable t) {
//            // clean up the connection resources
//            if (conn != null)
//                conn.disconnect();
//            throw new RuntimeException(t);
//        }

//        final RemoteRepository repo = new RemoteRepository(m_serviceURL);
        final AddOp add = new AddOp(wireData, rdfFormat);
        if (defaultContext != null)
        	add.setContext(defaultContext);
        return m_repo.add(add);

    }
    
    protected static String getResponseBody(final HttpURLConnection conn)
            throws IOException {

        final Reader r = new InputStreamReader(conn.getInputStream());

        try {

            final StringWriter w = new StringWriter();

            int ch;
            while ((ch = r.read()) != -1) {

                w.append((char) ch);

            }

            return w.toString();
        
        } finally {
            
            r.close();
            
        }
        
    }

    /**
     * Inserts some data into the KB and then issues a DESCRIBE query against
     * the REST API and verifies the expected results.
     * 
     * @param format
     *            The format is used to specify the Accept header.
     * 
     * @throws Exception
     */
    protected void doDescribeTest(final String method, final RDFFormat format)
            throws Exception {
        
        final URI mike = new URIImpl(BD.NAMESPACE + "Mike");
        final URI bryan = new URIImpl(BD.NAMESPACE + "Bryan");
        final URI person = new URIImpl(BD.NAMESPACE + "Person");
        final URI likes = new URIImpl(BD.NAMESPACE + "likes");
        final URI rdf = new URIImpl(BD.NAMESPACE + "RDF");
        final URI rdfs = new URIImpl(BD.NAMESPACE + "RDFS");
        final Literal label1 = new LiteralImpl("Mike");
        final Literal label2 = new LiteralImpl("Bryan");

      {
         final Graph g = new GraphImpl();
         g.add(mike, RDF.TYPE, person);
         g.add(mike, likes, rdf);
         g.add(mike, RDFS.LABEL, label1);
         g.add(bryan, RDF.TYPE, person);
         g.add(bryan, likes, rdfs);
         g.add(bryan, RDFS.LABEL, label2);

         m_repo.add(new AddOp(g));
      }

        // The expected results.
        final Graph expected = new GraphImpl();
        {
            expected.add(new StatementImpl(mike, likes, rdf));
            expected.add(new StatementImpl(mike, RDF.TYPE, person));
            expected.add(new StatementImpl(mike, RDFS.LABEL, label1));
        }
        
        // Run the query and verify the results.
        {
            
        	final String queryStr =
                "prefix bd: <"+BD.NAMESPACE+"> " +//
                "prefix rdf: <"+RDF.NAMESPACE+"> " +//
                "prefix rdfs: <"+RDFS.NAMESPACE+"> " +//
                "DESCRIBE ?x " +//
                "WHERE { " +//
                "  ?x rdf:type bd:Person . " +//
                "  ?x bd:likes bd:RDF " +//
                "}";

        	assertSameGraph(expected, m_repo.prepareGraphQuery(queryStr));
            
        }

    }
    
    /**
    * Sets up a simple data set on the server.
    * 
    * @throws Exception
    */
    protected void setupDataOnServer() throws Exception {
        
        final URI mike = new URIImpl(BD.NAMESPACE + "Mike");
        final URI bryan = new URIImpl(BD.NAMESPACE + "Bryan");
        final URI person = new URIImpl(BD.NAMESPACE + "Person");
        final URI likes = new URIImpl(BD.NAMESPACE + "likes");
        final URI rdf = new URIImpl(BD.NAMESPACE + "RDF");
        final URI rdfs = new URIImpl(BD.NAMESPACE + "RDFS");
        final Literal label1 = new LiteralImpl("Mike");
        final Literal label2 = new LiteralImpl("Bryan");

      {
         final Graph g = new GraphImpl();
         g.add(mike, RDF.TYPE, person);
         g.add(mike, likes, rdf);
         g.add(mike, RDFS.LABEL, label1);
         g.add(bryan, RDF.TYPE, person);
         g.add(bryan, likes, rdfs);
         g.add(bryan, RDFS.LABEL, label2);

         m_repo.add(new AddOp(g));
      }

    }
    
    /**
     * Sets up a simple data set on the server.
    * @throws Exception 
     */
    protected void setupQuadsDataOnServer() throws Exception {
        
        final URI mike = new URIImpl(BD.NAMESPACE + "Mike");
        final URI bryan = new URIImpl(BD.NAMESPACE + "Bryan");
        final URI person = new URIImpl(BD.NAMESPACE + "Person");
        final URI likes = new URIImpl(BD.NAMESPACE + "likes");
        final URI rdf = new URIImpl(BD.NAMESPACE + "RDF");
        final URI rdfs = new URIImpl(BD.NAMESPACE + "RDFS");
        final URI c1 = new URIImpl(BD.NAMESPACE + "c1");
        final URI c2 = new URIImpl(BD.NAMESPACE + "c2");
        final URI c3 = new URIImpl(BD.NAMESPACE + "c3");
        final Literal label1 = new LiteralImpl("Mike");
        final Literal label2 = new LiteralImpl("Bryan");

      {
         final Graph g = new GraphImpl();
         g.add(mike, RDF.TYPE, person, c1, c2, c3);
         g.add(mike, likes, rdf, c1, c2, c3);
         g.add(mike, RDFS.LABEL, label1, c1, c2, c3);
         g.add(bryan, RDF.TYPE, person, c1, c2, c3);
         g.add(bryan, likes, rdfs, c1, c2, c3);
         g.add(bryan, RDFS.LABEL, label2, c1, c2, c3);
         m_repo.add(new AddOp(g));
      }

    }
    
    protected void doConstructTest(final String method, final RDFFormat format)
            throws Exception {
        
        setupDataOnServer();
        final URI mike = new URIImpl(BD.NAMESPACE + "Mike");
        final URI bryan = new URIImpl(BD.NAMESPACE + "Bryan");
        final URI person = new URIImpl(BD.NAMESPACE + "Person");

        // The expected results.
        final Graph expected = new GraphImpl();
        {
//            expected.add(new StatementImpl(mike, likes, rdf));
            expected.add(new StatementImpl(mike, RDF.TYPE, person));
            expected.add(new StatementImpl(bryan, RDF.TYPE, person));
//            expected.add(new StatementImpl(mike, RDFS.LABEL, label1));
        }
        
        // Run the query and verify the results.
        {

            final String queryStr =
                "prefix bd: <"+BD.NAMESPACE+"> " +//
                "prefix rdf: <"+RDF.NAMESPACE+"> " +//
                "prefix rdfs: <"+RDFS.NAMESPACE+"> " +//
                "CONSTRUCT { ?x rdf:type bd:Person }" +//
                "WHERE { " +//
                "  ?x rdf:type bd:Person . " +//
//                "  ?x bd:likes bd:RDF " +//
                "}";

            final IPreparedGraphQuery query = m_repo.prepareGraphQuery(queryStr);

//            final Graph actual = asGraph(query.evaluate());

            assertSameGraph(expected, query);
            
        }
    
    }
    
   protected void assertSameGraph(final Graph expected,
         final IPreparedGraphQuery actual) throws Exception {

      assertSameGraph(expected, asGraph(actual));

    }
    
    /**
     * Compare two graphs for equality.
     * <p>
     * Note: This is not very efficient if the {@link Graph} implementations are
     * not indexed.
     * <p>
     * Note: This does not handle equality testing with blank nodes (it does not
     * test for isomorphic graphs).
     * 
     * @param expected
     * @param actual
     */
    protected void assertSameGraph(final Graph expected, final Graph actual) {

        for (Statement s : expected) {

            if (!actual.contains(s))
                fail("Expecting: " + s);

        }

        assertEquals("size", expected.size(), actual.size());

    }

    /**
    * Preferred version executes the {@link IPreparedGraphQuery} and ensures
    * that the {@link GraphQueryResult} is closed.
    * 
    * @param preparedQuery
    *           The prepared query.
    * 
    * @return The resulting graph.
    * 
    * @throws Exception
    */
    protected Graph asGraph(final IPreparedGraphQuery preparedQuery) throws Exception {

       final GraphQueryResult result = preparedQuery.evaluate();
       
       try {
          
          final Graph g = new GraphImpl();

          while (result.hasNext()) {

             g.add(result.next());

          }

          return g;
          
       } finally {
          
          result.close();
          
       }
       
    }

   /**
    * @deprecated by {@link #asGraph(IPreparedGraphQuery)} which can ensure that
    *             the {@link GraphQueryResult} is closed.
    */
   protected Graph asGraph(final GraphQueryResult result) throws Exception {

      try {
         final Graph g = new GraphImpl();

         while (result.hasNext()) {

            g.add(result.next());

         }

         return g;
      } finally {
         result.close();
      }

    }

    /**
     * Return the #of solutions in a result set.
     * 
     * @param result
     *            The result set.
     *            
     * @return The #of solutions.
     */
    protected long countResults(final RepositoryResult<Statement> result)
            throws Exception {

		try {
			long i;
			for (i = 0; result.hasNext(); i++) {
				result.next();
			}
			return i;
		} finally {
			result.close();
		}

    }

    /**
     * Return the exact number of statements in the repository.
     */
    protected long getExactSize() {

        return getSail().getDatabase().getStatementCount(true/* true */);

    }

    protected void doStressDescribeTest(final String method, final RDFFormat format, final int tasks, final int threads, final int statements)
            throws Exception {
        
        final URI person = new URIImpl(BD.NAMESPACE + "Person");
        final URI likes = new URIImpl(BD.NAMESPACE + "likes");
        final URI rdf = new URIImpl(BD.NAMESPACE + "RDF");
        final URI rdfs = new URIImpl(BD.NAMESPACE + "RDFS");

        {
           // create a large number of mikes and bryans
           final Graph g = new GraphImpl();
           for (int n = 0; n < statements; n++) {
               final URI miken = new URIImpl(BD.NAMESPACE + "Mike#" + n);
               final URI bryann = new URIImpl(BD.NAMESPACE + "Bryan#" + n);
               final Literal nameMiken = new LiteralImpl("Mike#" + n);
               final Literal nameBryann = new LiteralImpl("Bryan#" + n);
              g.add(miken, RDF.TYPE, person);
              g.add(miken, likes, rdf);
              g.add(miken, RDFS.LABEL, nameMiken);
              g.add(bryann, RDF.TYPE, person);
              g.add(bryann, likes, rdfs);
              g.add(bryann, RDFS.LABEL, nameBryann);
           }

           m_repo.add(new AddOp(g));
           
        }
        
        // The expected results.
        // Run the query and verify the results.
        {
            
        	final String queryStr =
                "prefix bd: <"+BD.NAMESPACE+"> " +//
                "prefix rdf: <"+RDF.NAMESPACE+"> " +//
                "prefix rdfs: <"+RDFS.NAMESPACE+"> " +//
                "DESCRIBE ?x " +//
                "WHERE { " +//
                "  ?x rdf:type bd:Person . " +//
                "  ?x bd:likes bd:RDF " +//
                "}";

        	final AtomicInteger errorCount = new AtomicInteger();
        	final Callable<Void> task = new Callable<Void>() {

				@Override
				public Void call() throws Exception {
					try {

					      final Graph actual = asGraph(m_repo.prepareGraphQuery(queryStr));
			
			            assertTrue(!actual.isEmpty());
			            
						return null;
					} catch (Exception e) {
						log.warn("Call failure", e);
						
						errorCount.incrementAndGet();
						
						throw e;
					}
				}
        		
        	};
        	
        	final int threadCount = Thread.activeCount();
        	
        	final ExecutorService exec = Executors.newFixedThreadPool(threads);
        	for (int r = 0; r < tasks; r++) {
        		exec.submit(task);
        	}
        	exec.shutdown();
        	exec.awaitTermination(2000, TimeUnit.SECONDS);
        	// force shutdown
        	exec.shutdownNow();
        	
        	int loops = 20;
        	while (Thread.activeCount() > threadCount && --loops > 0) {
        		Thread.sleep(500);
            	if (log.isTraceEnabled())
            		log.trace("Extra threads: " + (Thread.activeCount() - threadCount));
        	}
            
        	if (log.isInfoEnabled())
        		log.info("Return with extra threads: " + (Thread.activeCount() - threadCount));
    		
        	assertTrue(errorCount.get() == 0);
        }

    }

}
