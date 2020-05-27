package net.ivoa.roqi.model.builder;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.ivoa.roqi.model.AvailableUCD;
import net.ivoa.roqi.model.ClassTreeNode;

public interface RoqiModelBuilder 
{

	/**
	 * Build a tree node recursively start with an cls w/ indicated URI.
	 * 
	 * @param cls - ont class from which the tree node starts
	 * @param isOperationClass true if an ont operation
	 * @param superClasses - a list of OntClass object that already encounted to prevent infinite loop
	 * @param depth - level of recursive loop
	 * @return DefaultMutableTreeNode
	 */
	public ClassTreeNode buildClassTreeModel ( URI rootClassURI )
	throws ModelBuilderException;
	
	/**
	 * 
	 * @param parentClassURI
	 * @return
	 * @throws ModelBuilderException
	 */
	public Collection<AvailableUCD> buildAvailableUCDModel ( URI parentClassURI ) 
	throws ModelBuilderException;
	
	/**
	 * 
	 * @param subjectName
	 * @param requiredUcds
	 * @return
	 * @throws ModelBuilderException
	 */
	public Set<String> findMatchingResources ( URI subjectURI, List<URI> requiredUcds)
	throws ModelBuilderException;
	
	/**
	 * 
	 * @param subjectUri
	 * @return
	 * @throws ModelBuilderException
	 */
//	public Set<URI> findMatchingSubjects ( URI subjectUri )  throws ModelBuilderException;
	
}