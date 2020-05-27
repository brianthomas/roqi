/**
 * 
 */
package net.ivoa.roqi;

import java.net.URI;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;
import net.ivoa.roqi.model.Capability;
import net.ivoa.roqi.model.Interface;
import net.ivoa.roqi.model.RegistryResource;
import net.ivoa.roqi.model.Ucd;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTemplate;

import edu.noao.nsa.util.sqlexec.SQLExec;

/** Plumb the depths of the ORM and associated model/entities
 * 
 * @author thomas
 */
public class TestORM 
extends TestCase {


	private static final Logger logger = Logger.getLogger(TestORM.class);

	private static final String mapName = "roqidb.map";
	private static final String dbName = "roqi_testorm_db";

	private static final String dbUsername = "sa";
	private static final String dbUserpass = "password1";
	private static final String dbDriverClassname = "org.h2.Driver";
	private static final URI db_uri = URI.create("jdbc:h2:mem:"+dbName);

	protected final static String sourceDir = "target/classes";

	private  ClassPathXmlApplicationContext appContext 
		= new ClassPathXmlApplicationContext(new String[] {"test-orm-config.xml"});

	private static HibernateTemplate db;

	@Override
	protected void setUp() 
	throws Exception 
	{
		if (db == null)
		{
			db = ((HibernateTemplate) appContext.getBean("hibernateTemplate"));
			SQLExec loader = getSQLExecLoader(dbName, mapName);
			loader.execute();
			logger.info("initialized new db "+db);
		}
	}

	// create and insert an instance 
	public void test1() 
	throws Exception
	{
		logger.info("test create and insert a new instance "+db);
		
		assertEquals("have no instances in db",0,listResources(db).size());

		// now create and add an instance
		RegistryResource r = createTestResource();

		insertInstance(db,r); 
		assertEquals("have a single instance",1,listResources(db).size());

	}

	public void test2() {
		logger.info("test retrieve instance");
		

		List<RegistryResource> resources = listResources(db);
		assertEquals("have a single instance",1,resources.size());

		RegistryResource check = resources.get(0);
		RegistryResource orig = createTestResource();

		// resourceId wont be the same as we inserted w/ no assigned value
		assertNotSame("resourceId not matching between resources",check.getResourceId(),orig.getResourceId());
		assertEquals("footprint matches between resources",check.getFootprint(), orig.getFootprint());
		assertEquals("identifier matches between resources",check.getIdentifier(), orig.getIdentifier());
		assertEquals("rights matches between resources",check.getRights(),orig.getRights());
		assertEquals("shortname matches between resources",check.getShortName(),orig.getShortName());
		assertEquals("title matches between resources",check.getTitle(),orig.getTitle());
		assertEquals("valBy matches between resources",check.getValidatedBy(),orig.getValidatedBy());
		assertEquals("val value matches between resources",check.getValidationLvlValue(),orig.getValidationLvlValue());
		
		assertEquals("num of capabilities match between resources",check.getCapabilities().size(),orig.getCapabilities().size());
		assertEquals("num of ucds match between resources",check.getUcds().size(),orig.getUcds().size());
		
		Capability orig_cap = orig.getCapabilities().iterator().next();
		Capability check_cap = check.getCapabilities().iterator().next();
		
		// capabilityId wont be the same as we inserted w/ no assigned value
		assertNotSame("capId not matching between capabilities",check_cap.getCapabilityId(),orig_cap.getCapabilityId());
		assertEquals("name matches between capability1",check_cap.getName(),orig_cap.getName());
		assertEquals("standardID matches between capability1",check_cap.getStandardId(),orig_cap.getStandardId());
		assertEquals("xsiType matches between capability1",check_cap.getXsiType(),orig_cap.getXsiType());
		
		Interface orig_iface = orig_cap.getInterface();
		Interface check_iface = check_cap.getInterface();
		
		assertNotSame("ifaceId not matching between ifaces",check_iface.getInterfaceId(),orig_iface.getInterfaceId());
		assertEquals("queryType matches between iface",check_iface.getQueryType(),orig_iface.getQueryType());
		assertEquals("resultType matches between iface",check_iface.getResultType(),orig_iface.getResultType());
		assertEquals("role matches between iface",check_iface.getRole(),orig_iface.getRole());
		assertEquals("type matches between iface",check_iface.getType(),orig_iface.getType());
		assertEquals("use matches between iface",check_iface.getUse(),orig_iface.getUse());
		assertEquals("url matches between iface",check_iface.getUrl(),orig_iface.getUrl());
		
		Ucd orig_ucd = orig.getUcds().iterator().next();
		Ucd check_ucd = check.getUcds().iterator().next();

		assertNotSame("ucdId not matching between ucds",check_ucd.getUcdId(),orig_ucd.getUcdId());
		assertEquals("name matches between ucd",check_ucd.getName(),orig_ucd.getName());
		assertEquals("desc matches between ucd",check_ucd.getDescription(),orig_ucd.getDescription());
		assertEquals("uri matches between ucd",check_ucd.getUri(),orig_ucd.getUri());
		
	}

	// update instance
	public void test3() {
		logger.info("check update instance");

		List<RegistryResource> resources = listResources(db);
		
		RegistryResource toUpdate = resources.get(0);
		String new_desc = "The dude abides";
		toUpdate.setDescription(new_desc);
		
		insertInstance(db,toUpdate);
		
		List<RegistryResource> updatedResources = listResources(db);
		
		RegistryResource updated = updatedResources.get(0);
		
		assertEquals("got unchanged resourceId", updated.getResourceId(),toUpdate.getResourceId());
		assertEquals("got updated desc", new_desc, updated.getDescription());
		
	}

	// delete instance
//	public void test4()  { assertTrue(false); }

	private SQLExec getSQLExecLoader (String dbName, String mapFileName) 
	{ 

		// instanciate
		SQLExec loader = new SQLExec();

		// to basic configure the loader
		loader.setDriver(dbDriverClassname);
		loader.setSrcDir(sourceDir); // use the file in the target test directory

		loader.setUserid(dbUsername);
		loader.setPassword(dbUserpass);

		logger.debug("Set loader URL to:"+db_uri.toASCIIString());
		loader.setUrl(db_uri.toASCIIString());

		logger.debug("looking for mapfile :"+mapFileName);
		URL fileURL = getClass().getClassLoader().getResource(mapFileName);
		logger.debug("found mapfile URL:"+fileURL.toString());
		loader.addSqlDbMapURL(fileURL);

		return loader;
	}

	private static void insertInstance (HibernateTemplate db, RegistryResource r) 
	{
		db.saveOrUpdate(r);
	}

	@SuppressWarnings("unchecked")
	private static List<RegistryResource> listResources (HibernateTemplate db) 
	{
		List<RegistryResource> resources = (List<RegistryResource>) db.find("from RegistryResource as r inner join fetch r.ucdSet");
		return resources;
	}

	private static RegistryResource createTestResource() {

		RegistryResource r = new RegistryResource();
		// now create and add an instance
		r.setIdentifier("urn:someURIForTheDataSet");
		r.setShortName("shortName1");
		r.setDescription("description here");
		r.setFootprint("footprint1");
		r.setRights("rights1");
		r.setTitle("title1");
		r.setValidatedBy("validatedBy1");
		r.setValidationLvlValue("val_value1");

		Capability c = new Capability();
		c.setXsiType("xsitype1");
		c.setStandardId("standardId1");

		Interface iface = new Interface();
		iface.setQueryType("qtype1");
		iface.setResultType("rtype1");
		iface.setRole("role1");
		iface.setType("type1");
		iface.setUrl("url1");
		iface.setUse("use1"); 
 
		c.setInterface(iface);
		r.addCapability(c);

		Ucd ucd = new Ucd();
		ucd.setUri("urn:ucd1");
		ucd.setName("ucd1");
		ucd.setDescription("ucd1desc");
		r.addUcd(ucd);

		return r;
	}
}

