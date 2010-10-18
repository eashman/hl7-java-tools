/*
 *  $Id$
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

import java.io.File;
import java.io.IOException;

import java.net.URI;

import org.apache.log4j.Logger;

import us.conxio.hl7.hl7system.HL7Logger;




/**
 * A standalone HL7 message service, using the us.conxio.HL7 facility.
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7MessageServiceRunner {
   static Logger logger = HL7Logger.getHL7Logger();

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
                        logger.error("URISyntaxException:"
                                +    uriEx.toString()
                                +    ". on ["
                                +    uriArgStr
                                +    "].", uriEx);
                     } // try - catch
                  } // if
                  break;
                  
               default: 
                  logger.info("Unexpected option:" + args[argIndex]);
            } // switch
         } // if
      } // for

      if (svcURI == null) {
         logger.error("No service specified.");
      } else {
         try {         
            msgSvc = new HL7MessageService(svcURI) ;
            msgSvc.dump();
            msgSvc.runService(); // * This runs the service.
         } catch (IOException ioEx) {
            logger.error("IOException:", ioEx);
         } catch (Exception ex) {
            logger.error("Exception:", ex);
         } finally {
            if (msgSvc != null) msgSvc.close();
         } // try - catch - finally
      } // if - else
    } // main

} // HL7MessageServiceRunner

