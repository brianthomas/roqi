/**
 * 
 */
package net.ivoa.roqi.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author thomas
 *
 */
@Entity
@Table(name="interface")
public class Interface 
implements Serializable 
{

	private static final long serialVersionUID = -3749199592626757402L;
	
	@Id
	@GeneratedValue
	@Column(name="interface_id") 
	private Integer interfaceId;
	public final Integer getInterfaceId() { return interfaceId; }
	public final void setInterfaceId(Integer id) { this.interfaceId = id; }
	
	@Column(name="query_type") 
	private String queryType;
	public final String getQueryType() { return queryType; }
	public final void setQueryType(String queryType) { this.queryType = queryType; }
	
	@Column(name="result_type") 
	private String resultType;
	public final String getResultType() { return resultType; }
	public final void setResultType(String resultType) { this.resultType = resultType; }
	
	@Column
	private String role;
	public final String getRole() { return role; }
	public final void setRole(String role) { this.role = role; }
	
	@Column
	private String xsitype;
	public final String getType() { return xsitype; }
	public final void setType(String type) { this.xsitype = type; }
	
	@Column
	private String use;
	public final String getUse() { return use; }
	public final void setUse(String use) { this.use = use; }
	
	@Column
	private String url;
	public final String getUrl() { return url; }
	public final void setUrl(String url) { this.url = url; }
	
}
