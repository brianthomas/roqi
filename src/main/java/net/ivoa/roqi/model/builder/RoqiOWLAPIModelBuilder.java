package net.ivoa.roqi.model.builder;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.ivoa.roqi.Constant;
import net.ivoa.roqi.model.AvailableUCD;
import net.ivoa.roqi.model.ClassTreeNode;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.log4j.Logger;
import org.apache.myfaces.custom.tree2.TreeNode;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataPropertyExpression;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObjectSomeRestriction;
import org.semanticweb.owl.model.OWLObjectValueRestriction;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLSubClassAxiom;
import org.semanticweb.owl.util.OWLDescriptionVisitorAdapter;
import org.semanticweb.owl.util.SimpleShortFormProvider;
import org.semanticweb.owl.vocab.OWLRDFVocabulary;

/** Builder class to create TreeModels from ontological
 * (OWL) models and passed restrictions.
 *
 * @author thomas
 *
 */
public class RoqiOWLAPIModelBuilder 
implements RoqiModelBuilder
{

	private static final Logger logger = Logger.getLogger(RoqiOWLAPIModelBuilder.class);

	private SimpleShortFormProvider shortNameProvider = new SimpleShortFormProvider();
	private OWLOntologyManager manager = null;
	private static OWLOntology roqi_onto = null;
	private static OWLOntology ucd_onto = null;

	//	private static final int nodeDepthLimit = 0;

	public RoqiOWLAPIModelBuilder (OWLOntologyManager manager) { 
		this.manager = manager;
		roqi_onto = manager.getOntology(Constant.RoqiOntURI);
		ucd_onto = manager.getOntology(Constant.UCDOntURI);
	}

	/*
	 * (non-Javadoc)
	 * @see net.ivoa.roqi.model.builder.RoqiModelBuilder#buildClassTreeModel(java.net.URI)
	 */
	public ClassTreeNode buildClassTreeModel (
			URI rootClassURI
	)
	throws ModelBuilderException
	{

		ClassTreeNode rootNode = null;
		logger.debug("buildClassTreeModel called w/ uri:"+rootClassURI.toASCIIString());

		OWLClass rootClass = manager.getOWLDataFactory().getOWLClass(rootClassURI); 
		if (rootClass == null)
			throw new ModelBuilderException("Cant build tree: missing specified rootClass in passed ontology uri:"+rootClassURI);

		try {
			rootNode = createClassTreeNode (rootClass, new Vector<OWLClass>(), 0);
		} catch (Exception e) {
			throw new ModelBuilderException(e);
		}

		sortNodes (rootNode); 

		return rootNode;
	}

	//	private void sortNodes (TreeNode node, int level) 
	@SuppressWarnings("unchecked")
	private void sortNodes (TreeNode node) 
	{

		Collections.sort(node.getChildren(), new BeanComparator("name"));

		for (int i = 0; i < node.getChildCount(); i++) 
			sortNodes((TreeNode) node.getChildren().get(i));

	}

	/*
	 * (non-Javadoc)
	 * @see net.ivoa.roqi.model.builder.RoqiModelBuilder#buildAvailableUCDModel(java.net.URI)
	 */
	@SuppressWarnings("unchecked")
	public Collection<AvailableUCD> buildAvailableUCDModel (
			URI parentClassURI 
	) 
	throws ModelBuilderException
	{

		logger.debug("buildAvailableUCDModel called for ["+parentClassURI.getFragment()+"]");

		OWLClass pc = manager.getOWLDataFactory().getOWLClass(parentClassURI); 

		UCDRestrictionVisitor restrictionVisitor 
		= new UCDRestrictionVisitor(Collections.singleton(roqi_onto));

		// visit the class, and all of its sub-classes, looking for availableUCD props
		pc.accept(restrictionVisitor);
		try {
			for (OWLDescription desc : pc.getSubClasses(roqi_onto)) {
				OWLClass child = desc.asOWLClass();

				for(OWLSubClassAxiom ax : roqi_onto.getSubClassAxiomsForLHS(child)) {
					OWLDescription superCls = ax.getSuperClass();
					// Ask our superclass to accept a visit from the RestrictionVisitor - if it is an
					// existential restiction then our restriction visitor will process it - if not our
					// visitor will ignore it
					superCls.accept(restrictionVisitor);
				}
			}
		} catch (Exception e) {
			throw new ModelBuilderException(e);
		}

		// Our RestrictionVisitor has now collected all of the properties that have been restricted in existential
		// restrictions - now collect them as AvailableUCD objects
		Map<URI,AvailableUCD> ucds =  new HashMap<URI,AvailableUCD>();
		for(OWLObjectSomeRestriction desc: restrictionVisitor.getSomeRestrictedProperties()) {
			// logger.debug(" obj prop uri:"+desc.getProperty().asOWLObjectProperty().getURI().toASCIIString());
			if (desc.getProperty().asOWLObjectProperty().getURI().equals(Constant.HasAvailableUCDPropertyURI))
			{
				OWLClass target = desc.getFiller().asOWLClass();
				logger.debug("Checking target for labels uri:"+target.getURI().getFragment());
				if (!ucds.containsKey(target.getURI()))
				{
					ucds.put(target.getURI(),createAvailableUCD(target));
				}
			}
		}

		List<AvailableUCD> values = new Vector<AvailableUCD>(ucds.values());
		Collections.sort(values, new BeanComparator("label")); 
		return values;

	}
			
	/*
	 * (non-Javadoc)
	 * @see net.ivoa.roqi.model.builder.RoqiModelBuilder#buildResourcesModel(java.lang.String, java.util.List)
	 */
	public Set<String> findMatchingResources (
			URI subjectURI, List<URI> requiredUcds
	)
	throws ModelBuilderException
	{

		logger.debug("findMatchingResources called for ["+subjectURI.getFragment()+"]");

		OWLClass subjectCls = manager.getOWLDataFactory().getOWLClass(subjectURI);

		RegistryResourceRestrictionVisitor restrictionVisitor = 
			new RegistryResourceRestrictionVisitor(subjectCls, Collections.singleton(roqi_onto), requiredUcds);

		// visit the class, and all of its sub-classes
		subjectCls.accept(restrictionVisitor);

		// Our RestrictionVisitor has now collected all of the properties that have been restricted in existential
		// restrictions - now collect them as AvailableUCD objects
		Set<String> resources = new HashSet<String>();
		
		for(OWLIndividual resource : restrictionVisitor.getResources())
		{
			try {
				resources.add(getRegistryResourceIdentifier(resource));
			} catch (RegistryResourceIdentifierNotFoundException e) {
				logger.warn(e.getMessage());
			}
		}
			/*
		for(URI subjectUri: restrictionVisitor.getValueRestrictedProperties().keySet()) 
		{
			OWLObjectValueRestriction desc = restrictionVisitor.getValueRestrictedProperties().get(subjectUri); 
			//logger.info(" obj prop uri:"+desc.getProperty().asOWLObjectProperty().getURI().toASCIIString());
			if (desc.getProperty().asOWLObjectProperty().getURI().equals(Constant.HasRegistryResourcePropertyURI))
			{
				//			    logger.debug(" adding resource for subject:"+subjectUri.getFragment());
				OWLIndividual target = desc.getValue();
				try {
					resources.put(getRegistryResourceIdentifier(target), subjectUri);
				} catch (RegistryResourceIdentifierNotFoundException e) {
					logger.warn(e.getMessage());
				}

			}
		}
			*/

		return resources;
	}

	private String getRegistryResourceIdentifier ( OWLIndividual i)
	throws RegistryResourceIdentifierNotFoundException
	{

		//		logger.info("Create registryResource for i:"+i.getURI().getFragment());

		String identifier = "";
		Map<OWLDataPropertyExpression,Set<OWLConstant>> dprops = i.getDataPropertyValues(roqi_onto);
		for (OWLDataPropertyExpression dpe : dprops.keySet()) { 
			URI propUri = dpe.asOWLDataProperty().getURI();
			if (propUri.equals(Constant.identifierPropUri))
			{
				identifier = getDataTypePropertyValue(dprops.get(dpe));
				break;
			}
		}
		if (identifier.equals("")) 
			throw new RegistryResourceIdentifierNotFoundException("Cant find identifier for registryResource individual uri:"+i.getURI());

		return identifier;
	}

	private static String getDataTypePropertyValue (Set<OWLConstant> vals) {
		Iterator<OWLConstant> it =  vals.iterator();
		StringBuffer value = new StringBuffer("");
		while (it.hasNext()) {
			OWLConstant obj = it.next();
			value.append(obj.getLiteral());
		}
		return value.toString();
	}

	@SuppressWarnings("unchecked")
	private AvailableUCD createAvailableUCD (OWLClass n)
	{

		logger.debug("createAvailUCD w/ node:"+n.getURI().getFragment());
		AvailableUCD ucd = null;
		// find the label
		logger.debug(" *** target has anno axioms size:"+n.getAnnotationAxioms(ucd_onto).size());
		logger.debug(" *** target has annotions size:"+n.getAnnotations(ucd_onto).size());
		String ucd_id = "";
		//		ToStringRenderer renderer = ToStringRenderer.getInstance();
		for (OWLAnnotation a : n.getAnnotations(ucd_onto,OWLRDFVocabulary.RDFS_LABEL.getURI())) {
			//			logger.debug(" Found annotation uri:"+a.getAnnotationURI().toASCIIString()); 
			//			logger.debug(" Found annotation value:"+a.getAnnotationValue()); 
			String label = a.getAnnotationValueAsConstant().getLiteral();
			// strip off RDF datatype decl 
			//			label = label.substring(0, label.indexOf("^^string"));
			//			label = label.substring(1,label.length()-1);
			ucd_id += label;
		}
		ucd = new AvailableUCD (ucd_id, n.getURI().toASCIIString());

		// parse comments for description
		String desc = "";
		for (OWLAnnotation a : n.getAnnotations(ucd_onto,OWLRDFVocabulary.RDFS_COMMENT.getURI())) {
			//			logger.debug(" Found annotation:"+a.getAnnotationURI().toASCIIString()); 
			//			logger.debug(" Found annotation value:"+a.getAnnotationValue()); 
			String comment = a.getAnnotationValue().toString();
			// strip off RDF datatype decl 
			comment = comment.substring(0, comment.indexOf("^^string"));
			comment = comment.substring(1,comment.length()-1);
			// skip over the formatting comments
			if (!comment.matches("^\\w:.+$"))
			{
				desc += comment;
			}
		}
		ucd.setDescription(desc); 

		return ucd;
	}

	/**
	 * Build a tree node recursively start with an OntClass cls.
	 * 
	 * @param cls - ont class from which the tree node starts
	 * @param isOperationClass true if an ont operation
	 * @param superClasses - a list of OntClass object that already encounted to prevent infinite loop
	 * @param depth - level of recursive loop
	 * @return DefaultMutableTreeNode
	 */
	private ClassTreeNode createClassTreeNode 
	(
			OWLClass cls,
			List<OWLClass> superClasses, 
			int depth
	) 
	throws OWLReasonerException 
	{

		logger.debug("createClassTreeNode for cls:"+cls.getURI().toASCIIString());

		// create a node for this ont lass
		ClassTreeNode root = new ClassTreeNode ( shortNameProvider.getShortForm(cls) , depth);

		if (root != null) // && depth <= this.nodeDepthLimit)
		{
			if ( !superClasses.contains(cls)) 
			{

				//Set<Set<OWLClass>> subClsSets = reasoner.getDescendantClasses(cls);
				//Set<OWLClass> subClses = OWLReasonerAdapter.flattenSetOfSets(subClsSets);
				//for (OWLClass sub: subClses) {
				logger.debug("Create with sub-classes from onto:"+roqi_onto);
				for (OWLDescription desc: cls.getSubClasses(Collections.singleton(roqi_onto))) 
				{
					
					OWLClass sub = desc.asOWLClass();
					logger.debug("Got description :"+desc.toString());

					// ignore owl:Nothing
					if (!sub.isOWLNothing() && !sub.isAnonymous()) 
					{
						// we push this class on the occurs list before we recurse
						if (!superClasses.contains(cls))
							superClasses.add(cls);

						ClassTreeNode node = createClassTreeNode(sub, superClasses, depth + 1);
						if (node != null) {
							root.getChildren().add(node);
						}
					}

				}
			}
		}

		return root;
	}

	private static class UCDRestrictionVisitor 
	extends OWLDescriptionVisitorAdapter 
	{

		private boolean processInherited = false;
		private Set<OWLClass> processedClasses;
		private Set<OWLObjectSomeRestriction> restrictedSomeProperties;
		private Set<OWLOntology> onts;

		public UCDRestrictionVisitor(Set<OWLOntology> onts) {
			restrictedSomeProperties = new HashSet<OWLObjectSomeRestriction>();
			processedClasses = new HashSet<OWLClass>();
			this.onts = onts;
		}

		/*
		public void setProcessInherited(boolean processInherited) {
			this.processInherited = processInherited;
		}	
		 */

		public Set<OWLObjectSomeRestriction> getSomeRestrictedProperties() {
			return restrictedSomeProperties;
		}

		public void visit(OWLClass desc) {
			if(processInherited && !processedClasses.contains(desc)) {
				logger.debug(" UCDRestrictVisitor visit class uri:"+desc.getURI().getFragment());
				// If we are processing inherited restrictions then
				// we recursively visit named supers.  Note that we
				// need to keep track of the classes that we have processed
				// so that we don't get caught out by cycles in the taxonomy
				processedClasses.add(desc);
				for(OWLOntology ont : onts) {
					for(OWLSubClassAxiom ax : ont.getSubClassAxiomsForLHS(desc)) {
						ax.getSuperClass().accept(this);
					}
				}
			}
		}

		/*
		public void reset() {
			processedClasses.clear();
			restrictedSomeProperties.clear();
		}	
		 */

		public void visit (OWLObjectSomeRestriction desc) {
			logger.debug(" hasSome desc: "+desc.toString());
			//			logger.debug(" desc: "+desc.toString());
			//			logger.debug(" filler:"+desc.getFiller().asOWLClass().getURI().toASCIIString());
			// This method gets called when a description (OWLDescription) is an
			// existential (someValuesFrom) restriction and it asks us to visit it
			// TODO : place a check here to make sure its a "hasAvailableUCD" property
			restrictedSomeProperties.add(desc);
		}
	}
	
	private static class RegistryResourceRestrictionVisitor 
	extends OWLDescriptionVisitorAdapter 
	{
		/*
		private static class UcdResourceSomeRestrictionVisitor 
		extends OWLDescriptionVisitorAdapter 
		{
			private boolean processInherited = true;
			private Set<OWLClass> processedClasses = new HashSet<OWLClass>();
			private Set<OWLClass> someValueProperties = new HashSet<OWLClass>();
			private Set<OWLOntology> onts;
			private List<URI> requiredUcds;
			private OWLClass parentClass;

			public UcdResourceSomeRestrictionVisitor (OWLClass baseCls, Set<OWLOntology> onts, List<URI> requiredUcds) 
			{
				this.onts = onts;
				this.requiredUcds = requiredUcds;
				this.reset(baseCls);
			}

			public Set<OWLClass> getSomeValueProperties() { return someValueProperties; }

			public void reset (OWLClass baseCls) 
			{
				processedClasses.clear();
				someValueProperties.clear();
				this.parentClass = baseCls;
			}	

			public void visit (OWLClass desc) {


				if(processInherited && !processedClasses.contains(desc) 
						&& !desc.getURI().equals(Constant.subjectURI)
				) {

				//	logger.debug("UCD visit called for class:"+desc.toString());

					// If we are processing inherited restrictions then
					// we recursively visit named supers.  Note that we
					// need to keep track of the classes that we have processed
					// so that we don't get caught out by cycles in the taxonomy
					processedClasses.add(desc);
					//					currentClass = desc;

					if (desc.isOWLClass() 
							//							&& (desc.asOWLClass().getSuperClasses(onts).contains(parentClass)) 
							|| desc.asOWLClass().equals(parentClass))
					{
						logger.debug(" UCDVisitor visits class uri:"+desc.getURI().getFragment());
						
						for (OWLDescription subDesc : desc.getSubClasses(onts)) 
						{
							subDesc.accept(this);
						}
						
						// to pick up resources..
						for(OWLOntology ont : onts) 
						{
							for(OWLSubClassAxiom ax : ont.getSubClassAxiomsForLHS(desc)) 
							{
							//	logger.debug(" UCDVisitor visits superclass uri:"+ax.getSuperClass());
								ax.getSuperClass().accept(this);
							}
						}
					}
					else
					{
						//logger.debug("Skipping class which is not sub-class: "+desc);
					}

				}
			}

			@Override
			public void visit (OWLObjectValueRestriction desc) {
				logger.error(" MISSING handler for OWLObjectValueRestriction desc: "+desc.toString());
			}

			@Override
			public void visit(OWLDataAllRestriction desc) {
				logger.error(" MISSING handler for DataAllRestriction desc: "+desc.toString());
			}

			@Override
			public void visit(OWLDataSomeRestriction desc) {
				logger.error(" MISSING Handler for DataSomeRestriction desc: "+desc.toString());
			}

			@Override
			public void visit(OWLDataValueRestriction desc) {
				logger.error(" MISSING Handler for DataValueRestriction desc: "+desc.toString());
			}

			@Override
			public void visit(OWLObjectAllRestriction desc) {
				logger.error(" MISSING handler for ObjectAllRestriction desc: "+desc.toString());
			}
			
			// This method gets called when a description (OWLDescription) is an
			// existential (someValuesFrom) restriction and it asks us to visit it
			public void visit (OWLObjectSomeRestriction desc) {
				logger.debug(" hasSome desc: "+desc.toString());
				//				logger.debug(" filler:"+desc.getFiller().asOWLClass().getURI().toASCIIString());
				someValueProperties.add(desc.getFiller().asOWLClass());
				//			logger.debug(" desc: "+desc.toString());
			}

		}
*/
		
		private boolean processInherited = true;
		private Set<OWLClass> processedClasses;
		private List<OWLObjectValueRestriction> restrictedValueProperties;
		private List<OWLIndividual> resources;
		private Set<OWLClass> someValueProperties;
		private Set<OWLOntology> onts;
		private OWLClass parentClass;
//		private OWLClass currentClass;
		private List<URI> requiredUcds;
//		private	UcdResourceSomeRestrictionVisitor ucdVisitor;

		public RegistryResourceRestrictionVisitor (OWLClass baseCls, Set<OWLOntology> onts, List<URI> requiredUcds) 
		{
			this.parentClass = baseCls;
			this.restrictedValueProperties = new Vector<OWLObjectValueRestriction>();
			this.someValueProperties = new HashSet<OWLClass>();
			this.processedClasses = new HashSet<OWLClass>();
			this.resources = new Vector<OWLIndividual>();
			this.requiredUcds = requiredUcds;
			this.onts = onts;
//			this.ucdVisitor =  new UcdResourceSomeRestrictionVisitor(baseCls, Collections.singleton(roqi_onto), requiredUcds);

		}

		//		public void setProcessInherited (boolean processInherited) { this.processInherited = processInherited; }	

		/*
		public List<OWLObjectValueRestriction> getValueRestrictedProperties() {
			return restrictedValueProperties;
		}
		*/
		public List<OWLIndividual> getResources() { return this.resources; }

		public Set<OWLClass> getSomeValueProperties() {
			return someValueProperties;
		}

		public void visit (OWLClass desc) {
			if(processInherited && !processedClasses.contains(desc) 
				// && !desc.getURI().equals(Constant.subjectURI)
			) {

				// If we are processing inherited restrictions then
				// we recursively visit named supers.  Note that we
				// need to keep track of the classes that we have processed
				// so that we don't get caught out by cycles in the taxonomy
				processedClasses.add(desc);
//				currentClass = desc;

				if (desc.isOWLClass() 
						&& (desc.asOWLClass().getSuperClasses(onts).contains(parentClass)) 
						|| desc.asOWLClass().equals(parentClass))
				{
					logger.debug(" RegResistrict visit class uri:"+desc.getURI().getFragment());

					// This will pick up the resources..
					for(OWLOntology ont : onts) 
					{
						for(OWLSubClassAxiom ax : ont.getSubClassAxiomsForLHS(desc)) 
						{
							// logger.debug("Subclass Axiom check for class:"+desc);
							ax.getSuperClass().accept(this);
						}
					}
					
					// Check sub-classes for resources
					for (OWLDescription subDesc : desc.getSubClasses(onts)) 
					{
						this.someValueProperties.clear(); // clear for next class (this is NOT correct..)
						subDesc.accept(this);
					}
					
					// TODO : remove check on required UCDS, in the future, we will do that outside the reasoner..
					if (hasRequiredUCDs(desc)) { 
						logger.debug("CLASS HAS REQUIRED UCDS");
						for(OWLObjectValueRestriction vr: this.restrictedValueProperties) {
							if (vr.getProperty().asOWLObjectProperty().getURI().equals(Constant.HasRegistryResourcePropertyURI))
							{
								this.resources.add(vr.getValue());
							}
						}
						// clear for next pass
						this.restrictedValueProperties.clear();
					}
					
				}
				else
				{
					//	logger.debug("Skipping class which is not sub-class: "+desc);
				}

			}
		}

		/*
		public void reset() {
			processedClasses.clear();
			restrictedValueProperties.clear();
	//		restrictedSomeValueProperties.clear();
		}	
		 */

		private boolean hasRequiredUCDs (OWLClass checkClass) {

			if (requiredUcds.size() > 0)
			{
				
				// check that we meet all of the criteria
				logger.debug("Found "+this.getSomeValueProperties().size()
						+" ucd properties on class:"+checkClass.getURI().getFragment());
				
				for (URI requiredUCD : this.requiredUcds)
				{
					boolean hasUCD = false;
					for( OWLClass testUCD : this.getSomeValueProperties()) 
					{
						if (requiredUCD.equals(testUCD.getURI())
								||
							 checkClassSupersForUCD(requiredUCD, testUCD) 
						) {
							hasUCD = true;
							break;
						}
					}
					
					if (!hasUCD) { return false; }
				}
				
				/*
				// check that we meet all of the criteria
				logger.debug("Found "+ucdVisitor.getSomeValueProperties().size()
						+" ucd properties on class:"+checkClass.getURI().getFragment());

				for (URI requiredUCD : this.requiredUcds)
				{
					logger.debug("Check requiredUCD: "+requiredUCD);
					boolean hasUCD = false;
					for( OWLClass testUCD : ucdVisitor.getSomeValueProperties()) 
					{
						if (requiredUCD.equals(testUCD.getURI())
								||
							 checkClassSupersForUCD(requiredUCD, testUCD) 
						) {
							hasUCD = true;
							break;
						}
					}
					if (!hasUCD)
						return false;
				}
				*/
			}

			return true;
		}

		private boolean checkClassSupersForUCD (URI requiredUCD, OWLClass testUCD) {
//			logger.debug("CheckClass Supers for UCD:"+requiredUCD.getFragment());
			for (OWLDescription supCls : testUCD.getSuperClasses(Collections.singleton(ucd_onto))) 
			{
				if (requiredUCD.equals(supCls.asOWLClass().getURI()))
					return true;
				else if (checkClassSupersForUCD(requiredUCD, supCls.asOWLClass()))
					return true;
			}
			return false;
		}
						
		public void visit (OWLObjectValueRestriction desc) {
			logger.debug(" hasValue desc: "+desc.toString());
			restrictedValueProperties.add(desc);
		}

		/*
		@Override
		public void visit(OWLDataAllRestriction desc) {
			logger.error(" MISSING handler for DataAllRestriction desc: "+desc.toString());
		}

		@Override
		public void visit(OWLDataSomeRestriction desc) {
			logger.error(" MISSING Handler for DataSomeRestriction desc: "+desc.toString());
		}

		@Override
		public void visit(OWLDataValueRestriction desc) {
			logger.error(" MISSING Handler for DataValueRestriction desc: "+desc.toString());
		}

		@Override
		public void visit(OWLObjectAllRestriction desc) {
			logger.error(" MISSING handler for ObjectAllRestriction desc: "+desc.toString());
		}
		*/

		// This method gets called when a description (OWLDescription) is an
		// existential (someValuesFrom) restriction and it asks us to visit it
		public void visit (OWLObjectSomeRestriction desc) {
			logger.debug(" hasSomeRestiction desc: "+desc.toString());
			// TODO : place a check here to make sure its a "hasAvailableUCD" property
			someValueProperties.add(desc.getFiller().asOWLClass());
			//			logger.debug(" desc: "+desc.toString());
			//			logger.debug(" filler:"+desc.getFiller().asOWLClass().getURI().toASCIIString());
		}

	}

}
