/**
 * 
 */
package net.ivoa.roqi.model;

import java.util.List;

import org.apache.myfaces.custom.tree2.TreeNode;
import org.apache.myfaces.custom.tree2.TreeNodeBase;

/**
 * @author thomas
 *
 */
public class ClassTreeNode 
extends TreeNodeBase 
implements TreeNode
{

	private static final long serialVersionUID = 6031390303817687361L;
	
	private String name;
    private String description;
    private boolean expanded = true;
    private int level = 0;
    private boolean rendered = true;
    
    public ClassTreeNode (String name, int level) {
    	this.name = name;
    	setType("ClassTreeNode");
    	setLevel(level);
    }
    
	public final String getName() { return name; }
	public final void setName(String name) { this.name = name; }
	
	public final String getDescription() { return description; }
	public final void setDescription(String description) { this.description = description; }
	
	public final boolean isRendered() { return rendered; }
	public final void setRendered(boolean rendered) { this.rendered = rendered; }
	
	public final int getLevel() { return level; }
	public final void setLevel(int level) { this.level = level; }
	
	public final boolean isExpanded() { return expanded; }
	public final void setExpanded(boolean expanded) { this.expanded = expanded; }
	
	public final void setChildren (List<ClassTreeNode> children) 
    {
        int count = getChildCount();
        getChildren().addAll(children);
        while (count-- > 0) getChildren().remove(0);
    }
	
	@SuppressWarnings("unchecked")
	@Override
	public List<ClassTreeNode> getChildren() {
		return (List<ClassTreeNode>) super.getChildren();
	}
	
    @Override
    public final String getType() {
        return super.getType();
    }

	@Override
	public String toString() {
		return this.getName();
	}
	
	public void filter(String filterStr) {
		
		if (filterStr == null)
		{
			this.rendered = true;
		}
		else if (this.getName().contains(filterStr))
		{
			this.rendered = true;
			// TODO: logic here is not good
			this.expanded = true; // be sure to expand it 
 		} else { 
			this.rendered = false;
		}
		
		for (ClassTreeNode childNode : this.getChildren())
		{
			// TODO: if the parent is rendered, and child matches, render then parent children
			childNode.filter(filterStr);
		}
		
	}

}
