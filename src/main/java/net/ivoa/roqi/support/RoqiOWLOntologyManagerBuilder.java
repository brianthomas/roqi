/**
 * 
 */
package net.ivoa.roqi.support;

import java.net.URI;

import net.ivoa.roqi.Constant;

import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.util.SimpleURIMapper;

/**
 * @author thomas
 *
 */
public class RoqiOWLOntologyManagerBuilder 
{

	private static RoqiOWLOntologyManagerBuilder instance;
	 
	private RoqiOWLOntologyManagerBuilder ()  { }
	
	public static RoqiOWLOntologyManagerBuilder getInstance() {
		if (instance == null)
			instance = new RoqiOWLOntologyManagerBuilder();
		return instance;
	}
	
	public OWLOntologyManager getRoqiOntologyManager (
			URI roqiUIPhysicalURI, 
			URI registryPhysicalURI, 
			URI ucdPhysicalURI
	) 
	throws OWLOntologyCreationException
	{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		manager.addURIMapper(new SimpleURIMapper(Constant.RoqiOntURI, roqiUIPhysicalURI));
	 	manager.addURIMapper(new SimpleURIMapper(Constant.RegistryOntURI, registryPhysicalURI));
	 	manager.addURIMapper(new SimpleURIMapper(Constant.UCDOntURI, ucdPhysicalURI));
	 		
	 	manager.loadOntology(Constant.RoqiOntURI);
		
		return manager;
	}

	public OWLOntologyManager getRoqiOntologyManager () 
	throws OWLOntologyCreationException
	{
		return getRoqiOntologyManager(Constant.defaultRoqiPhysicalURI, Constant.defaultRegistryPhysicalURI, Constant.defaultUCDPhysicalURI);
	}
	
}
