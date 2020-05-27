package net.ivoa.roqi.tool;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.ivoa.roqi.model.RegistryResource;
import net.ivoa.roqi.model.Subject;
import net.ivoa.roqi.model.Ucd;

import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * 
 */

/**
 * @author thomas
 *
 */
public class DumpRoqiDb {

	private static final Logger logger = Logger.getLogger(DumpRoqiDb.class);

	private	static ClassPathXmlApplicationContext appContext 
	= new ClassPathXmlApplicationContext(new String[] {"roqi-dumpdb-config.xml"});

	private static HibernateTemplate database;

	static 
	{
		if (database == null)
			database = (HibernateTemplate) appContext.getBean("hibernateTemplate");
	}

	private static void dumpDatabase() {

		logger.info("DUMPING database");
		int nrof_subjects_with_ucds = 0;
		
		List<Subject> subjects = (List<Subject>) database.find("select s from Subject as s ");
		for (Subject s : subjects) { 
			if (dumpSubject(s) >0)
				nrof_subjects_with_ucds++;
		}

		logger.info("Subjects total:"+subjects.size()+" Subjects w/UCD:"+nrof_subjects_with_ucds);
		logger.info("DONE DUMPING database");
	}

	private static int dumpSubject(Subject s)
	{
		int nrof_ucd = getUcds(s).size();
		
		StringBuilder str = new StringBuilder();
		str.append("label:").append(s.getLabel())
		   .append(" id:").append(s.getSubjectId())
		   .append(" #rsrc:").append(s.getResources().size())
		   .append(" #ucd:").append(nrof_ucd)
//		   .append(" uri:").append(URI.create(s.getUri()).getFragment())
		   .append(" uri:").append(s.getUri());

		logger.info(str.toString()); 
		
		return nrof_ucd;
	}

	private static Set<Ucd> getUcds (Subject s) {
		Set<Ucd> ucds = new HashSet<Ucd>();
		Iterator<RegistryResource> riter = s.getResources().iterator();
		while (riter.hasNext()) {
			ucds.addAll(riter.next().getUcds());
		}
		return ucds;
	}	

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		dumpDatabase();
	}

}
