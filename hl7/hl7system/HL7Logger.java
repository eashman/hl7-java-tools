/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7system;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;



/**
 *
 * @author scott
 */
public class HL7Logger  {
   public static final String       HL7_DEFAULT_ROOT_LOGGER    = "us.conxio.hl7";
   public static final String       PROPERTY_LOG4J_CONFIG      = "log4j.configuration";

   private static Logger            logger = Logger.getLogger(HL7_DEFAULT_ROOT_LOGGER);
   private static HL7Properties     hl7Properties = null;
   
   private static boolean initialized = false;

   private HL7Logger() {  }


   public static Logger getHL7Logger() {
      if (!initialized) {
         initializeLogger();
         initialized = true;
      } // if

      return logger;
   } // getLogger

   private static void initializeLogger() {
      String logConfig = System.getProperty(PROPERTY_LOG4J_CONFIG);
      if (StringUtils.isEmpty(logConfig)) logConfig = System.getenv(PROPERTY_LOG4J_CONFIG);

      if (hl7Properties == null) hl7Properties = new HL7Properties();
      if (StringUtils.isEmpty(logConfig)) logConfig = HL7Logger.hl7Properties.getProperty(PROPERTY_LOG4J_CONFIG);


      if (StringUtils.isEmpty(logConfig)) {
         BasicConfigurator.configure();
         HL7Logger.logger.error("log4j.configuration not found. used BasicConfigurator.");
         return;
      } // if

      if (!logConfig.toLowerCase().endsWith("xml") ) {
         BasicConfigurator.configure();
         HL7Logger.logger.info("log4j.configuration (" + logConfig + ") used by BasicConfigurator.");
         return;
      } // if

      DOMConfigurator.configure(logConfig);
      HL7Logger.logger.info("log4j.configuration (" + logConfig + ") used by DOMConfigurator.");
   } // initializeLogger


} // HL7Logger
