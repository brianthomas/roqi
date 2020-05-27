package net.ivoa.roqi;

import java.io.Serializable;
import java.util.List;

public interface Query 
extends Serializable 
{

	public abstract String getSubject();
	public abstract void setSubject(String name);
	
	public abstract List<String> getProperties();
	public abstract void addProperty(String label, String prop);
	public abstract List<String> getPropertyLabels();
	
	public abstract void clear();
	
}