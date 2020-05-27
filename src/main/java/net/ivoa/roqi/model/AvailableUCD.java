/**
 * 
 */
package net.ivoa.roqi.model;

import java.io.Serializable;

/**
 * @author thomas
 *
 */
public class AvailableUCD 
implements Serializable
{

	private static final long serialVersionUID = 7412720948908646609L;

	private String label;
	private String uri;
	private String description = "";
	
	public AvailableUCD (String label, String uri) {
		if (label == null) 
			throw new NullPointerException ("Can't construct with null label");
		this.label = label;
		this.uri = uri;
	}
	
	public final String getLabel() { return label; }
//	public final void setLabel(String label) { this.label = label; }

	public final String getDescription() { return description; }
	public final void setDescription(String description) { this.description = description; }
	
	public final String getUri() { return uri; }

	@Override
	public String toString() {
		return label+" ["+uri+"] ["+description+"]";
	}

	
}
