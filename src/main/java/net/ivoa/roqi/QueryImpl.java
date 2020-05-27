/**
 * 
 */
package net.ivoa.roqi;

import java.util.List;
import java.util.Vector;

/** A bean to hold the state of the query.
 * 
 * @author thomas
 *
 */
public class QueryImpl 
implements Query 
{
	
//	private static final Logger logger = Logger.getLogger(Query.class);

	private static final long serialVersionUID = 8427215549126100317L;
	
	private String subject = "";
	private List<String> properties = new Vector<String>();
	private List<String> propLabels = new Vector<String>();

	public QueryImpl() {}
	
	/* (non-Javadoc)
	 * @see net.ivoa.roqi.Query#getProperties()
	 */
	public final List<String> getProperties() { return properties; }

	public final List<String> getPropertyLabels() { return propLabels; }
	
	/* (non-Javadoc)
	 * @see net.ivoa.roqi.Query#setProperties(java.util.List)
	 */
	public final void addProperty(String label, String prop) { 
		this.propLabels.add(label);
		this.properties.add(prop); 
	}

	/* (non-Javadoc)
	 * @see net.ivoa.roqi.Query#getSubject()
	 */
	public final String getSubject () { return subject; }
	
	/* (non-Javadoc)
	 * @see net.ivoa.roqi.Query#setSubject(java.lang.String)
	 */
	public final void setSubject (String name) { subject = name; }
	
	public void clear() {
		this.subject = "";
		this.properties.clear();
		this.propLabels.clear();
	}
	
}
