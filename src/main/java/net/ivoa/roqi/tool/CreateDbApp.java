/**
 * 
 */
package net.ivoa.roqi.tool;

import java.net.URI;
import java.net.URL;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;

import edu.noao.nsa.util.sqlexec.SQLExec;

/** A program to create the roqi database which holds information about Roqi resources.
 * 
 * @author thomas
 *
 */
public class CreateDbApp 
extends AbstractToolApp
{

	private static final long serialVersionUID = -289918921428661082L;

	private static final Logger logger = Logger.getLogger(CreateDbApp.class);

	@Option(name="--mapfile", usage="database sql mapping file", required=false)
	private String mapFile = "roqidb.map";
	public final String getMapFile() { return mapFile; }
	public final void setMapFile(String mapFile) { this.mapFile = mapFile; }

	@Option(name="--url", usage="the url of the database to create", required=false)
	private String url = "jdbc:h2:roqidb";
	public final String getUrl() { return url; }
	public final void setUrl(String dbName) { this.url = dbName; }

	@Option(name="--user", usage="name of the database user", required=false)
	private String username = "";
	public final String getUsername() { return username; }
	public final void setUsername(String dbUsername) { this.username = dbUsername; }

	@Option(name="--password", usage="the database user password", required=false)
	private String password = "";
	public final String getPassword() { return password; }
	public final void setPassword(String dbUserpass) { this.password = dbUserpass; }

	@Option(name="--sourceDir", usage="the location of sql files referenced in map file", required=false)
	private String sourceDir = "target/classes";
	public final String getSourceDir() { return sourceDir; }
	public final void setSourceDir(String sourceDir) { this.sourceDir = sourceDir; }

	@Option(name="--driver", usage="the JDBC driverclass name to use", required=false)
	private String dbDriverClassname = "org.h2.Driver";
	public final String getDriverClassName() { return dbDriverClassname; }
	public final void setDriverClassName (String driverClassName) { this.dbDriverClassname = driverClassName; }

	@Override
	public void run() 
	throws RuntimeException
	{

		logger.info("creating roqi db using mapfile:"+mapFile);
		logger.info(" working directory:"+System.getProperty("user.dir"));

		try {
	
			SQLExec loader = new SQLExec();

			// to basic configure the loader
			loader.setDriver(dbDriverClassname);
			loader.setSrcDir(sourceDir); // use the file in the target test directory
			loader.setUserid(username);
			loader.setPassword(password);

			URI db_uri = URI.create(url);
			logger.debug("Set loader URL to:"+db_uri.toASCIIString());
			loader.setUrl(db_uri.toASCIIString());

			logger.debug("looking for mapfile :"+mapFile);
			URL fileURL = getClass().getClassLoader().getResource(mapFile);
			logger.debug("found mapfile URL:"+fileURL.toString());
			loader.addSqlDbMapURL(fileURL);

			loader.execute();

		} 
		catch (Exception e) 
		{
			logger.fatal(e.getMessage());
			throw new RuntimeException(e);
		} 

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		runProgram(new CreateDbApp(), args);

	}


}
