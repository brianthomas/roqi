/**
 * 
 */
package net.ivoa.roqi.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * @author thomas
 *
 */
@Entity
@Table(name="capability")
@Inheritance(strategy=InheritanceType.JOINED)
public class Capability 
implements Serializable 
{

	private static final long serialVersionUID = 8918897256124148289L;

	@Id
	@GeneratedValue
	@Column(name="capability_id") 
	private Integer capabilityId;
	public final Integer getCapabilityId() { return capabilityId; }
	public final void setCapabilityId(Integer id) { this.capabilityId = id; }
	
	@OneToOne ( cascade = CascadeType.ALL )
    @JoinColumn(name="interface_id_fk")
	private Interface iface;
	public final Interface getInterface() { return iface; }
	public final void setInterface (Interface iface) { this.iface = iface; }
	
	@Column(name="standard_id") 
	private String standardId;
	public final String getStandardId() { return standardId; }
	public final void setStandardId(String standardId) { this.standardId = standardId; }
	
	@Column
	private String xsitype;
	public final String getXsiType() { return xsitype; }
	public final void setXsiType(String type) { this.xsitype = type; }
	
	public final String getName() {
		if (xsitype == null)
			return "Unknown Name";
		if (xsitype.equals("")) {
			return getInterface().getType();
		}
		return xsitype;
	}
	
	public final boolean getIsAvailable() 
	{
		if(getName().equals("")) 
			return false;
		return true;
	}
	
}
