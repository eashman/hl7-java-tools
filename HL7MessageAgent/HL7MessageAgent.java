/*
 *  $Id: HL7MessageAgent.java 40 2009-12-11 00:41:49Z scott $
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7MessageAgent.java : A standalone HL7 message agent, using the
 *                         us.conxio.HL7 facility.
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

package us.conxio.HL7MessageAgent;

import java.io.*;
import java.net.URI;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

import us.conxio.HL7.HL7Message.*;
import us.conxio.HL7.HL7MessageService.*;
import us.conxio.HL7.HL7Stream.*;


/**
 * A standalone HL7 message agent, using the us.conxio.HL7 facility.
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7MessageAgent {
   static Logger logger = Logger.getLogger("us.conxio.HL7");

   public static void main(String[] args) {
      URI      sourceURI = null,
               deliveryURI = null;
      HL7Route hl7Route = null;

      BasicConfigurator.configure();

      // * Process command line.
      try {
         for (int argIndex = 0; argIndex < args.length; ++argIndex) {
            if (args[argIndex].startsWith("-")) {
               switch (args[argIndex].charAt(1)) {
                  case 'f' : // * native file source
                     if (args[++argIndex] != null) {
                        sourceURI = new File(args[argIndex]).toURI();
                     } // if
                     break;

                  case 's' : // * source URI
                     if (args[++argIndex] != null) {
                        sourceURI = new URI(args[argIndex]);
                     } // if
                     break;

                  case 'd' : // * destination / delivery URI
                      if (args[++argIndex] != null) {
                        deliveryURI = new URI(args[argIndex]);
                     } // if
                     break;

                  case 'r' : // * route specification(s) URI
                  case 't' : // * transform specification(s) URI
                      if (args[++argIndex] != null) {
                        hl7Route = new HL7Route(new URI(args[argIndex]) );
                     } // if
                     break;

                  default :
                     HL7MessageAgent.logger.info("HL7MessageAgent: unexpected argument:[" + args[argIndex] + "].");
               } // switch
            } // if
         } // for
      } catch (java.net.URISyntaxException uEx) {
         HL7MessageAgent.logger.info("HL7MessageAgent: caught URISyntaxException: " + uEx.getMessage());
      } catch (Exception ex) {
         HL7MessageAgent.logger.info("HL7MessageAgent: caught Exception: " + ex.getMessage() );
      } // try - catch

      // * Route specified.
      if (hl7Route != null) {
         hl7Route.dump();
         try {
            hl7Route.open();
         } catch (HL7IOException ioEx) {
            HL7MessageAgent.logger.info(  "HL7MessageAgent: caught HL7IOException on HL7Route:"
                                       +  hl7Route.getID()
                                       +  ": "
                                       +  ioEx.getMessage() );
         } // try - catch
         hl7Route.run();
      } else if (sourceURI == null) {
         HL7MessageAgent.logger.info(  "HL7MessageAgent: No source specified.");
      } else if (deliveryURI == null) {
         HL7MessageAgent.logger.info(  "HL7MessageAgent: No delivery specified.");
      } else { // * Ad hoc delivery
         HL7Stream msgReader = null;
         HL7Stream msgWriter = null;
         try {
            msgReader = new HL7StreamURI(sourceURI).getHL7StreamReader();
            msgWriter = new HL7StreamURI(deliveryURI).getHL7StreamWriter();
            HL7Message hl7Msg;

            while ( (hl7Msg = msgReader.read() ) != null) {
               hl7Msg.fresh();
               msgWriter.write(hl7Msg);
            } // while
         } catch (HL7IOException ioEx) {
            HL7MessageAgent.logger.info(  "HL7MessageAgent: caught HL7IOException on Ad hoc delivery from:"
                                       +  sourceURI.toString()
                                       +  ", to: "
                                       +  deliveryURI.toString()
                                       +  ": "
                                       +  ioEx.toString());
         } finally {
            // close the reader
            try {
               msgReader.close();
            } catch (HL7IOException ioEx) {
               HL7MessageAgent.logger.info(  "HL7MessageAgent: caught HL7IOException on closure of reader:"
                                          +  sourceURI.toString()
                                          +  ": "
                                          +  ioEx.toString());
            } // try - catch

            // close the writer
            try {
               msgWriter.close();
            } catch (HL7IOException ioEx) {
               HL7MessageAgent.logger.info(  "HL7MessageAgent: caught HL7IOException on closure of writer:"
                                          +  deliveryURI.toString()
                                          +  ": "
                                          +  ioEx.toString());
            } // try - catch
         } // try - catch
      } // if, else if,.. else
   } // main
} // HL7MessageAgent


/*
 * Revision History
 *
 * Id: 22
 * Origination. Tested and verified.
 *
 * HL7MessageAgent.java 26 2009-11-30 05:31:17Z scott
 * Added proper licensing and annotation.
 *
 * HL7MessageAgent.java 40 2009-12-11 00:41:49Z scott
 * Modified for compatability with the us.conxio.HL7Stream package.
 *
 * $Id: HL7MessageAgent.java 40 2009-12-11 00:41:49Z scott $
 * Cleaning up HL7Stream closure, and associated exception handling.

 */

