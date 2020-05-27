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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author thomas
 *
 */
@Entity
@Table(name="subject")
@Inheritance(strategy=InheritanceType.JOINED)
public class Subject 
implements Serializable
{

	private static final long serialVersionUID = 2419313182435178312L;
	
	@Id
	@GeneratedValue
	@Column(name="subject_id") 
	private Integer subjectId;
	public final Integer getSubjectId() { return subjectId; }
	public final void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
	
	@Column
	private String label;
	public final String getLabel() { return label; }
	public final void setLabel(String label) { this.label = label; }
	
	@Column
	private String uri;
	public final String getUri() { return uri; }
	public final void setUri(String uri) { this.uri = uri; }

	/*
	@Column
	private String description;
	public final String getDescription() { return description; }
	public final void setDescription(String description) { this.description = description; }
	*/
	
	@OneToMany (
			fetch = FetchType.EAGER,
			cascade = CascadeType.ALL 
	)
	@JoinTable(
			name="subject_resource_assoc",
			joinColumns=@JoinColumn( name="subject_id" ),
			inverseJoinColumns=@JoinColumn( name="resource_id" )
	)
	private Set<RegistryResource> resourceSet = new HashSet<RegistryResource>();
	protected final Set<RegistryResource> getResourceSet() { return this.resourceSet; }
	public final void setResourceSet(Set<RegistryResource> resources) { this.resourceSet = resources; }
	public final void addResource (RegistryResource r) { this.resourceSet.add(r); }
	
	@Transient
	public final Set<RegistryResource> getResources() { return this.getResourceSet(); }
	
	
}
