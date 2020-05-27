package net.ivoa.roqi;

import java.io.Serializable;
import java.util.List;

import net.ivoa.roqi.model.RegistryResource;

public interface Result 
extends Serializable 
{

	public abstract List<String> getProperties();
	public abstract String getSubject();

	public abstract void setQuery(Query query);
	public abstract void setReasoner(RoqiReasoner reasoner);

	public abstract List<RegistryResource> getResults();

}