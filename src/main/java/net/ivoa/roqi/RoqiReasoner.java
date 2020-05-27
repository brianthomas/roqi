package net.ivoa.roqi;

import java.util.Collection;
import java.util.List;

import net.ivoa.roqi.model.AvailableUCD;
import net.ivoa.roqi.model.ClassTreeNode;
import net.ivoa.roqi.model.RegistryResource;

public interface RoqiReasoner {
	
	public List<RegistryResource> findMatchingVOResources ( String subjectName,  List<String> requiredUCDs );

	public ClassTreeNode findAllSubjects ();
	
	public Collection<AvailableUCD> findAvailableUCDs (String subjectName);
	
}
