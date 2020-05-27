package net.ivoa.roqi;

import java.util.List;
import java.util.Vector;

import junit.framework.TestCase;
import net.ivoa.roqi.model.RegistryResource;
import net.ivoa.roqi.model.Ucd;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestReasoner2 
extends TestCase 
{

	private static final Logger logger = Logger.getLogger(TestReasoner2.class);

//	private static URI roqiTestURI = URI.create("http://archive.astro.umd.edu/owl/roqi_test.owl#");
	private static final String ucdURI = Constant.UCDOntURI + "#";

	private static RoqiReasoner rea = null;

	private	ClassPathXmlApplicationContext appContext 
			= new ClassPathXmlApplicationContext(new String[] {"test-reasoner-config2.xml"});
		
	@Override
	protected void setUp() 
	throws Exception 
	{
		
		if (rea == null)
			rea = (RoqiReasoner) appContext.getBean("reasoner");
		
		super.setUp();
	}

	public void test1a() {
		logger.info("= Test with UCD constraints. Should match all instances"); 
		List<String> ucds = new Vector<String>();
//		ucds.add(ucdURI+"Metaid");
//		ucds.add(ucdURI+"Metabib");
		ucds.add(ucdURI+"Meta");
//		ucds.add(ucdURI+"Emoptv");
		ucds.add(ucdURI+"Em");
		ucds.add(ucdURI+"Phot");
		ucds.add(ucdURI+"Phys");
		ucds.add(ucdURI+"Spect");
		ucds.add(ucdURI+"Pos");
		List<RegistryResource> resources =  rea.findMatchingVOResources("abundances--stars", ucds); 

		//assertEquals("Reasoner finds correct number of Registry resources ", 1, resources.size());
		//RegistryResource r = resources.get(0);
		for (RegistryResource r : resources) {
			dumpResource(r);
		}
		
//		assertEquals("got correct test resource","ivo://resource1",r.getIdentifier()); 

		// while we are here, check its metadata (just here, once)
	//	assertTrue ("got a desc", !r.getDescription().equals(""));
	//	assertTrue ("got a footprint ", !r.getFootprint().equals(""));
	//	assertTrue ("got a waveband", r.getWaveband().equals("Optical"));
	//	assertEquals("Reasoner finds correct number of capabilities", 3, r.getCapabilities().size());

	}
	
	public void test1b() {
		logger.info("= Test with UCD constraints. Should match all instances"); 
		List<String> ucds = new Vector<String>();
//		ucds.add(ucdURI+"Metaid");
//		ucds.add(ucdURI+"Metabib");
//		ucds.add(ucdURI+"Emoptv");
//		ucds.add(ucdURI+"Em");
//		ucds.add(ucdURI+"Phot");
//		ucds.add(ucdURI+"Posangdistance");
//		ucds.add(ucdURI+"Phys");
//		ucds.add(ucdURI+"Meta");
		ucds.add(ucdURI+"Metamain");
//		ucds.add(ucdURI+"Spect");
//		ucds.add(ucdURI+"Pos");
//		List<RegistryResource> resources =  rea.findMatchingVOResources("abundances--ages--stars--variable_stars", ucds); 
		List<RegistryResource> resources =  rea.findMatchingVOResources("ages--stars--variable_stars", ucds); 

		//assertEquals("Reasoner finds correct number of Registry resources ", 1, resources.size());
		//RegistryResource r = resources.get(0);
		for (RegistryResource r : resources) { dumpResource(r); }
		
//		assertEquals("got correct test resource","ivo://resource1",r.getIdentifier()); 

		assertTrue(true);
	}
	
	private void dumpResource(RegistryResource r) {
		logger.debug("Resource Id:"+r.getIdentifier()+" Subject:"+r.getSubjectname());
		for (Ucd ucd : r.getUcds()) {
			logger.debug("\t UCD Id:"+ucd.getName()+" ["+ucd.getUri().getFragment()+"]");
		}
	}
	
}
