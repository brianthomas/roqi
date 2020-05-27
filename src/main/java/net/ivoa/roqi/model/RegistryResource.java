/**
 * 
 */
package net.ivoa.roqi.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author thomas
 *
 */
@Entity
@Table(name="registry_resource")
@Inheritance(strategy=InheritanceType.JOINED)
public class RegistryResource 
implements Serializable
{

	private static final long serialVersionUID = 2643813847403369336L;

	@Transient
	private String subject;
	@Transient
	public final String getSubjectname() { return subject; }
	@Transient
	public final void setSubjectname(String subject) { this.subject = subject; }

	@Id
	@GeneratedValue
	@Column(name="resource_id") 
	private Integer resourceId;
	public final Integer getResourceId() { return resourceId; }
	public final void setResourceId(Integer resourceId) { this.resourceId = resourceId; }

	@Column
	private String description; 
	public final String getDescription() { return description; }
	public final void setDescription(String description) { this.description = description; }

	@Column
	private String rights;
	public final String getRights() { return rights; }
	public final void setRights(String rights) { this.rights = rights; }

	@Column
	private String title;
	public final String getTitle() { return title; }
	public final void setTitle(String title) { this.title = title; }

	@Column(name="short_name") 
	private String shortName;
	public final String getShortName() { return shortName; }
	public final void setShortName(String shortName) { this.shortName = shortName; }

	@Column
	private String identifier;
	public final String getIdentifier() { return identifier; }
	public final void setIdentifier(String identifier) { this.identifier = identifier; }

	@Column
	private String footprint;
	public final String getFootprint() { return footprint; }
	public final void setFootprint(String footprint) { this.footprint = footprint; }

	@Column
	private String waveband;
	public final String getWaveband() { return waveband; }
	public final void setWaveband(String waveband) { this.waveband = waveband; }

	@Column(name="validation_value")
	private String validationValue;
	public final String getValidationLvlValue() { return validationValue; }
	public final void setValidationLvlValue(String v) { this.validationValue = v; }

	@Column(name="validation_by")
	private String validatedBy;
	public final String getValidatedBy() { return validatedBy; }
	public final void setValidatedBy(String validatedBy) { this.validatedBy = validatedBy; }

	@ManyToMany (
			fetch = FetchType.EAGER,
	        targetEntity=net.ivoa.roqi.model.Ucd.class,
			cascade = CascadeType.ALL 
	    )
	@JoinTable(
			name="resource_ucd_assoc",
			joinColumns=@JoinColumn( name="resource_id" ),
			inverseJoinColumns=@JoinColumn( name="ucd_id" )
	)
	private Set<Ucd> ucdSet = new HashSet<Ucd>();
	protected final Set<Ucd> getUcdSet() { return this.ucdSet; }
	public final void setUcdSet(Set<Ucd> ucds) { this.ucdSet = ucds; }
	public final void addUcd (Ucd ucd) { this.ucdSet.add(ucd); }
	
	@Transient
	public final Set<Ucd> getUcds() { return this.getUcdSet(); }
	
	@OneToMany(
			fetch = FetchType.EAGER,
			cascade = CascadeType.ALL 
	)
	@JoinTable(
			name="resource_capability_assoc",
			joinColumns=@JoinColumn( name="resource_id" ),
			inverseJoinColumns=@JoinColumn( name="capability_id" )
	)
	private Set<Capability> capabilities = new HashSet<Capability>();
	public final Set<Capability> getCapabilities() { return capabilities; }
	/*
	private List<Capability> capabilities = new Vector<Capability>();
	public final List<Capability> getCapabilities() { return capabilities; }
	*/
	public final void addCapability(Capability capability) { this.capabilities.add(capability); }



}
