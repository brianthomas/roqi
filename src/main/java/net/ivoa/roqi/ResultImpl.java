/**
 * 
 */
package net.ivoa.roqi;

import java.util.List;

import net.ivoa.roqi.model.RegistryResource;

/** A bean to hold the state of the query.
 * 
 * @author thomas
 *
 */
public class ResultImpl 
implements Result
{
	
	private static final long serialVersionUID = 111840340186276957L;

//	private static final Logger logger = Logger.getLogger(Query.class);

	private RoqiReasoner reasoner;
	private Query query;

	public ResultImpl () { }
	
	/* (non-Javadoc)
	 * @see net.ivoa.roqi.Query#getProperties()
	 */
	public final List<String> getProperties() { return query.getPropertyLabels(); }
	
	/* (non-Javadoc)
	 * @see net.ivoa.roqi.Query#getSubject()
	 */
	public final String getSubject () { return query.getSubject(); }
	
	/* (non-Javadoc)
	 * @see net.ivoa.roqi.Query#setReasoner(net.ivoa.roqi.RoqiReasoner)
	 */
	public final void setReasoner(RoqiReasoner reasoner) { this.reasoner = reasoner; }
	
	/* (non-Javadoc)
	 * @see net.ivoa.roqi.Query#getResults()
	 */
	public final List<RegistryResource> getResults() 
	{
		return reasoner.findMatchingVOResources(query.getSubject(), query.getProperties());
	}

	/*
	 * (non-Javadoc)
	 * @see net.ivoa.roqi.Result#setQuery(net.ivoa.roqi.Query)
	 */
	public final void setQuery(Query query) { this.query = query; }
	
}
