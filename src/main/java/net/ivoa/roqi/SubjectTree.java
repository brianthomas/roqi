package net.ivoa.roqi;
import java.io.Serializable;

import javax.faces.event.ActionEvent;

import net.ivoa.roqi.model.ClassTreeNode;

import org.apache.log4j.Logger;
import org.apache.myfaces.custom.tree2.Tree;

/**
 * @author thomas
 *
 */
public class SubjectTree 
implements Serializable 
{

	private static final long serialVersionUID = -3854757001946690915L;

	private static final Logger logger = Logger.getLogger(SubjectTree.class);
	
	private ClassTreeNode subjects;

	// field will be set by the webapp
	private RoqiReasoner reasoner;
	private transient Tree tree;
	private String filter;
	
	public ClassTreeNode getSubjects() {
		return subjects;
	}
	
	public Tree getTree() { return tree; }
	public void setTree(Tree tree) { this.tree = tree; }
	
	/**
	 * 
	 * @return
	 */
	public final RoqiReasoner getReasoner() { return reasoner; }
	
	/**
	 * 
	 * @param reasoner
	 */
	public final void setReasoner(RoqiReasoner reasoner) { 
		logger.error(" ST got Reasoner:"+reasoner);
		this.reasoner = reasoner; 
		subjects = reasoner.findAllSubjects();
	}
	
	public final String getFilter() { return filter; }
	public final void setFilter(String filter) { this.filter = filter; }
	
	// just a dummy because facelets tries to read this 
	// in tomahawktree.xhtml/t:updateActionListener
	//	public String getSortfieldDirection() { return null; }

	// actionListener for the tree, for filtering results
	public void listenFilter (ActionEvent e) { subjects.filter(filter); }
	public void listenExpand (ActionEvent e) {  tree.expandAll(); }
	public void listenCollapse (ActionEvent e) {  tree.collapseAll(); }
	
}
