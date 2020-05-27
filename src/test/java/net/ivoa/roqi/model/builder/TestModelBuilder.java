package net.ivoa.roqi.model.builder;

import junit.framework.TestCase;
import net.ivoa.roqi.Constant;
import net.ivoa.roqi.RoqiOWLAPIReasoner;
import net.ivoa.roqi.model.ClassTreeNode;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestModelBuilder extends TestCase 
{
	
	private static final Logger logger = Logger.getLogger(TestModelBuilder.class);

	private static RoqiModelBuilder builder;
//	private static OWLOntologyManager manager;
 	
	private	ClassPathXmlApplicationContext appContext 
		= new ClassPathXmlApplicationContext(new String[] {"test-reasoner-config.xml"});

	@Override
	protected void setUp() 
	throws Exception 
	{
		if (builder == null)
		{
			RoqiOWLAPIReasoner rea = (RoqiOWLAPIReasoner) appContext.getBean("reasoner");
			builder = rea.getBuilder();
			/*
			manager = RoqiOWLOntologyManagerBuilder.getInstance().getRoqiOntologyManager();
			builder = new RoqiOWLAPIModelBuilder(manager);
			*/
		}
	}

	public void test1() 
	throws ModelBuilderException
	{
		logger.info("Build class tree ");
		
		logger.info("builder"+builder);
		
		ClassTreeNode root = builder.buildClassTreeModel(Constant.subjectURI); 
//		ClassTreeNode root = builder.buildClassTreeModel(URI.create("http://archive.astro.umd.edu/owl/roqi.owl#abundances--ages--galaxies")); 
		assertNotNull(root);
		
		assertNotNull(root.getName());
		
		logger.debug("Got root node name:"+root.getName()+" type:"+root.getType());
		assertTrue("root subject node has children",root.getChildCount() > 0);
		
		printClassTreeNode(root,"");
		
	}

	private void printClassTreeNode (ClassTreeNode node, String msg) {
		for (ClassTreeNode n : node.getChildren()) { 
			logger.debug(msg+n.getName());
			printClassTreeNode(n,"  "+msg);
		}
	}
	
}
