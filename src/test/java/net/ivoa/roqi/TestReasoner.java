package net.ivoa.roqi;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import junit.framework.TestCase;
import net.ivoa.roqi.model.AvailableUCD;
import net.ivoa.roqi.model.ClassTreeNode;
import net.ivoa.roqi.model.RegistryResource;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestReasoner 
extends TestCase 
{

	private static final Logger logger = Logger.getLogger(TestReasoner.class);

//	private static URI roqiTestURI = URI.create("http://archive.astro.umd.edu/owl/roqi_test.owl#");
	private static final String ucdURI = Constant.UCDOntURI + "#";

	private static RoqiReasoner rea = null;

	private	ClassPathXmlApplicationContext appContext 
			= new ClassPathXmlApplicationContext(new String[] {"test-reasoner-config.xml"});
		
	@Override
	protected void setUp() 
	throws Exception 
	{
		
		//BasicConfigurator.configure();
		//System.setProperty("log4j.configuration", "src/test/resources/log4j.properites");
		
		if (rea == null)
		{
			rea = (RoqiReasoner) appContext.getBean("reasoner");
//			logger.debug("SETTING REASONER: "+rea);
		}
		
		super.setUp();
	}

	public void test1() {
		logger.info("= Test Reasoner findAllSubjects");
		ClassTreeNode subjects = rea.findAllSubjects();
		
		//logger.info("Reasoner.findAllSubjects() returns subjects "+subjects.getChildCount());
		assertTrue("Reasoner.findAllSubjects() returns subjects ",subjects.getChildCount() > 0);

		// check top subjects, make sure no repeats! and count unique ones 
		Set<String> topSubjects = new HashSet<String>();
		for (ClassTreeNode child : subjects.getChildren()) { 
			logger.debug(" got top subject:"+child.getName()); 
			if (!topSubjects.contains(child.getName()))
				topSubjects.add(child.getName());
			else
				assertTrue("Got reapeated top subject?!? name:"+child.getName(),false);
		}
		printClassTreeNode(subjects,"");
		
		assertEquals("Got expected number of top subjects",3,topSubjects.size());
	}

	/*
	public void test2a() {
		logger.info("= Test Reasoner findAvailableUCDS for Subject");
		Collection<AvailableUCD> ucds = rea.findAvailableUCDs("subject");
		assertTrue("subject: Subject has ucds ",ucds.size() == 2);
		for (AvailableUCD ucd: ucds ) { logger.debug(" Subject : got ucd:"+ucd); }
	}
	
	public void test2b() {
		logger.info("= Test Reasoner findAvailableUCDS for Galaxies ");
		Collection<AvailableUCD> ucds = rea.findAvailableUCDs("galaxies");
		assertTrue("galaxy has no ucds ",ucds.size() == 0);
		for (AvailableUCD ucd: ucds ) { logger.debug(" Galaxy : got ucd:"+ucd); }
	}
	
	public void test2c() {
		logger.info("= Test Reasoner findAvailableUCDS for abundances");
		Collection<AvailableUCD> ucds = rea.findAvailableUCDs("abundances");
		assertTrue("abundances has ucds ",ucds.size() == 2);
		for (AvailableUCD ucd: ucds ) { logger.debug(" abundances : got ucd:"+ucd); }
		
	}

	public void test2d() {
		logger.info("= Test Reasoner findAvailableUCDS for stars");
		Collection<AvailableUCD> ucds = rea.findAvailableUCDs("stars");
		assertTrue("stars has ucds ",ucds.size() == 2);
		for (AvailableUCD ucd: ucds ) { logger.debug(" stars : got ucd:"+ucd); }
	}
	
	public void test2e() {
		logger.info("= Test Reasoner findAvailableUCDS for abundances--stars");
		Collection<AvailableUCD> ucds = rea.findAvailableUCDs("abundances--stars");
		assertTrue("stars has ucds ",ucds.size() == 2);
		for (AvailableUCD ucd: ucds ) { logger.debug(" stars : got ucd:"+ucd); }
	}
	
	public void test2f() {
		logger.info("= Test Reasoner findAvailableUCDS for abundances--galaxies");
		Collection<AvailableUCD> ucds = rea.findAvailableUCDs("abundances--galaxies");
		assertTrue("stars has no ucds ",ucds.size() == 0);
		for (AvailableUCD ucd: ucds ) { logger.debug(" stars : got ucd:"+ucd); }
	}
	
	public void test3a() {
		logger.info("= Test with no UCD constraints. Should match all instances (1)"); 
		List<String> ucds = new Vector<String>();
		List<RegistryResource> resources =  rea.findMatchingVOResources("abundances--stars", ucds); 

		assertEquals("Reasoner finds correct number of Registry resources ", 1, resources.size());
		RegistryResource r = resources.get(0);
		assertEquals("got correct test resource","ivo://resource1",r.getIdentifier()); 

		// while we are here, check its metadata (just here, once)
		assertTrue ("got a desc", !r.getDescription().equals(""));
		assertTrue ("got a footprint ", !r.getFootprint().equals(""));
		assertTrue ("got a waveband", r.getWaveband().equals("Optical"));
		assertEquals("Reasoner finds correct number of capabilities", 3, r.getCapabilities().size());

	}
	
	public void test3a2() {
		logger.info("= Test with no UCD constraints. Match on Superclass. Should match all instances (1)"); 
		List<String> ucds = new Vector<String>();
		List<RegistryResource> resources =  rea.findMatchingVOResources("stars", ucds); 

		assertEquals("Reasoner finds correct number of Registry resources ", 1, resources.size());
		RegistryResource r = resources.get(0);
		assertEquals("got correct test resource","ivo://resource1",r.getIdentifier()); 

		// while we are here, check its metadata (just here, once)
		assertTrue ("got a desc", !r.getDescription().equals(""));
		assertTrue ("got a footprint ", !r.getFootprint().equals(""));
		assertTrue ("got a waveband", r.getWaveband().equals("Optical"));

		assertEquals("Reasoner finds correct number of capabilities", 3, r.getCapabilities().size());

	}

	public void test3b() {
		logger.info("= Test where the UCD constraints are a subclass of the ucd's contained, therefore the reasoner returns no resources."); 
		List<String> ucds = new Vector<String>();
		ucds.add(ucdURI+"Metaidcross");
		List<RegistryResource> resources =  rea.findMatchingVOResources("abundances--stars", ucds); 
		assertEquals("Reasoner finds correct number of Registry resources ", 0, resources.size());

	}

	public void test3c() {
		logger.info("= Test where one UCD constraint is given (but 0 specified in resource), returns 1 resources"); 
		List<String> ucds = new Vector<String>();
		ucds.add(ucdURI+"Metaid");
		List<RegistryResource> resources =  rea.findMatchingVOResources("galaxies", ucds); 
		assertEquals("Reasoner finds correct number of Registry resources ", 0, resources.size());

	}

	public void test3c2() {
		logger.info("= Test where one UCD constraint is given (but 2 specified in resource), returns 1 resources"); 
		List<String> ucds = new Vector<String>();
		ucds.add(ucdURI+"Metaid");
		List<RegistryResource> resources =  rea.findMatchingVOResources("abundances--stars", ucds); 
		assertEquals("Reasoner finds correct number of Registry resources ", 1, resources.size());

	}
	
	public void test3d() {
		logger.info("= Test where UCD constraints are superclasses of existing ones, returns 1 resources"); 
		List<String> ucds = new Vector<String>();
		ucds.add(ucdURI+"Photflux");
		ucds.add(ucdURI+"Meta");
		List<RegistryResource> resources =  rea.findMatchingVOResources("abundances--stars", ucds); 

		assertEquals("Reasoner finds correct number of Registry resources ", 1, resources.size());
		
		RegistryResource r = resources.get(0);
		assertEquals("got correct test resource",r.getIdentifier(),"ivo://resource1"); 

	}

	public void test3e() {
		logger.info("= Test where UCD constraints dont exist (in any resource), returns 0 resources"); 
		List<String> ucds = new Vector<String>();
		ucds.add(ucdURI+"Spect");
		List<RegistryResource> resources =  rea.findMatchingVOResources("abundances", ucds); 

		assertEquals("Reasoner finds correct number of Registry resources ", 0, resources.size());

	}

	public void test3f() {
		logger.info("= Test where both the subject and requiredUCDs are all superclasses of existing, returns 1 resources"); 
		List<String> ucds = new Vector<String>();
		ucds.add(ucdURI+"Meta");
		List<RegistryResource> resources =  rea.findMatchingVOResources("subject", ucds); 

		assertEquals("Reasoner finds correct number of Registry resources ", 1, resources.size());

		RegistryResource r = resources.get(0);
		assertEquals("got correct test resource","ivo://resource1",r.getIdentifier()); 

	}
	*/
	
	private void printClassTreeNode (ClassTreeNode node, String msg) {
		for (ClassTreeNode n : node.getChildren()) { 
			logger.debug(msg+n.getName());
			printClassTreeNode(n,"  "+msg);
		}
	}
}
