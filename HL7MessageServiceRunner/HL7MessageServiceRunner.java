/*
 *  $Id: HL7MessageServiceRunner.java 27 2009-11-30 05:32:18Z scott $
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7MessageServiceRunner.java : A standalone HL7 message service, using the
 *                                 us.conxio.HL7 facility.
 *
 *  Copyright (C) 2009  Scott Herman
 *
 *  This is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This code is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this code.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package us.conxio.HL7MessageServiceRunner;

import java.io.*;
import java.net.URI;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

import us.conxio.HL7.HL7MessageService.*;


/**
 * A standalone HL7 message service, using the us.conxio.HL7 facility.
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7MessageServiceRunner {
   static Logger logger = Logger.getLogger("us.conxio.HL7");

   public HL7MessageServiceRunner() { }

    /**
     * The main executable, essentially a runnable wrapper for the HL7MessageService class.
     * @param args the command line arguments,
     * <ul>which specify the HL7MessageService specification XML data source.
     * <li> -f specifies a file.
     * <li> -u specifies a URI (Universal Resource Identifier)..
     */
    public static void main(String[] args) {
      URI               svcURI = null;
      HL7MessageService msgSvc = null;
      HL7MessageServiceRunner svcRunner = new HL7MessageServiceRunner();
      
      BasicConfigurator.configure();

      for (int argIndex = 0; argIndex < args.length; ++argIndex) {
         if (args[argIndex].startsWith("-")) {
            switch (args[argIndex].charAt(1)) {
               case 'f' :
                  String fileName = args[++argIndex];
                  if (fileName != null) {
                     svcURI = new File(fileName).toURI();
                  } // if
                  break;

               case 'u' :
                  String uriArgStr = args[++argIndex];
                  if (uriArgStr != null) {
                     try {
                        svcURI = new URI(uriArgStr);
                     } catch (java.net.URISyntaxException uriEx) {
                        HL7MessageServiceRunner.logger.info( "URISyntaxException:"
                                                        +    uriEx.toString()
                                                        +    ". on ["
                                                        +    uriArgStr
                                                        +    "].");
                     } // try - catch
                  } // if
                  break;
                  
               default: 
                  HL7MessageServiceRunner.logger.info("Unexpected option:" + args[argIndex]);
            } // switch
         } // if
      } // for

      if (svcURI == null) {
         HL7MessageServiceRunner.logger.info("No service specified.");
      } else {
         try {         
            msgSvc = new HL7MessageService(svcURI) ;
            msgSvc.dump();
            msgSvc.open(); // * This runs the service.
         } catch (IOException ioEx) {
            HL7MessageServiceRunner.logger.info("IOException:" + ioEx.toString() );
         } catch (Exception ex) {
            HL7MessageServiceRunner.logger.info("Exception:" + ex.toString() );
         } finally {
            msgSvc.close();
         } // try - catch - finally
      } // if - else
    } // main

} // HL7MessageServiceRunner


/*
 * Revision History
 *
 * Id: HL7MessageServiceRunner.java 14 2009-11-20 22:03:44Z scott
 * Origination. Tested and verified.
 *
 * $Id: HL7MessageServiceRunner.java 27 2009-11-30 05:32:18Z scott $
 * Added appropriate licensing and annotation.
 */
