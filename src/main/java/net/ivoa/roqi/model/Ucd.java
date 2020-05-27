/**
 * 
 */
package net.ivoa.roqi.model;

import java.io.Serializable;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author thomas
 *
 */
@Entity
@Table(name="ucd")
@Inheritance(strategy=InheritanceType.JOINED)
public class Ucd 
implements Serializable
{
	
	private static final long serialVersionUID = -6026031085298943814L;
	
	@Id
	@GeneratedValue
	@Column(name="ucd_id")
	private Integer ucdId;
	public final Integer getUcdId() { return ucdId; }
	public final void setUcdId (Integer ucdId) { this.ucdId = ucdId; }
	
	@Column
	private String uri;
	public final URI getUri() { return URI.create(uri); }
	public final void setUri (String uri) { this.uri = uri; }
	
	@Transient
	public final String getFragment() { return getUri().getFragment(); }
	
	@Column
	private String name;
	public final String getName() { return name; }
	public final void setName(String name) { this.name = name; }
	
	@Column
	private String description;
	public final String getDescription() { return description; }
	public final void setDescription(String description) { this.description = description; }
	


	@ManyToMany(
            cascade = CascadeType.ALL,
            mappedBy = "ucdSet",
            targetEntity = RegistryResource.class
    )
    private Set<RegistryResource> resources = new HashSet<RegistryResource>();
	public void setResources(Set<RegistryResource> resources) { this.resources = resources; }
	public Set<RegistryResource> getResources() { return resources; }
	
}
