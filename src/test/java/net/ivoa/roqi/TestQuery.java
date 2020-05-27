/**
 * 
 */
package net.ivoa.roqi;

import java.util.List;

import junit.framework.TestCase;
import net.ivoa.roqi.model.RegistryResource;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author thomas
 *
 */
public class TestQuery 
extends TestCase 
{

	private static final Logger logger = Logger.getLogger(TestQuery.class);

	private  ClassPathXmlApplicationContext appContext 
		= new ClassPathXmlApplicationContext(new String[] {"test-query-config.xml"});

	private static RoqiReasoner reasoner;

	@Override
	protected void setUp() 
	throws Exception 
	{
		if (reasoner == null)
			reasoner = ((RoqiReasoner) appContext.getBean("reasoner"));
	}

	/* In the test ontology, there are 5 classes, abundances, abundances--stars, 
	 * abundances--galaxies, galaxies and stars. The abundance class is the superclass
	 * of the "abundances--*" classes, and the "abundances--galaxies","abundances-stars"
	 * are the respective super classes of the "galaxies" and "stars" classes. The 
	 * "abundances--stars" and "galaxies" classes have resources "resource1" and "resource2" 
	 * respectively associated with them. This means that a query of "abundances--galaxies" 
	 * should return NO resources, a query of "abundances" should return 1 resource, and
	 * a query of  "stars" should return 1 resource. 
	 * 
	 */
	public void test1() {
		logger.info("Test queries with no ucd constraints");
		
		Query q = new QueryForTesting("abundances");
		assertEquals("abundance query has right number of results", 1,  doQueryTest(q) );
		
		Query q2 = new QueryForTesting("galaxies");
		assertEquals("galaxies query has right number of results", 1,  doQueryTest(q2) );
		
		Query q3 = new QueryForTesting("abundances--stars");
		assertEquals("abundances--stars query has right number of results", 1,  doQueryTest(q3) );
		
		// pick up the resource in the superclass "abundances--stars"
		Query q4 = new QueryForTesting("stars");
		assertEquals("stars query has right number of results", 1,  doQueryTest(q4) );
		
		Query q5 = new QueryForTesting("abundances--galaxies");
		assertEquals("abundances--galaxies query has right number of results", 0,  doQueryTest(q5) );
		
	}
	
	public void test2() {
		// TODO
		logger.info("TODO: Test queries with UCD constraints");
		assertTrue(true);
	}
	
	private static int doQueryTest(Query q)
	{
		Result r = new TestQuery().new ResultTestImpl();
		r.setQuery(q);
		List<RegistryResource> results = r.getResults();
		for (RegistryResource res : results) { logger.debug("Resource "+res.getIdentifier()); }
	    return results.size();	
	}

	class QueryForTesting 
	extends QueryImpl
	{
		private static final long serialVersionUID = 1L;
		public QueryForTesting(String subject) { setSubject(subject); }
	}
	
	class ResultTestImpl extends ResultImpl {
		private static final long serialVersionUID = 1L;
		public ResultTestImpl() { setReasoner(reasoner);} 
	}
	
}
