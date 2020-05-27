package net.ivoa.roqi;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import net.ivoa.roqi.model.AvailableUCD;
import net.ivoa.roqi.model.ClassTreeNode;
import net.ivoa.roqi.model.RegistryResource;
import net.ivoa.roqi.model.Subject;
import net.ivoa.roqi.model.Ucd;
import net.ivoa.roqi.model.builder.ModelBuilderException;
import net.ivoa.roqi.model.builder.RoqiModelBuilder;
import net.ivoa.roqi.model.builder.RoqiOWLAPIModelBuilder;

import org.apache.log4j.Logger;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.OWLDescriptionVisitorAdapter;
import org.semanticweb.owl.util.SimpleURIMapper;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class RoqiOWLAPIReasoner 
implements RoqiReasoner
{

	private static final Logger logger = Logger.getLogger(RoqiOWLAPIReasoner.class);

	private ClassTreeNode allSubjects;
	private RoqiModelBuilder builder;
	private OWLOntologyManager manager;
	private HibernateTemplate database;
	private URI roqiPhysicalOntoURI;

	public RoqiOWLAPIReasoner() {
		logger.debug("CREATE NEW REASONER!! with DEFAULT ONTO");
		this.setSubjectOntology("roqi.owl");
	} 
	
	public RoqiOWLAPIReasoner(String uri) { this.setSubjectOntology(uri); } 

	public final void setHibernateTemplate (HibernateTemplate ht) {  this.database = ht; }
	
	// used in testing
	public final RoqiModelBuilder getBuilder() { return builder; }
	
	public final void setSubjectOntology (String uri)
	{
		logger.debug("setSubjectOntology roqi_str_uri:"+uri);

		// initialize our model. 
		try {

			this.roqiPhysicalOntoURI = RoqiOWLAPIReasoner.class.getClassLoader().getResource(uri).toURI();
			logger.debug("roqi_phys_uri: "+roqiPhysicalOntoURI.toASCIIString());

			manager = OWLManager.createOWLOntologyManager();
			
			manager.addURIMapper(new SimpleURIMapper(Constant.RoqiOntURI, roqiPhysicalOntoURI));
		 	manager.addURIMapper(new SimpleURIMapper(Constant.RegistryOntURI, Constant.defaultRegistryPhysicalURI ));
		 	manager.addURIMapper(new SimpleURIMapper(Constant.UCDOntURI, Constant. defaultUCDPhysicalURI));
		 	manager.loadOntology(Constant.RoqiOntURI);
		 	
			builder = new RoqiOWLAPIModelBuilder(manager);

		} 
		catch (URISyntaxException ue) 
		{
			logger.error(ue.getMessage());
			throw new IllegalArgumentException(ue);
		} 
		catch (OWLOntologyCreationException e) 
		{
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	public final ClassTreeNode findAllSubjects () 
	{
		if (allSubjects == null)
		{
			try {
				logger.debug(" findAllSubjects BULIDER:"+builder+" SubjectURI:"+Constant.subjectURI);
				allSubjects = builder.buildClassTreeModel(Constant.subjectURI);
				logger.debug("FOUND "+allSubjects.getChildCount()+" child nodes");
			} catch (ModelBuilderException e) { 
				logger.error(e.getMessage());
			}
		}
		return allSubjects;
	}

	public final Collection<AvailableUCD> findAvailableUCDs (String subjectName)
	{

		logger.debug("findAvailableUCDs called subject:"+subjectName);
		if (subjectName == null || subjectName.equals("")) { return null; }

		Collection<AvailableUCD> data = new Vector<AvailableUCD>();

		Map<URI,Ucd> hasUcd = new HashMap<URI,Ucd>();
		for (RegistryResource r : findMatchingVOResources(subjectName, new Vector<String>()))
		{
			logger.debug("GOT matching resource:"+r.getIdentifier());
			Iterator<Ucd> uiter = r.getUcds().iterator();
			while (uiter.hasNext())
			{
				Ucd ucd = uiter.next();
				if (!hasUcd.containsKey(ucd.getUri()))
					hasUcd.put(ucd.getUri(),ucd);
			}
		}

		for (Entry<URI,Ucd> entry: hasUcd.entrySet())
		{
			Ucd ucd = entry.getValue();
			data.add(new AvailableUCD(ucd.getName(),ucd.getUri().toASCIIString()));
		}

		return data; 
	}

	public final List<RegistryResource> findMatchingVOResources (
			String subjectName, 
			List<String> requiredUCDs
	) 
	{

		List<RegistryResource> matchingResources = new Vector<RegistryResource>();

		logger.debug("findMatchingVOResources called subject:"+subjectName+" nrof_required_ucds :"+requiredUCDs.size());

//			List<Subject> subjects = (List<Subject>) database.find("select s from Subject as s");
//			for (Subject s : subjects) { logger.debug("DB has subject uri:["+s.getUri()+"]"); }

		URI baseSubjectUri = URI.create(Constant.RoqiOntURI.toASCIIString()+"#"+subjectName);
		// Session session = database.getSessionFactory().getCurrentSession();
		try {
			//			List<URI> ucds = new Vector<URI>(); for (String rucd : requiredUCDs) { ucds.add(URI.create(rucd)); }
			//			Set<String> resourceInfo  = 
			//				builder.findMatchingResources(URI.create(Constant.RoqiOntURI.toASCIIString()+"#"+subjectName), ucds);

			Set<URI> subjectUris = findMatchingSubjects(baseSubjectUri); 
			logger.debug("Got "+subjectUris.size()+" matching subjects to "+baseSubjectUri.toASCIIString());
			Iterator<URI> siter = subjectUris.iterator();
			while (siter.hasNext()) {
				URI subjectUri = siter.next();
				Subject subject = getSubject(database, subjectUri);
				if (subject != null)
				{
					logger.debug("Found "+subject.getResources().size()+" possible resources");

					for (RegistryResource res : subject.getResources())
					{
						String subStrName = subjectUri.getFragment();
						res.setSubjectname(subStrName);

						if (isOkToAddResource(res, requiredUCDs))
							matchingResources.add(res);
					}
				}
				else
					logger.info( "Search cant find any subject in db uri:"+subjectUri.toASCIIString()+" no instance in the database, skipping");
			}

		} catch (Exception e) { 
			logger.error(e.getMessage(),e);
		}

		return matchingResources;
	}

	private Set<URI> findMatchingSubjects ( URI subjectUri ) 
	throws ModelBuilderException
	{

		OWLClass subjectCls = manager.getOWLDataFactory().getOWLClass(subjectUri);

		logger.debug("findMatchingSubjects called subCls:"+subjectCls+" in "+roqiPhysicalOntoURI);
		
		OWLOntology onto = manager.getOntology(Constant.RoqiOntURI);
		SubclassVisitor visitor = new SubclassVisitor (Collections.singleton(onto));
		subjectCls.accept(visitor);
		return visitor.getSubclasses();
	}

	@SuppressWarnings("unchecked")
	private Subject getSubject (HibernateTemplate db, URI uri) 
	{
		logger.debug("getSubject uri:"+uri.toASCIIString());
		Subject subject = null;
		//		List<Subject> subjects = (List<Subject>) db.find("from Subject as s left join fetch s.resourceSet as r left join fetch r.ucdSet as u where s.uri='"+uri.toASCIIString()+"'");
		//		List<Subject> subjects = (List<Subject>) db.find("from Subject as s left join fetch s.resourceSet where s.uri='"+uri.toASCIIString()+"'");
		List<Subject> subjects = (List<Subject>) db.find("from Subject as s where s.uri='"+uri.toASCIIString()+"'");
		if (subjects.size() == 1) { subject = subjects.get(0); }
		/*
		else { 
			logger.error("GOT WRONG NUMBER OF SUBJECTS: "+subjects.size()); 
			for (Subject s : subjects) { logger.error(" SUBJECT : "+s.getUri()); }
//			for (Subject s : (List<Subject>) db.find("from Subject as s"))  { logger.error("SUBJECT id:"+s.getSubjectId()+" uri:"+s.getUri()); }
		}
		 */
		return subject;
	}

	/*
	@SuppressWarnings("unchecked")
	private RegistryResource getRegistryResource (HibernateTemplate db, String identifier) 
	throws ResourceNotFoundException
	{
		RegistryResource resource = null;
//		List<RegistryResource> resources = (List<RegistryResource>) db.find("from RegistryResource as r left join fetch r.ucdSet as u where r.identifier='"+identifier+"'");
		List<RegistryResource> resources = (List<RegistryResource>) db.find("from RegistryResource as r where r.identifier='"+identifier+"'");
		if (resources.size() == 1)
			resource = resources.get(0);
		else
			throw new ResourceNotFoundException("Can't find unique Resource w/ identifier:"+identifier);

		return resource;
	}
	 */

	// Check if we have the requiredUcds
	private boolean isOkToAddResource (RegistryResource r, List<String> requiredUcds)
	{
		logger.debug("isOkToAddResource called");
		logger.debug("          nrof requiredUcds:"+requiredUcds.size());

		if (r == null)
			return false;

		boolean isOkToAddResource = true;
		if (requiredUcds.size() > 0)
		{
			for (String requiredUcd : requiredUcds) 
			{
				logger.debug("Check requiredUcd:"+requiredUcd);
				
				if (!resourceHasRequiredUcd(r, requiredUcd))
				{
					isOkToAddResource = false; 
					break;
				}
			}
		}
		
		logger.debug(" ** returns ok:"+isOkToAddResource);
		return isOkToAddResource;
		
	}

	private boolean resourceHasRequiredUcd (RegistryResource r, String requiredUcd)
	{
		logger.debug("resourceHasRequiredUcd "+r.getIdentifier()+" ucd:"+requiredUcd);
		
		OWLOntology onto = manager.getOntology(Constant.UCDOntURI);
		SubclassVisitor visitor = new SubclassVisitor (Collections.singleton(onto));
		
		OWLClass ucdCls = manager.getOWLDataFactory().getOWLClass(URI.create(requiredUcd));
		
		ucdCls.accept(visitor);
		Set<URI> legalUcds = visitor.getSubclasses();
		
		Iterator<Ucd> uiter = r.getUcds().iterator();
		while (uiter.hasNext())
		{
			Ucd ucd = uiter.next();
			logger.debug(" --found ucd:"+ucd.getUri().getFragment());
			if (legalUcds.contains(ucd.getUri()))
					return true;
		}
		return false;
	}

	private static class SubclassVisitor 
	extends OWLDescriptionVisitorAdapter 
	{

		private boolean processInherited = true;
		private Set<OWLClass> processedClasses;
		private Set<URI> matchingSubjects;
		private Set<OWLOntology> onts;

		public SubclassVisitor (Set<OWLOntology> onts) 
		{
			this.processedClasses = new HashSet<OWLClass>();
			this.matchingSubjects = new HashSet<URI>();
			this.onts = onts;
		}

		public final Set<URI> getSubclasses() { return this.matchingSubjects; }

		public final void visit (OWLClass desc) 
		{
			
			if(processInherited && !processedClasses.contains(desc)) 
			{

				logger.debug(" SubclassVistor visits class uri:"+desc.getURI().getFragment());
				// If we are processing inherited restrictions then
				// we recursively visit named supers.  Note that we
				// need to keep track of the classes that we have processed
				// so that we don't get caught out by cycles in the taxonomy
				processedClasses.add(desc);

				if (desc.isOWLClass())
				{
					logger.debug(" add matching uri:"+desc.getURI().getFragment());
					this.matchingSubjects.add(desc.asOWLClass().getURI());

					// Check sub-classes
					for (OWLDescription subDesc : desc.getSubClasses(onts)) 
					{
						subDesc.accept(this);
					}
					
					/*
					for(OWLOntology ont : onts) {
						logger.debug("Check onto:"+ont);
						for(OWLSubClassAxiom ax : ont.getSubClassAxiomsForRHS(desc)) {
							logger.debug("try traversal of "+ax);
							ax.getSubClass().accept(this);
						}
					}
					*/

				}

			}
		}
	}
}

class ResourceNotFoundException extends Exception
{

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
