/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import us.conxio.hl7.hl7message.HL7Encoding;
import us.conxio.hl7.hl7message.HL7Time;

/**
 *
 * @author scott
 */
public class HL7Properties extends Properties {
   private String       propSource = null;

   private static Logger logger = HL7Logger.getHL7Logger();

   public HL7Properties() {
      initializeHL7Properties();
   } // HL7Properties


   private void initializeHL7Properties()  {
      File sourcePath = findSourcePath();
      if (sourcePath != null) try {
         loadFromSource(sourcePath);
      } catch (FileNotFoundException ex) {
         if (logger != null) logger.error(null, ex);
      } catch (IOException ex) {
         if (logger != null) logger.error(null, ex);
      } // try - catch

      if (loadRemainderFromSystem() || loadDefaultHL7Properties()) {
         if (propSource == null) {
            propSource = PROP_SRC_LOCAL_XML;
            saveTo(propSource);
         } // if
      } // if
   } // initializeHL7Properties


   private void loadFromSource(File sourcePath) throws FileNotFoundException, IOException {
      propSource = sourcePath.getAbsolutePath();
      FileInputStream inStream = new FileInputStream(sourcePath);
      if (propSource.toLowerCase().endsWith("xml")) {
         loadFromXML(inStream);
      } else {
         load(inStream);
      } // if - else
   } // loadFromSource


   // In order of precedence
   public static final String PROP_SRC_LOCAL_XML         = "./hl7.properties.xml";
   public static final String PROP_SRC_LOCAL             = "./hl7.properties";
   public static final String PROP_SRC_LOCAL_HIDDEN_XML  = "./.hl7.properties.xml";
   public static final String PROP_SRC_LOCAL_HIDDEN      = "./.hl7.properties";
   public static final String PROP_SRC_HL7_SYS_XML       = "/etc/hl7/hl7.properties.xml";
   public static final String PROP_SRC_HL7_SYS           = "/etc/hl7/hl7.properties";
   public static final String PROP_SRC_SYS_XML           = "/etc/hl7.properties.xml";
   public static final String PROP_SRC_SYS               = "/etc/hl7.properties";

   private File findSourcePath() {
      File fProps = null;
      fProps = findSourcePathFromProperties();
      if (fProps != null) return fProps;

      fProps = searchSourceFilePaths();
      if (fProps != null) return fProps;

      return null;

   } // findSourcePath
   

   public static final String PROPERTY_HL7_PROPERTIES = "hl7.properties";

   private File findSourcePathFromProperties() {
      File fProps = null;
      String propSrc = System.getenv(PROPERTY_HL7_PROPERTIES);
      if (StringUtils.isEmpty(propSrc)) propSrc = System.getProperty(PROPERTY_HL7_PROPERTIES);
      if (StringUtils.isNotEmpty(propSrc)) {
         fProps = new File(propSrc);
         if (fProps.exists()) return fProps;
      } // if

      return null;
   } // findSourcePathFromProperties


   private File searchSourceFilePaths() {
      File fProps = null;
      String[] protocol = {   PROP_SRC_LOCAL_XML,
                              PROP_SRC_LOCAL,
                              PROP_SRC_LOCAL_HIDDEN_XML,
                              PROP_SRC_LOCAL_HIDDEN,
                              PROP_SRC_HL7_SYS_XML,
                              PROP_SRC_HL7_SYS,
                              PROP_SRC_SYS_XML,
                              PROP_SRC_SYS };
      List<String>   searchList = Arrays.asList(protocol);

      for (String srcName : searchList) {
         fProps = new File(srcName);
         if (fProps.exists()) return fProps;
      } // for

      return null;
   } // searchSourceFilePaths


   public void saveTo(String fileName) {
      FileOutputStream out = null;
      try {
         out = new FileOutputStream(new File(fileName));
         storeToXML(out, "Modified by:"
                              +  getClass().getCanonicalName()
                              +  "("
                              +  HL7Time.get()
                              +  ")");
      } catch (FileNotFoundException ex) {
         logger.error(null, ex);
      } catch (IOException ex) {
         logger.error(null, ex);
      } // try - catch
   } // saveTo


   public static final String HL7_PROPERTY_FIELD_SEP           = "encoding.fieldseparator";
   public static final String HL7_PROPERTY_COMPONENT_SEP       = "encoding.componentseparator";
   public static final String HL7_PROPERTY_REPETITION_SEP      = "encoding.repetitionseparator";
   public static final String HL7_PROPERTY_ESCAPE_IND          = "encoding.escapeindicator";
   public static final String HL7_PROPERTY_SUBCOMPONENT_SEP    = "encoding.subcomponentseparator";
   public static final String HL7_PROPERTY_ENCODING            = "encoding";

   public static final String HL7_PROPERTY_LOCAL_FACILITY      = "local.facility";
   public static final String HL7_PROPERTY_LOCAL_APPLICATION   = "local.application";

   public static final String HL7_PROPERTY_REMOTE_FACILITY     = "remote.facility";
   public static final String HL7_PROPERTY_REMOTE_APPLICATION  = "remote.application";

   public static final String HL7_PROPERTY_VERSION             = "version";
   public static final String HL7_PROPERTY_PROCESSING_CODE     = "processcode";

   private static final String HL7_PROPERTY_PREFIX             = "hl7";
   private static final String HL7_PROPERTY_DELIMITER          = ".";
   private static final String HL7_PROPERTY_DEFAULT_INDICATION = "default";

   private static final String VALUE_DEFAULT_LOCAL_FACILITY    = "local.facility.not.set";
   private static final String VALUE_DEFAULT_LOCAL_APPLICATION = "local.application.not.set";

   private static final String VALUE_DEFAULT_REMOTE_FACILITY    = "remote.facility.not.set";
   private static final String VALUE_DEFAULT_REMOTE_APPLICATION = "remote.application.not.set";

   private static final String VALUE_DEFAULT_VERSION           = "2.3";
   private static final String VALUE_DEFAULT_PROCESSING_CODE   = "D";


   private boolean loadRemainderFromSystem() {
      loadEncodingFromSystem();
      loadPropertyFromSystem(HL7_PROPERTY_LOCAL_FACILITY);
      loadPropertyFromSystem(HL7_PROPERTY_LOCAL_APPLICATION);
      loadPropertyFromSystem(HL7_PROPERTY_REMOTE_FACILITY);
      loadPropertyFromSystem(HL7_PROPERTY_REMOTE_APPLICATION);
      loadPropertyFromSystem(HL7_PROPERTY_VERSION);
      loadPropertyFromSystem(HL7_PROPERTY_PROCESSING_CODE);

      return hasHL7Property(HL7_PROPERTY_LOCAL_FACILITY)
          && hasDefaultHL7Property(HL7_PROPERTY_LOCAL_FACILITY)
          && hasHL7Property(HL7_PROPERTY_LOCAL_APPLICATION)
          && hasDefaultHL7Property(HL7_PROPERTY_LOCAL_APPLICATION)
          && hasHL7Property(HL7_PROPERTY_REMOTE_FACILITY)
          && hasDefaultHL7Property(HL7_PROPERTY_REMOTE_FACILITY)
          && hasHL7Property(HL7_PROPERTY_REMOTE_APPLICATION)
          && hasDefaultHL7Property(HL7_PROPERTY_REMOTE_APPLICATION)
          && hasHL7Property(HL7_PROPERTY_VERSION)
          && hasDefaultHL7Property(HL7_PROPERTY_VERSION)
          && hasHL7Property(HL7_PROPERTY_PROCESSING_CODE)
          && hasDefaultHL7Property(HL7_PROPERTY_PROCESSING_CODE)
          && hasHL7Property(HL7_PROPERTY_ENCODING)
          && hasDefaultHL7Property(HL7_PROPERTY_ENCODING);
   } // getFromSystem


   private boolean loadDefaultHL7Properties() {
      HL7Encoding defaultEncoding = HL7Encoding.getDefaultEncoding();
      setEncoding(defaultEncoding);
      setEncodeChars(defaultEncoding);
      if (!hasDefaultEncodeChars()) setDefaultEncodeChars(getEncodeChars());

      // load hardcoded default properties.
      loadDefaultHL7Property(HL7_PROPERTY_LOCAL_FACILITY, VALUE_DEFAULT_LOCAL_FACILITY);
      loadDefaultHL7Property(HL7_PROPERTY_LOCAL_APPLICATION, VALUE_DEFAULT_LOCAL_APPLICATION);
      loadDefaultHL7Property(HL7_PROPERTY_REMOTE_FACILITY, VALUE_DEFAULT_REMOTE_FACILITY);
      loadDefaultHL7Property(HL7_PROPERTY_REMOTE_APPLICATION, VALUE_DEFAULT_REMOTE_APPLICATION);
      loadDefaultHL7Property(HL7_PROPERTY_VERSION, VALUE_DEFAULT_VERSION);
      loadDefaultHL7Property(HL7_PROPERTY_PROCESSING_CODE, VALUE_DEFAULT_PROCESSING_CODE);

      return true; // does not fail.
   } // loadDefaultHL7Properties
   

   private boolean loadEncodingFromSystem() {
      if (hasEncoding()
      &&  hasDefaultEncoding()
      &&  hasEncodeChars()
      &&  hasDefaultEncodeChars())
         return true;

      if (!hasEncoding() && !hasEncodeChars()) {
         String propStr = getPropertyFromSystem(HL7_PROPERTY_ENCODING);
         if (StringUtils.isNotEmpty(propStr)) setEncodeChars(new HL7Encoding(propStr));
      } // if


      if (hasEncoding() && !hasEncodeChars()) setEncodeChars(getHL7Encoding());
      if (hasEncodeChars() && !hasDefaultEncodeChars()) setDefaultEncodeChars(getEncodeChars());
      if (hasDefaultEncodeChars() && !hasEncodeChars() ) setEncodeChars(getDefaultEncodeChars());

      return hasDefaultEncodeChars() && hasEncodeChars();
   } // loadEncodingFromSystem


   private String buildPropName(String preFix, String content) {
      return new StringBuilder(preFix)
                  .append(HL7_PROPERTY_DELIMITER)
                  .append(content)
                  .toString();
   } // buildPropName


   private String buildPropName(String preFix, String indicator, String content) {
      return new StringBuilder(preFix)
                  .append(HL7_PROPERTY_DELIMITER)
                  .append(indicator)
                  .append(HL7_PROPERTY_DELIMITER)
                  .append(content)
                  .toString();
   } // buildPropName


   private String getFromSystem(String propName) {
      String propStr = System.getenv(propName);
      if (StringUtils.isEmpty(propStr)) propStr = System.getProperty(propName);
      return propStr;
   } // getFromSystem


   private String getPropertyFromSystem(String propName) {
      String propStr = getFromSystem(buildPropName(HL7_PROPERTY_PREFIX, propName));
      if (StringUtils.isEmpty(propStr)) {
         propStr = getFromSystem(buildPropName(HL7_PROPERTY_PREFIX,
                                                HL7_PROPERTY_DEFAULT_INDICATION,
                                                propName));
      } // if

      return propStr;
   } // getPropertyFromSystem


   private boolean hasEncodeChars() {
      return hasFieldSeparator()
          && hasRepetitionSeparator()
          && hasComponentSeparator()
          && hasSubComponentSeparator()
          && hasEscapeIndicator();
   } // hasEncodeChars


   private boolean hasDefaultEncodeChars() {
      return hasDefaultFieldSeparator()
          && hasDefaultRepetitionSeparator()
          && hasDefaultComponentSeparator()
          && hasDefaultSubComponentSeparator()
          && hasDefaultEscapeIndicator();
   } // hasDefaultEncodeChars


   private void setEncodeChars(HL7Encoding encoders) {
      setProperty(buildHL7PropName(HL7_PROPERTY_FIELD_SEP),
                  encoders.getFieldSeparator());
      setProperty(buildHL7PropName(HL7_PROPERTY_REPETITION_SEP),
                  encoders.getRepetitionSeparator());
      setProperty(buildHL7PropName(HL7_PROPERTY_COMPONENT_SEP),
                  encoders.getComponentSeparator());
      setProperty(buildHL7PropName(HL7_PROPERTY_SUBCOMPONENT_SEP),
                  encoders.getSubComponentSeparator());
      setProperty(buildHL7PropName(HL7_PROPERTY_ESCAPE_IND),
                  encoders.getEscapeChar());
      setEncoding(encoders);
   } // setEncodeChars


   private void setDefaultEncodeChars(HL7Encoding encoders) {
      setProperty(buildDefaultHL7PropName(HL7_PROPERTY_FIELD_SEP),
                  encoders.getFieldSeparator());
      setProperty(buildDefaultHL7PropName(HL7_PROPERTY_REPETITION_SEP),
                  encoders.getRepetitionSeparator());
      setProperty(buildDefaultHL7PropName(HL7_PROPERTY_COMPONENT_SEP),
                  encoders.getComponentSeparator());
      setProperty(buildDefaultHL7PropName(HL7_PROPERTY_SUBCOMPONENT_SEP),
                  encoders.getSubComponentSeparator());
      setProperty(buildDefaultHL7PropName(HL7_PROPERTY_ESCAPE_IND),
                  encoders.getEscapeChar());
      setDefaultEncoding(encoders);
   } // setDefaultEncodeChars


   private String buildHL7PropName(String propID) {
      return buildPropName(HL7_PROPERTY_PREFIX, propID);
   } // buildHL7PropName


   private String buildDefaultHL7PropName(String propID) {
      return buildPropName(HL7_PROPERTY_PREFIX, HL7_PROPERTY_DEFAULT_INDICATION, propID);
   } // buildDefaultHL7PropName


   private boolean hasFieldSeparator() {
      return StringUtils.isNotEmpty(getProperty(buildHL7PropName(HL7_PROPERTY_FIELD_SEP)));
   } // hasFieldSeparator


   private boolean hasRepetitionSeparator() {
      return StringUtils.isNotEmpty(getProperty(buildHL7PropName(HL7_PROPERTY_REPETITION_SEP)));
   } // hasRepetitionSeparator


   private boolean hasComponentSeparator() {
      return StringUtils.isNotEmpty(getProperty(buildHL7PropName(HL7_PROPERTY_COMPONENT_SEP)));
   } // hasComponentSeparator


   private boolean hasSubComponentSeparator() {
      return StringUtils.isNotEmpty(getProperty(buildHL7PropName(HL7_PROPERTY_SUBCOMPONENT_SEP)));
   } // hasSubComponentSeparator


   private boolean hasEscapeIndicator() {
      return StringUtils.isNotEmpty(getProperty(buildHL7PropName(HL7_PROPERTY_ESCAPE_IND)));
   } // hasEscapeIndicator


   private boolean hasDefaultFieldSeparator() {
      return StringUtils.isNotEmpty(getProperty(buildDefaultHL7PropName(HL7_PROPERTY_FIELD_SEP)));
   } // hasFieldSeparator


   private boolean hasDefaultRepetitionSeparator() {
      return StringUtils.isNotEmpty(getProperty(buildDefaultHL7PropName(HL7_PROPERTY_REPETITION_SEP)));
   } // hasRepetitionSeparator


   private boolean hasDefaultComponentSeparator() {
      return StringUtils.isNotEmpty(getProperty(buildDefaultHL7PropName(HL7_PROPERTY_COMPONENT_SEP)));
   } // hasComponentSeparator


   private boolean hasDefaultSubComponentSeparator() {
      return StringUtils.isNotEmpty(getProperty(buildDefaultHL7PropName(HL7_PROPERTY_SUBCOMPONENT_SEP)));
   } // hasSubComponentSeparator


   private boolean hasDefaultEscapeIndicator() {
      return StringUtils.isNotEmpty(getProperty(buildDefaultHL7PropName(HL7_PROPERTY_ESCAPE_IND)));
   } // hasEscapeIndicator


   private void setEncoding(HL7Encoding encoders) {
      setProperty(buildHL7PropName(HL7_PROPERTY_ENCODING),
                  encoders.toString());
   } // setEncoding


   private void setDefaultEncoding(HL7Encoding encoders) {
      setProperty(buildDefaultHL7PropName(HL7_PROPERTY_ENCODING),
                  encoders.toString());
   } // setDefaultEncoding


   private HL7Encoding getEncodeChars() {
      HL7Encoding retn = HL7Encoding.getDefaultEncoding();
      retn.setFieldSeparator(getProperty(buildHL7PropName(HL7_PROPERTY_FIELD_SEP)));
      retn.setRepetitionSeparator(getProperty(buildHL7PropName(HL7_PROPERTY_REPETITION_SEP)));
      retn.setComponentSeparator(getProperty(buildHL7PropName(HL7_PROPERTY_COMPONENT_SEP)));
      retn.setSubComponentSeparator(getProperty(buildHL7PropName(HL7_PROPERTY_SUBCOMPONENT_SEP)));
      retn.setEscapeChar(getProperty(buildHL7PropName(HL7_PROPERTY_ESCAPE_IND)));
      return retn;
   } // getEncodeChars


   private HL7Encoding getDefaultEncodeChars() {
      HL7Encoding retn = HL7Encoding.getDefaultEncoding();
      retn.setFieldSeparator(getProperty(buildDefaultHL7PropName(HL7_PROPERTY_FIELD_SEP)));
      retn.setRepetitionSeparator(getProperty(buildDefaultHL7PropName(HL7_PROPERTY_REPETITION_SEP)));
      retn.setComponentSeparator(getProperty(buildDefaultHL7PropName(HL7_PROPERTY_COMPONENT_SEP)));
      retn.setSubComponentSeparator(getProperty(buildDefaultHL7PropName(HL7_PROPERTY_SUBCOMPONENT_SEP)));
      retn.setEscapeChar(getProperty(buildDefaultHL7PropName(HL7_PROPERTY_ESCAPE_IND)));
      return retn;
   } // getDefaultEncodeChars


   private void loadDefaultHL7Property(String propID, String value) {
      if (!hasHL7Property(propID) && !hasDefaultHL7Property(propID)) {
         setDefaultHL7Property(propID, value);
         setHL7Property(propID, value);
      } // if
   } // loadDefaultHL7Property


   public boolean hasHL7Property(String propID) {
      return StringUtils.isNotEmpty(getProperty(buildHL7PropName(propID)));
   } // hasHL7Property


   public boolean hasDefaultHL7Property(String propID) {
      return StringUtils.isNotEmpty(getProperty(buildDefaultHL7PropName(propID)));
   } // hasDefaultHL7Property


   public void setDefaultHL7Property(String propID, String value) {
      if (StringUtils.isEmpty(propID) || StringUtils.isEmpty(value)) return;
      setProperty(buildDefaultHL7PropName(propID), value);
   } // setDefaultHL7Property


   public void setHL7Property(String propID, String value) {
      if (StringUtils.isEmpty(propID) || StringUtils.isEmpty(value)) return;
      setProperty(buildHL7PropName(propID), value);
   } // setHL7Property


   private boolean loadPropertyFromSystem(String propID) {
      String propStr = getPropertyFromSystem(propID);
      if (StringUtils.isEmpty(propStr)) return false;

      if (!hasHL7Property(propID)) setHL7Property(propID, propStr);
      if (!hasDefaultHL7Property(propID)) setDefaultHL7Property(propID, propStr);
      return true;
   } // loadPropertyFromSystem


   public boolean hasEncoding() {
      return getProperty(buildHL7PropName(HL7_PROPERTY_ENCODING)) != null;
   } // hasEncoding


   public boolean hasDefaultEncoding() {
      return getProperty(buildDefaultHL7PropName(HL7_PROPERTY_ENCODING)) != null;
   } // hasDefaultEncoding

   public HL7Encoding getHL7Encoding() {
      if (!hasEncoding()) return null;
      return new HL7Encoding(getProperty(buildHL7PropName(HL7_PROPERTY_ENCODING)));
   } // getHL7Encoding

   public static String getProcessId() {
       String nameStr = ManagementFactory.getRuntimeMXBean().getName();
       StringBuilder pidBuffer = new StringBuilder();
       for (int index = 0, len = nameStr.length(); index < len; ++index) {
           if (Character.isDigit(nameStr.charAt(index))) {
               pidBuffer.append(nameStr.charAt(index));
           } else if (pidBuffer.length() > 0) {
               break;
           } // if
       } // for

       return pidBuffer.toString();
   } // getProcessId


   public static void registerProcessID(String procName) throws IOException {
      if (StringUtils.isEmpty(procName)) return;

      String pidStr = getProcessId();
      if (StringUtils.isEmpty(pidStr)) return;

      File file = new File("/var/run/" + procName + ".pid");
      if (!file.exists()) file.createNewFile();

      FileOutputStream outStream = new FileOutputStream(file);
      outStream.write(pidStr.getBytes());
      outStream.close();
   } // registerProcessID


} // HL7Properties
