/*
 *  $Id$
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

package us.conxio.hl7MessageAgent;

import java.io.File;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

import us.conxio.hl7.hl7message.HL7Message;
import us.conxio.hl7.hl7service.HL7Route;
import us.conxio.hl7.hl7stream.HL7IOException;
import us.conxio.hl7.hl7stream.HL7Stream;
import us.conxio.hl7.hl7stream.HL7StreamURI;
import us.conxio.hl7.hl7system.HL7Logger;


/**
 * A standalone HL7 message agent, using the us.conxio.HL7 facility.
 * @author scott herman <scott.herman@unconxio.us>
 */
public class HL7MessageAgent {
   static Logger logger = HL7Logger.getHL7Logger();

   private static void routeHL7(HL7Route hl7Route) {
      if (hl7Route == null) return;

      hl7Route.dump();
      try {
         hl7Route.open();
      } catch (HL7IOException ioEx) {
         logger.error("HL7Route:" +  hl7Route.getID(), ioEx);
      } // try - catch
      
      hl7Route.run();
   } // routeHL7


   private static void adHocTransfer(URI sourceURI, URI deliveryURI) {
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
         logger.error("HL7MessageAgent: caught HL7IOException on Ad hoc delivery from:"
                     +  sourceURI.toString()
                     +  ", to: "
                     +  deliveryURI.toString(), ioEx);
      } finally {
         try {  // close the reader
            if (msgReader != null) msgReader.close();
         } catch (HL7IOException ioEx) {
            logger.error(  "HL7MessageAgent: caught HL7IOException on closure of reader:"
                        +  sourceURI.toString(), ioEx);
         } // try - catch


         try {  // close the writer
            if (msgWriter != null) msgWriter.close();
         } catch (HL7IOException ioEx) {
            logger.info(  "HL7MessageAgent: caught HL7IOException on closure of writer:"
                        +  deliveryURI.toString(), ioEx);
         } // try - catch
      } // try - catch
   } // adHocTransfer

   
   /**
    * A stand alone HL7 message transfer agent.
    * @param args<uL>
    * <li> <b>-f</b>  Specifies a file containing one or more HL7 transaction messages to
    * be transferred.
    * <li> <b>-s</b>  Specifies a source URI from which one or more HL7 transaction messages
    * are to be read and transferred.
    * <li> <b>-d</b>  Specifies a destination URI to which one or more HL7 transaction messages
    * are to be transferred.
    * <li> <b>-r</b>
    * <li> <b>-t</b> Specifies a route or transform configuration URI from which source,
    * destination, and transform specification information can be read.
    * </ul>
    *
    */
   public static void main(String[] args) {
      URI      sourceURI = null,
               deliveryURI = null;
      HL7Route hl7Route = null;

      

      // * Process command line.
      for (int argIndex = 0; argIndex < args.length; ++argIndex) {
         if (args[argIndex].startsWith("-")) {
            switch (args[argIndex].charAt(1)) {
               case 'f' : // * native file source
                  if (args[++argIndex] != null) sourceURI = new File(args[argIndex]).toURI();
                  break;

               case 's' : // * source URI
                  if (args[++argIndex] != null) try {
                     sourceURI = new URI(args[argIndex]);
                  } catch (URISyntaxException ex) {
                     logger.error("Bad Source URI:" + args[argIndex], ex);
                  } // if - try - catch
                  break;

               case 'd' : // * destination / delivery URI
                  if (args[++argIndex] != null) try {
                     deliveryURI = new URI(args[argIndex]);
                  } catch (URISyntaxException ex) {
                     logger.error("Bad Delivery URI:" + args[argIndex], ex);
                  } // if - try - catch
                  break;

               case 'r' : // * route specification(s) URI
               case 't' : // * transform specification(s) URI
                  if (args[++argIndex] != null) try {
                     hl7Route = new HL7Route(new URI(args[argIndex]));
                  } catch (URISyntaxException ex) {
                     logger.error("Bad Route URI:" + args[argIndex], ex);
                  } // if - try - catch
                  break;

               default :
                  logger.info("HL7MessageAgent: unexpected argument:[" + args[argIndex] + "].");
            } // switch
         } // if
      } // for

      if (hl7Route != null) {  // * Route specified.
         routeHL7(hl7Route);
         return;
      } // if

      if (sourceURI == null) logger.error("HL7MessageAgent: No source specified.");
      if (deliveryURI == null) logger.error("HL7MessageAgent: No delivery specified.");
      if (sourceURI == null || deliveryURI == null) return;

      adHocTransfer(sourceURI, deliveryURI);
   } // main

} // HL7MessageAgent


/*
 * $Id$
 * $HeadURL: $
 *
 * A standalone HL7 message transfer agent.
 *
 * Revision History
 * -------- -------
 * Id: 22
 * Origination. Tested and verified.
 *
 * HL7MessageAgent.java 26 2009-11-30 05:31:17Z scott
 * Added proper licensing and annotation.
 *
 * HL7MessageAgent.java 40 2009-12-11 00:41:49Z scott
 * Modified for compatability with the us.conxio.HL7Stream package.
 *
 * Id: HL7MessageAgent.java 23 2010-03-14 02:46:06Z scott.herman@unconxio.us
 * Cleaning up HL7Stream closure, and associated exception handling.
 *
 * Revision: 92, Date: 2010-10-18 16:21:25 -0700 (Mon, 18 Oct 2010), Author: scott.herman@unconxio.us
 * Upgraded to hl7-2.0 (r.90), restructured slightly, and documented.
 *
 * $Revision$, $Date$, $Author$
 * Renamed, to avoid collision, and corrected package declaration.
 *
 */

