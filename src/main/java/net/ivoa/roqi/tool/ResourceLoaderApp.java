
package net.ivoa.roqi.tool;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import net.ivoa.roqi.Constant;
import net.ivoa.roqi.model.Capability;
import net.ivoa.roqi.model.Interface;
import net.ivoa.roqi.model.RegistryResource;
import net.ivoa.roqi.model.Subject;
import net.ivoa.roqi.model.Ucd;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.SimpleURIMapper;
import org.semanticweb.owl.vocab.OWLRDFVocabulary;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** A loader application to create RegistryResource entries in roqi database.
 * 
 * @author thomas
 *
 */
public class ResourceLoaderApp 
extends AbstractToolApp
{

	private static final long serialVersionUID = 801225321185849159L;

	private static final Logger logger = Logger.getLogger(ResourceLoaderApp.class);

	@Option(name="--config", usage="resourceApp config (spring) file to use", required=false)
	private String springConfig = "roqi-loader.xml";
	public final String getSpringConfig() { return springConfig; }
	public final void setSpringConfig(String hibernateConfig) { this.springConfig = hibernateConfig; }

	@Option(name="--initdb", usage="initialize the database (new instance)", required=false)
	private Boolean initdb = false;
	public final Boolean getInitdb() { return initdb; }
	public final void setInitdb(Boolean initdb) { this.initdb = initdb; }

	@Option(name="--resourcesFile", usage="the XML file containing resources to load", required=true)
	private String resourcesFile;
	public final String getResourcesFile() { return resourcesFile; }
	public final void setResourcesFile(String resourcesFile) { this.resourcesFile = resourcesFile; }

	@Option(name="--ucdOntoUri", usage="physical uri of ucd ontology.", required=false)
	private String ucdPhysicalUriStr = "file:src/main/resources/UCD.owl";

	private static List<Subject> readSubjectsFromXML(String xmlFileUri, HibernateTemplate db) 
	{
		List<Subject> subjects = new Vector<Subject>();
		
		logger.debug("Read Subjects from XMLFile"+xmlFileUri);
		// open file, load the db 
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(xmlFileUri);

			Element root = document.getDocumentElement();
			NodeList subjectNodes = root.getElementsByTagName("subject");

			logger.debug("found subject nodes number:"+ subjectNodes.getLength());
			for (int i=0; i<subjectNodes.getLength(); i++) 
			{
				Node child = subjectNodes.item(i); 
				if (child.getNodeType() == 1) {
					Element elem = (Element) child;
					Subject newSubject = createSubjectFromElement(elem, db);
					StringBuilder newUriStr = new StringBuilder(Constant.RoqiOntURI.toASCIIString());
					newUriStr.append("#").append(newSubject.getUri());
					newSubject.setUri(newUriStr.toString());
					if (newSubject != null)
						subjects.add(newSubject);
				}
			}

		}
		catch (FactoryConfigurationError e) {
			// unable to get a document builder factory
			e.printStackTrace();
		} 
		catch (SAXException e) {
			// parsing error
			e.printStackTrace();
		} 
		catch (IOException e) {
			// i/o error
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		logger.debug("Read Subjects from XMLFile finished");
		
		return subjects;
	}
	
	/*
	private static List<RegistryResource> readResourcesFromXML(String xmlFileUri, HibernateTemplate db) 
	{
		List<RegistryResource> resources = new Vector<RegistryResource>();

		// open resources file, load the db 
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(xmlFileUri);

			Element root = document.getDocumentElement();
			NodeList resourceNodes = root.getElementsByTagName("r:Resource");

			logger.debug("found nodes number:"+ resourceNodes.getLength());
			for (int i=0; i<resourceNodes.getLength(); i++) 
			{
				Node child = resourceNodes.item(i); 
				if (child.getNodeType() == 1) {
					Element elem = (Element) child;
					RegistryResource newResource = createResourceFromElement(elem, db);
					if (newResource != null)
						resources.add(newResource);
				}
			}

		}
		catch (FactoryConfigurationError e) {
			// unable to get a document builder factory
			e.printStackTrace();
		} 
		catch (SAXException e) {
			// parsing error
			e.printStackTrace();
		} 
		catch (IOException e) {
			// i/o error
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		return resources;
	}
	*/

	private static Subject createSubjectFromElement(Element sElement, HibernateTemplate db) 
	{
		Subject subject = new Subject();
		subject.setUri(sElement.getAttribute("rdf:ID"));
			
		Node child = sElement.getFirstChild();
		while (child !=null)
		{

			if (child instanceof Element)
			{
				Element elem = (Element) child;
				
				/*
				if (elem.getTagName().equals("rdfs:comment"))
				{
					subject.setDescription(getText(elem));
				}
				else
				*/
				if (elem.getTagName().equals("rdfs:label"))
				{
					subject.setLabel(getText(elem));
					logger.debug("Got label:"+subject.getLabel());
				}
				else
				if (elem.getTagName().equals("r:Resource"))
				{
					if ( !(elem.getAttribute("rdf:ID").equals("")))
					{
						RegistryResource resource = createResourceFromElement(elem, db);
						if (resource != null)
							subject.addResource(resource);
						else 
							logger.error("Can't load resource:"+elem.getAttribute("rdf:ID")+" in database, not linking"); 
					}
				}
			}
			child = child.getNextSibling();
		}
		return subject;
	}
				

	/** Parse an DOM Element representing the resource into a RegistryResource POJO
	 * 
	 * @param rElement
	 * @return
	 */
	private static RegistryResource createResourceFromElement(Element rElement, HibernateTemplate db) 
	{
		RegistryResource r = new RegistryResource();

		Node child = rElement.getFirstChild();
		while (child !=null)
		{

			if (child instanceof Element)
			{
				Element elem = (Element) child;
				if (elem.getTagName().equals("rdfs:comment"))
				{
					r.setDescription(getText(elem));
				}
				else
					if (elem.getTagName().equals("r:identifier"))
					{
						r.setIdentifier(getText(elem));
						logger.debug("Got identifier:"+r.getIdentifier());
					}
					else
						if (elem.getTagName().equals("r:shortName"))
						{
							r.setShortName(getText(elem));
						}
						else
							if (elem.getTagName().equals("r:title"))
							{
								r.setTitle(getText(elem));
							}
							else
								if (elem.getTagName().equals("r:hasCapability"))
								{
									Capability cap = createCapabilityFromElement(
											(Element) elem.getElementsByTagName("r:Capability").item(0)
									);
									if (cap != null)
										r.addCapability(cap); 
								}
								else
									if (elem.getTagName().equals("r:hasCoverage"))
									{
										setResourceCoverageFields(r,
												(Element) elem.getElementsByTagName("r:Coverage").item(0)
										);
									}
									else
										if (elem.getTagName().equals("r:hasValidationLevel"))
										{
											setResourceValidationLevelFields(r,
													(Element) elem.getElementsByTagName("r:ValidationLevel").item(0)
											);
										}
										else
											if (elem.getTagName().equals("hasUCD"))
											{
												Ucd ucd = null;
												Node hasChild = elem.getFirstChild();
												while (hasChild != null && hasChild.getNodeType() != 1)
													hasChild = hasChild.getNextSibling();

												if (hasChild != null
														&&
													!((Element)hasChild).getAttribute("rdf:ID").equals(""))
												{
													ucd = createUCDFromElement((Element) hasChild, db);

													if (ucd != null)
														r.addUcd(ucd);
													else 
														logger.warn("Can't find ucd:"+((Element)hasChild).getAttribute("rdf:ID")+" in database, not linking"); 
												}
												else
												{
												//	logger.debug("No child ucd node defined for hasUCD?, not linking"); 
												}
												
											}
			}

			child = child.getNextSibling();
		}

		return r;
	}

	private static Capability createCapabilityFromElement (Element capElement) 
	{

		Capability c = new Capability();
		Node child = capElement.getFirstChild();
		while (child !=null)
		{

			if (child instanceof Element)
			{
				Element elem = (Element) child;
				if (elem.getTagName().equals("r:standardId"))
				{
					c.setStandardId(getText(elem)); 
				}
				else
					if (elem.getTagName().equals("xsi:type"))
					{
						c.setXsiType(getText(elem)); 
					}
					else
						if (elem.getTagName().equals("r:hasInterface"))
						{
							Interface iface = createInterfaceFromElement(
									(Element) elem.getElementsByTagName("r:Interface").item(0)
							);
							if (iface != null)
								c.setInterface(iface); 
						}
			}
			child = child.getNextSibling();
		}

		return c;
	}

	@SuppressWarnings("unchecked")
	private static Ucd createUCDFromElement(Element ucdElement, HibernateTemplate db) 
	{

		logger.debug(" GOT UCD element"+ucdElement);
		Ucd ucd = null;
		if (ucdElement != null)
		{
			logger.debug(" tagname: "+ucdElement.getTagName());
			ucd = new Ucd();
			String tagname = ucdElement.getTagName();
			String ucdUri = tagname.substring(tagname.indexOf(":")+1);

			List<Ucd> ucds = db.find("select u from Ucd as u");
			logger.debug(" FOUND "+ucds.size()+" ucds in the db.");

			try {
				ucd = getUcd(Constant.UCDOntURI+"#"+ucdUri,db);
			} catch (NullPointerException e) {
				logger.error(e.getMessage());
			}
		}
		return ucd;
	}

	private static Interface createInterfaceFromElement (Element ifaceElement) 
	{

		Interface i = new Interface();

		Node child = ifaceElement.getFirstChild();
		while (child !=null)
		{

			if (child instanceof Element)
			{
				Element elem = (Element) child;
				if (elem.getTagName().equals("r:queryType"))
				{
					i.setQueryType(getText(elem));
				}
				else
					if (elem.getTagName().equals("r:resultType"))
					{
						i.setResultType(getText(elem));
					}
					else
						if (elem.getTagName().equals("r:role"))
						{
							i.setRole(getText(elem));
						}
						else
							if (elem.getTagName().equals("r:hasAccessURL"))
							{
								setInterfaceAccessURLFields(i,
										(Element) elem.getElementsByTagName("r:AccessURL").item(0)
								);
							}
							else
								if (elem.getTagName().equals("xsi:type"))
								{
									i.setType(getText(elem)); 
								}
			}
			child=child.getNextSibling();
		}
		return i;
	}

	private static void setInterfaceAccessURLFields(Interface i, Element accessURLElem) 
	{

		Node child = accessURLElem.getFirstChild();
		while (child !=null)
		{

			if (child instanceof Element)
			{
				Element elem = (Element) child;
				if (elem.getTagName().equals("r:url"))
				{
					i.setUrl(getText(elem));
				}
				else
					if (elem.getTagName().equals("r:use"))
					{
						i.setUse(getText(elem));
					}
			}
			child = child.getNextSibling();
		}
	}

	private static void setResourceCoverageFields(RegistryResource r, Element coverageElem) 
	{

		Node child = coverageElem.getFirstChild();
		while (child !=null)
		{

			if (child instanceof Element)
			{
				Element elem = (Element) child;
				if (elem.getTagName().equals("r:footprint"))
				{
					r.setFootprint(getText(elem));
				}
				else
					if (elem.getTagName().equals("r:waveband"))
					{
						r.setWaveband(getText(elem));
					}
			}
			child = child.getNextSibling();
		}
	}

	private static void setResourceValidationLevelFields(RegistryResource r, Element valLevelElem) 
	{

		Node child = valLevelElem.getFirstChild();
		while (child !=null)
		{

			if (child instanceof Element)
			{
				Element elem = (Element) child;
				if (elem.getTagName().equals("r:validatedBy"))
				{
					r.setValidatedBy(getText(elem));
				}
				else
					if (elem.getTagName().equals("r:value"))
					{
						r.setValidationLvlValue(getText(elem));
					}
			}
			child = child.getNextSibling();
		}
	}

	private static String getText (Element e) {
		StringBuffer buf = new StringBuffer();

		Node child = e.getFirstChild();
		while (child != null) {

			//			logger.debug("Child node:"+child.getNodeType()+" val:"+child);
			if (child.getNodeType() == 3)
			{
				buf.append(child.getNodeValue());
			}
			child=child.getNextSibling();
		}
		return buf.toString();
	}

	/*
	private static void loadResourceIntoDb (HibernateTemplate db, RegistryResource resource) 
	throws Exception
	{
		if (resource == null )
			throw new NullPointerException("passed null resource, ignorning load");

		logger.info("loading Resource id="+resource.getIdentifier());
		db.save(resource);
	}
	*/
	
	private static void loadSubjectIntoDb (HibernateTemplate db, Subject subject) 
	throws Exception
	{
		if (subject == null )
			throw new NullPointerException("passed null subject, ignorning load");

		logger.info("loading Subject uri="+subject.getUri());
		db.save(subject);
	}

	private void loadUCDsIntoDbFromOntology(HibernateTemplate db) 
	throws Exception 
	{

		URI ucdPhysicalURI = new URI(ucdPhysicalUriStr);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		manager.addURIMapper(new SimpleURIMapper(Constant.UCDOntURI, ucdPhysicalURI));

		OWLOntology ucd_ont = manager.loadOntology(Constant.UCDOntURI);

		OWLClass rootUCDClass = manager.getOWLDataFactory().getOWLClass(Constant.rootUCDURI);

		addUCDSubclassesToDb(rootUCDClass, new Vector<OWLClass>(), ucd_ont, db);

	}

	private void addUCDSubclassesToDb
	(
			OWLClass cls,
			List<OWLClass> didClass,
			OWLOntology ont,
			HibernateTemplate db 
	)
	{

		for (OWLDescription desc: cls.getSubClasses(ont)) 
		{
			OWLClass sub = desc.asOWLClass();
			//logger.debug("Got description :"+desc.toString());

			// ignore owl:Nothing
			if (!sub.isOWLNothing() && !didClass.contains(sub))
			{
				didClass.add(sub); 

				// add to db
				addUCDtoDB(sub, ont, db);

				// do sub-sub classes
				addUCDSubclassesToDb(sub,didClass, ont, db); 
			}

		}
	}

	@SuppressWarnings("unchecked")
	private void  addUCDtoDB (OWLClass ucdCls, OWLOntology ucd_onto, HibernateTemplate db) 
	{
		logger.debug("ADD UCD to Db:"+ucdCls.getURI().toASCIIString());
		Ucd ucd = new Ucd();
		ucd.setUri(ucdCls.getURI().toASCIIString());

		// parse in the name from RDFS:label 
		for (OWLAnnotation a : ucdCls.getAnnotations(ucd_onto,OWLRDFVocabulary.RDFS_LABEL.getURI())) 
		{
			String label = a.getAnnotationValueAsConstant().getLiteral();
			//logger.debug("UCD label value:"+label);
			ucd.setName(label);
		}

		// UCD description fields from RDFS:comment
		StringBuffer desc = new StringBuffer();
		for (OWLAnnotation a : ucdCls.getAnnotations(ucd_onto,OWLRDFVocabulary.RDFS_COMMENT.getURI())) 
		{
			String comment = a.getAnnotationValueAsConstant().getLiteral();
			if (comment.charAt(1) != ':') { desc.append(comment); }
		}
		// logger.debug("UCD desc value:"+desc.toString());
		ucd.setDescription(desc.toString());

		db.save(ucd);

	}


	@SuppressWarnings("unchecked")
	private static Ucd getUcd (String uri, HibernateTemplate db) 
	{
		logger.debug("getUCD uri:"+uri);
		Ucd ucd = null;
		List<Ucd> ucds = db.find("select u from Ucd as u where u.uri = '"+uri+"'");

		logger.debug("Found nrof ucds:"+ucds.size()+" with uri:"+uri);
			
		if (ucds.size() == 1)
			ucd = ucds.get(0);

		logger.debug("Returning ucd:"+ucd);

		return ucd;
	}

	/*
	private static void listUcd ( HibernateTemplate db) 
	{
		Iterator<Ucd> iter = (Iterator<Ucd>) db.find("select u from Ucd as u").iterator();
		while (iter.hasNext())
		{
			Ucd ucd = iter.next();
			logger.debug("Found ucd uri:"+ucd.getUri());
		}
	}
	 */

	@Override
	public void run()
	throws RuntimeException
	{

		logger.info("loading resources from file:"+resourcesFile);
		logger.debug("        using hibernate config:"+springConfig);
		logger.debug("        initdb? :"+initdb);

		ClassPathXmlApplicationContext appContext = 
			new ClassPathXmlApplicationContext(new String[] {springConfig});

		HibernateTemplate db = ((HibernateTemplate) appContext.getBean("hibernateTemplate"));

		// initialize the db? Need to do for new instances
		if (initdb) 
		{
			CreateDbApp initApp = (CreateDbApp) appContext.getBean("dbcreator");
			initApp.run();

			// now load in UCDs from UCD ontology
			try {
				loadUCDsIntoDbFromOntology(db);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		try {

			//	URL fileUrl = ResourceLoaderApp.class.getResource(args[1]);
			String fileUri = "file:" + new File(resourcesFile).getAbsolutePath();

			/*
			for (RegistryResource r: readResourcesFromXML(fileUri, db)) {
				try {
					loadResourceIntoDb(db,r);
				} catch (Exception e) {
					throw new RuntimeException("Could not load resource:"+r.getShortName(),e);
				}
			}
			*/
			
			// now load subjects
			for (Subject s: readSubjectsFromXML(fileUri, db)) {
				try {
					loadSubjectIntoDb(db,s);
				} catch (Exception e) {
					throw new RuntimeException("Could not load subject:"+s.getLabel(),e);
				}
			}
			
			// listUcd(db);
		} catch (Exception e) {
			logger.fatal(e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		logger.info("Finished");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		runProgram(new ResourceLoaderApp(), args);
	}

}
