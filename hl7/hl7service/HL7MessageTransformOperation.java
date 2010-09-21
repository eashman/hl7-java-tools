/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7MessageTransformOperation.java : A class for the atomic operations of HL7
 *                                      message transforms.
 *
 *  Copyright (c) 2009, 2010  Scott Herman
 *
 *  This is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This code is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this code.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package us.conxio.hl7.hl7service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

import org.w3c.dom.Node;

import us.conxio.XMLUtilities.AttributeMap;
import us.conxio.hl7.hl7message.HL7Designator;
import us.conxio.hl7.hl7message.HL7Message;



/**
 * A polymorphic container class for the argument items of HL7 transform operations. <ul>It contains two strings;
 * <li>a type identifier string. <li>a value string.</ul>
 */
class Operand {
   String  typeStr,
           value;


   public Operand() {}

   public Operand(String type, String value) {
      this.typeStr = type;
      this.value = value;
   } // Operand


   String dumpString() {
      return "Operand:" + this.typeStr + ", " + this.value;
   } // Dump

   boolean isType(String name) {
      return StringUtils.equals( this.typeStr.toLowerCase(),
                                 (name != null) ? name.toLowerCase() : name);
   } // isType
} // Operand




/**
 * A class for the atomic operations of HL7 transforms.
 * Each operation is specified by a separate xml entity,
 * <ul>and contains
 * <li>a name. (String)
 * <li>a resultant HL7 item designation. (String)
 * <li>a list of operands.</ul>
 * @author scott
 */
public class HL7MessageTransformOperation {
   private String             opName;
   private String             resultDesignator;
   private Method             method = null;
   private Pattern            opPattern;
   private TranslationTable   xlateTable = null;
   private URI                resourceURI = null;

   ArrayList<Operand>         operands = null;
   private static Logger      logger = Logger.getLogger("us.conxio.hl7");


   public static final String   OPERAND_TYPE_STRING = "string";
   public static final String   OPERAND_TYPE_SEARCH = "search";
   public static final String   OPERAND_TYPE_DESIGNATOR = "designator";
   public static final String   OPERAND_TYPE_RESOURCE = "resource-uri";

   public static final String   OPERATION_NAME_QUALIFY = "qualify";
   public static final String   OPERATION_NAME_EXCLUDE = "exclude";

   // constructors
   public HL7MessageTransformOperation() { }

   public HL7MessageTransformOperation(String name, String designator) {
      this.opName = name;
      this.resultDesignator = designator;
      this.setMethod();
   } // HL7MessageTransformOperation


   public HL7MessageTransformOperation(String name, String designator, String operandType, String operandValue) {
      this.opName = name;
      this.resultDesignator = designator;
      this.setMethod();
      this._addOperand(new Operand(operandType, operandValue));
   } // HL7MessageTransformOperation


   /**
    * Constructs a HL7MessageTransformOperation for the argument xml Node.
    * @param node The node from which to construct the object.
    * @throws IOException If the file is not found, or is not properly formatted.
    */
   public HL7MessageTransformOperation(Node node) throws IOException {
     opName = node.getNodeName().toLowerCase();
     if (!haveThisMethod()) {
        logger.error("Not a valid method:" + opName);
        return;
     } // if

      if (node.hasAttributes()) {
         AttributeMap attributes = new AttributeMap(node);
         if (attributes.hasKey(OPERAND_TYPE_DESIGNATOR)) {
            resultDesignator = attributes.get(OPERAND_TYPE_DESIGNATOR);
         } // if

         if (attributes.hasKey(OPERAND_TYPE_SEARCH)) {
            _addOperand(new Operand(OPERAND_TYPE_SEARCH, attributes.get(OPERAND_TYPE_SEARCH)));
         } // if
         
         if (attributes.hasKey(OPERAND_TYPE_RESOURCE)) {
            initializeMap(attributes.get(OPERAND_TYPE_RESOURCE));
         } // if
      } // if

      String nodeText = node.getTextContent();
      if (StringUtils.isNotEmpty(nodeText)) {
         String[] operandsArray = nodeText.split(", ?", -2);
         int opsLength = operandsArray.length;
         for (int index = 0; index < opsLength; ++index) {
            this._addOperand(new Operand("string", operandsArray[index]));
         } // for
      } // if
   } // HL7MessageTransformOperation


   private void _addOperand(Operand operand) {
      if (this.operands == null) this.operands = new ArrayList<Operand>();
      this.operands.add(operand);
   } // _addOperand


   void addOperand(Operand operand) { _addOperand(operand); }

   /**
    * Add an operand to the context operation.
    * @param typeStr
    * @param valueStr
    * @return
    */
   public HL7MessageTransformOperation addOperand(String typeStr, String valueStr) {
      this.addOperand(new Operand(typeStr, valueStr));
      return this;
   } // addOperand


   void dump() {
      logger.debug(  "HL7Operation name:"
                   + this.getOpName()
                   + ", ResultDesignator:"
                   + this.getResultDesignator());
      for (Operand operand : this.operands) logger.debug(operand.dumpString());
   } // dump


   private void initializeMap(String uriStr) throws FileNotFoundException, IOException {
      try {
         resourceURI = new URI(uriStr);
      } catch (URISyntaxException ex) {
         throw new IllegalArgumentException(ex);
      } // try - catch

      if (!resourceURI.getScheme().equalsIgnoreCase("file")) {
         throw new IllegalArgumentException("URI schemes other than file/// not handled.");
      } // if

      xlateTable = this.loadTranslationTable();
   } // initializeMap


   private TranslationTable loadTranslationTable() throws FileNotFoundException, IOException {
      return TranslationTable.make(this.resourceURI);
   } // loadTranslationTable

// Operation handlers:

   boolean qualify(HL7Message msg) {
      if (!hasPattern() && hasStringOperandAt(0)) opPattern = Pattern.compile(stringOperandValueAt(0));
      return this.messageMatches(msg);
   } // qualify


   boolean exclude(HL7Message msg) {
      if (!hasPattern() && hasStringOperandAt(0)) opPattern = Pattern.compile(stringOperandValueAt(0));
      return this.messageMatches(msg);
   } // exclude

   private boolean messageMatches(HL7Message msg) {
      if (!hasDesignator()) return false;
      if (opPattern == null) return false;
      String subject = msg.get(resultDesignator);
      if (StringUtils.isEmpty(subject)) return false;
      return this.opPattern.matcher(subject).matches();
   } // messageMatches

   boolean assign(HL7Message msg) {
      if (hasDesignator() && hasStringOperandAt(0)) {
         msg.set(resultDesignator, stringOperandValueAt(0));
         return true;
      } // if

      return false;
   } // assign


   boolean newsegment(HL7Message msg) {
      String segStr = null;
      String arg = stringOperandValueAt(0);
      if (hasDesignator() && resultDesignator.length() > 2) {
         segStr = this.resultDesignator.substring(0, 3);
      } // if

      if (StringUtils.isNotEmpty(segStr)
      &&  StringUtils.isNotEmpty(arg)
      &&  arg.length() > segStr.length()) {
         segStr = arg;
      } // if

      if (StringUtils.isEmpty(segStr)) segStr = arg;
      if (StringUtils.isEmpty(segStr) || segStr.length() < 3) return false;

      msg.addSegment(segStr);
      return true;
   } // newsegment


   /**
    * Re-evaluate wild card segment index to refer to the last segment.
    * @param designatorStr
    * @param msg
    * @return
    */
   private String normalizeDesignator(String designatorStr, HL7Message msg) {
      String returnStr = designatorStr;
      HL7Designator hl7Designator = new HL7Designator(designatorStr);
      if (hl7Designator.getSegIndex() <= 0) {
         hl7Designator.setSegIndex(msg.countSegment(hl7Designator.getSegID()) - 1);
         returnStr = hl7Designator.toString();
      } // if

      return returnStr;
   } // normalizeDesignator


   boolean appoint(HL7Message msg) {    
      if (hasDesignator() && hasStringOperandAt(0)) {
         msg.set(normalizeDesignator(resultDesignator, msg), stringOperandValueAt(0));
         return true;
      } // if

      return false;
   } // appoint


   boolean swap(HL7Message msg) {
      if (!hasStringOperandAt(0) || !hasStringOperandAt(1)) return false;
      msg.swap(stringOperandValueAt(0), stringOperandValueAt(1));
      return(true);
   } // swap


   boolean replace(HL7Message msg) {
      if (!hasDesignator()) return false;
      String subject = msg.get(resultDesignator);
      if (StringUtils.isEmpty(subject)) return false;

      String replacement = null;

      for (Operand op : this.operands) {
         if (op.isType(OPERAND_TYPE_SEARCH)) {
            if (!this.hasPattern() && StringUtils.isNotEmpty(op.value)) {
               this.opPattern = Pattern.compile(op.value);
            } // if
         } else {
            replacement = op.value;
         } // if - else
      } // for

      if (!hasPattern()) return false;
      if (replacement == null) replacement = "";

      Matcher matcher = this.opPattern.matcher(subject);
      String result = matcher.replaceAll(replacement);


      /* Note that regex search and replace only occurs when
       *  - the subject (destination) is not empty
       *  - the search string is not empty
       * In all other situations the specified replacement is assigned to the designated item (destination).
       */
      if (result == null) result = replacement;

      msg.set(resultDesignator, result);
      return true;
   } // replace


   boolean scrub(HL7Message msg) {
      if (!this.hasDesignator()) return false;

      String toBeScrubbed = msg.get(resultDesignator);
      if (StringUtils.isNotEmpty(toBeScrubbed)) {
         String replUCAlpha = toBeScrubbed.replaceAll("[A-Z]", "X");
         String replLCAlpha = replUCAlpha.replaceAll("[a-z]", "x");
         String replDigits = replLCAlpha.replaceAll("[0-9]", "9");
         msg.set(resultDesignator, replDigits);
         return true;
      } // if

      return false;
   } // scrub


   boolean copy(HL7Message msg) {
      if (!this.hasDesignator()) return false;

      String fromDesignator = null;
      if (this.hasStringOperandAt(0) || this.hasDesignatorOperandAt(0)) {
         fromDesignator = this.operands.get(0).value;
      } // if

      if (StringUtils.isEmpty(fromDesignator)) return false;

      String subject = msg.get(fromDesignator);
      msg.set(resultDesignator, subject);

      return(true);
   } // copy


   boolean remove(HL7Message msg) {
      if (!this.hasDesignator()) return false;
      msg.remove(resultDesignator);
      return(true);
   } // remove


   boolean translate(HL7Message msg) throws FileNotFoundException, IOException {
      if (!hasDesignator()) return false;

      String subject = null;
      if (hasStringOperandAt(0)) subject = stringOperandValueAt(0);
      if (StringUtils.isEmpty(subject)) subject = msg.get(resultDesignator);
      if (StringUtils.isEmpty(subject)) return false;

      String replacement = null;
      if (!hasTranslationTable()) xlateTable = this.loadTranslationTable();
      if (!hasTranslationTable()) return false;
      
      replacement = this.xlateTable.get(subject);
      msg.set(resultDesignator, replacement);
      return true;
   } // translate

   // operation "aliases"
   
   boolean isQualified(HL7Message msg) {
      if (this.opName.equalsIgnoreCase(OPERATION_NAME_QUALIFY)) return this.qualify(msg);
      if (this.opName.equalsIgnoreCase(OPERATION_NAME_EXCLUDE)) return !this.exclude(msg);
      return false;
   } // isQualified

   
   HL7Message transform(HL7Message msg) {
      if (msg == null) return null;

      if (!this.hasMethod()) this.setMethod();
      try {
         Object retnObj = this.method.invoke(this, msg);
         return  ((Boolean)retnObj).booleanValue()
                 ? msg
                 : null;
      } catch (IllegalAccessException ex) {
         logger.error(null, ex);
      } catch (IllegalArgumentException ex) {
         logger.error(null, ex);
      } catch (InvocationTargetException ex) {
         logger.error(null, ex);
      } // try - catch

      return null;
   } // transform

   private void setMethod() {
      Class parms[] = { HL7Message.class };
      try {
         this.method = this.getClass().getDeclaredMethod(getOpName().toLowerCase(), parms);
      } catch (NoSuchMethodException ex) {
         logger.error("opName:" + getOpName(), ex);
      } catch (SecurityException ex) {
         logger.error("opName:" + getOpName(), ex);
      } // try - catch
   } // setMethod

   private boolean hasMethod() {
      return this.method != null;
   } //hasMethod

   /**
    * @return the opName
    */
   public String getOpName() {
      return opName;
   }

   /**
    * @param opName the opName to set
    */
   public void setOpName(String opName) {
      this.opName = opName;
   }

   /**
    * @return the resultDesignator
    */
   public String getResultDesignator() {
      return resultDesignator;
   }

   /**
    * @param resultDesignator the resultDesignator to set
    */
   public void setResultDesignator(String resultDesignator) {
      this.resultDesignator = resultDesignator;
   }

   private boolean hasOperand(int index) {
      return this.hasOperands() && this.operands.size() > index;
   } // hasOperand

   private boolean hasOperands() {
      return this.operands != null && !this.operands.isEmpty();
   } // hasOperands

   private boolean hasStringOperandAt(int index) {
      return this.hasTypeOperandAt(OPERAND_TYPE_STRING, index);
   } // hasStringOperandAt

   private String stringOperandValueAt(int index) {
      return (this.hasStringOperandAt(index)) ? this.operands.get(index).value : null;
   } // stringOperandValueAt

   private boolean hasPattern() {
      return this.opPattern != null;
   } // hasPattern

   private boolean hasDesignatorOperandAt(int index) {
      return this.hasTypeOperandAt(OPERAND_TYPE_DESIGNATOR, index);
   } // hasDesignatorOperandAt

   private boolean hasTypeOperandAt(String typeStr, int index) {
      return this.hasOperand(index)
      &&     this.operands.get(index).isType(typeStr)
      &&     StringUtils.isNotEmpty(this.operands.get(index).value);
   } // hasTypeOperandAt

   private boolean hasDesignator() {
      return StringUtils.isNotEmpty(resultDesignator);
   } // hasDesignator

   private boolean haveThisMethod() {
      Class parms[] = { HL7Message.class };
      try {
         method = this.getClass().getDeclaredMethod(opName.toLowerCase(), parms);
         return true;
      } catch (NoSuchMethodException ex) {
         return false;
      } catch (SecurityException ex) {
         return false;
      } // try - catch
   } // haveThisMethod

   public static boolean haveMethod(String name) {
      Class parms[] = { HL7Message.class };
      try {
         HL7MessageTransformOperation.class.getDeclaredMethod(name.toLowerCase(), parms);
         return true;
      } catch (NoSuchMethodException ex) {
         return false;
      } catch (SecurityException ex) {
         return false;
      } // try - catch
   }

   boolean isQualificationOperation() {
         return   opName.equalsIgnoreCase(OPERATION_NAME_QUALIFY)
         ||       opName.equalsIgnoreCase(OPERATION_NAME_EXCLUDE);
   } // isQualificationOperation

   private boolean hasTranslationTable() {
      return xlateTable != null && !xlateTable.isEmpty();
   } // hasTranslationTable

} // HL7MessageTransformOperation
