package net.ivoa.roqi;

import java.net.URI;
import java.net.URISyntaxException;

public class Constant 
{
	
	// default ontos
	public static URI defaultRoqiPhysicalURI = null;
	public static URI defaultRegistryPhysicalURI = null;
	public static URI defaultUCDPhysicalURI = null;
	
	public static final String roqiOntoName = "roqi.owl";
	public static final String registryOntoName = "registryResource.owl";
	public static final String ucdOntoName = "UCD.owl";
	
    static {
    	try {
		 	defaultRegistryPhysicalURI = Constant.class.getClassLoader().getResource(registryOntoName).toURI();
		 	defaultUCDPhysicalURI = Constant.class.getClassLoader().getResource(ucdOntoName).toURI();
		 	defaultRoqiPhysicalURI = Constant.class.getClassLoader().getResource(roqiOntoName).toURI();
    	} catch (URISyntaxException e) {
    		System.err.println(e.getMessage());
    	}
    }
	
	public static final String xsi_uri = "http://www.w3.org/2001/XMLSchema-instance#";
    
	public static final URI RoqiOntURI = URI.create("http://archive.astro.umd.edu/owl/roqi.owl");
	public static final URI RegistryOntURI = URI.create("http://www.ivoa.net/owl/registryResource.owl");
	public static final URI UCDOntURI = URI.create("http://www.ivoa.net/Document/WD/vocabularies/20080222/UCD");
	
	public static final URI rootUCDURI = URI.create(UCDOntURI.toASCIIString()+"#UCD");
	public static final URI subjectURI = URI.create(RoqiOntURI.toASCIIString()+"#Subject");
	
	public static final URI HasAvailableUCDPropertyURI = URI.create(RoqiOntURI.toASCIIString()+"#hasAvailableUcd");
	public static final URI HasRegistryResourcePropertyURI = URI.create(RoqiOntURI.toASCIIString()+"#hasResource");
	
	// some registry properties
	public static final URI identifierPropUri = URI.create(RegistryOntURI.toASCIIString()+"#identifier"); 
	public static final URI rightsPropUri = URI.create(RegistryOntURI.toASCIIString()+"#rights"); 
	public static final URI shortNamePropUri = URI.create(RegistryOntURI.toASCIIString()+"#shortName"); 
	public static final URI titlePropUri = URI.create(RegistryOntURI.toASCIIString()+"#title"); 
	
	public static final URI hasAccessUrlPropUri = URI.create(RegistryOntURI.toASCIIString()+"#hasAccessURL"); 
	public static final URI hasUCDPropUri = URI.create(RoqiOntURI.toASCIIString()+"#hasUCD"); 
	public static final URI hasCapabilityPropUri = URI.create(RegistryOntURI.toASCIIString()+"#hasCapability"); 
	public static final URI hasCoveragePropUri = URI.create(RegistryOntURI.toASCIIString()+"#hasCoverage"); 
	public static final URI hasInterfacePropUri = URI.create(RegistryOntURI.toASCIIString()+"#hasInterface"); 
	public static final URI hasValidationLevelPropUri = URI.create(RegistryOntURI.toASCIIString()+"#hasValidationLevel"); 
	
	// accessUrl
	public static final URI urlPropUri = 
		URI.create(RegistryOntURI.toASCIIString()+"#url"); 
	public static final URI usePropUri = 
		URI.create(RegistryOntURI.toASCIIString()+"#use"); 
	
	// capability 
	public static final URI standardIdPropUri = 
		URI.create(RegistryOntURI.toASCIIString()+"#standardId"); 
	public static final URI xsitypePropUri = URI.create(xsi_uri+"type"); 
	
	//coverage
	public static final URI footprintPropUri = 
		URI.create(RegistryOntURI.toASCIIString()+"#footprint"); 
	public static final URI wavebandPropUri = 
		URI.create(RegistryOntURI.toASCIIString()+"#waveband"); 
	
	// interface 
	public static final URI queryTypePropUri = URI.create(RegistryOntURI.toASCIIString()+"#queryType"); 
	public static final URI resultTypePropUri = URI.create(RegistryOntURI.toASCIIString()+"#resultType"); 
	public static final URI rolePropUri = URI.create(RegistryOntURI.toASCIIString()+"#role"); 
	
	// validationLevel
	public static final URI validatedByPropUri 
	= URI.create(RegistryOntURI.toASCIIString()+"#validatedBy"); 
	public static final URI valuePropUri =  URI.create(RegistryOntURI.toASCIIString()+"#value"); 
	
	private Constant() { }
	
}
