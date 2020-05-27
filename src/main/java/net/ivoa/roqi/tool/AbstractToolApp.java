/**
 * 
 */
package net.ivoa.roqi.tool;

import java.io.Serializable;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * @author thomas
 *
 */
abstract public class AbstractToolApp 
implements Runnable, Serializable 
{

	private static final long serialVersionUID = 5748409898042067799L;

   /** quick n dirty utility method.
    *
    * @param main
    * @param args
    */
   protected static void runProgram (Runnable main, String[] args) {

           CmdLineParser parser = new CmdLineParser(main);
           try {
                   parser.parseArgument(args);
                   main.run();
           } catch (CmdLineException e) {
                   System.err.println(e.getMessage());
                   System.err.println("Available command line flags:");
                   parser.printUsage(System.err);
           }
   }

}
