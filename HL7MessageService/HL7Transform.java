/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7Transform.java : An ordered group of operations to be applied to a HL7
 *                      transaction message (HL7Message).
 *
 *  Copyright (C) 2009  Scott Herman
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


package us.conxio.HL7.HL7MessageService;

/**
 *
 * @author scott herman <scott.herman@unconxio.us>
 */

import org.w3c.dom.*;
import java.util.regex.*;
import java.net.URI;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import us.conxio.HL7.HL7Message.*;
import us.conxio.HL7.HL7Stream.*;

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
} // Operand


/**
 * A class for the atomic operations of HL7 transforms. Each operation is specified by a separate xml entity, 
 * <ul>and contains <li>a name. (String)<li>a resultant HL7 item designation. (String)<li>a list of operands.</ul>
 */
class HL7Operation {
   String                  opName,
                           resultDesignator;
   Pattern                 opPattern;
   Operand[]               operands;
   private static Logger   logger = null;

   /* TBD: Add an operation to add a segment.
    */
   
   // constructors
   public HL7Operation() { }
   
   public HL7Operation(String name, String designator) {
      this.opName = name;
      this.resultDesignator = designator;
      HL7Operation.logger = Logger.getLogger(this.getClass());
      HL7Operation.logger.setLevel(Level.TRACE);
   } // HL7Operation
   
   public HL7Operation(Node node) {
     HL7Operation.logger = Logger.getLogger(this.getClass());
     HL7Operation.logger.setLevel(Level.TRACE); 
     String localOpName = node.getNodeName();

     this.opName = localOpName;
      if (node.hasAttributes()) {
         Node tmpNode;
         NamedNodeMap map = node.getAttributes();
         if (localOpName.matches("assign|appoint|exclude|replace|qualify|scrub|copy|remove|newseg.*|freshen") ) {
            if ( (tmpNode = map.getNamedItem("designator")) != null) {
               this.resultDesignator = tmpNode.getNodeValue();
            } // if
            
            if (localOpName.equals("replace")) {
               if ( (tmpNode = map.getNamedItem("search")) != null) {
                  this.addOperand(new Operand("search", tmpNode.getNodeValue() ) );
               } // if               
            } // if
         } // if 
      } // if
      
      String nodeText = node.getTextContent();
      if (nodeText != null && nodeText.length() > 0) {
         String[] operandsArray = nodeText.split(", ?", -2);
         int opsLength = operandsArray.length;
         for (int index = 0; index < opsLength; ++index) {
            this.addOperand(new Operand("string", operandsArray[index]));
         } // for
      } // if
   } // HL7Operation
   

   private void logDebug(String msg) {
      if (HL7Operation.logger != null) {
         HL7Operation.logger.debug(msg);
      } else {
         System.out.println(msg);
      } // if - else
   } // logDebug


   void addOperand(Operand operand) {
      int opListLength = 0;
      if (this.operands != null) {
         opListLength = this.operands.length;
      } // if
      
      Operand[] newOps = new Operand[opListLength + 1];
      for (int index = 0; index < opListLength; ++index) {
         newOps[index] = this.operands[index];
      } // for
      newOps[opListLength] = operand;
      this.operands = newOps;
   } // addOperand


   void dump() {
      this.logDebug("HL7Operation name:" + this.opName + ", ResultDesignator:" + this.resultDesignator);
      
      int numOps = 0;
      if (this.operands != null) {
         numOps = this.operands.length;
      } // if
      
      for (int index = 0; index < numOps; ++index) {
         this.logDebug(this.opName + ":" + this.operands[index].dumpString() );
      } // for
   } // dump
   
   
// Operation handlers:
   
   boolean qualify(HL7Message msg) {
      boolean qualify = false,
              exclude = false;
      
      if (this.opName.equals("qualify") ) {
         qualify = true;
      } else if (this.opName.equals("exclude") ) {
         exclude = true;
      } // if - else if
      
      if (!(qualify ||exclude)) return false;
      
      String designator = this.resultDesignator;

      // First time through this operation, initialize the pattern
      if (this.opPattern == null) {
         String checkStr = this.operands[0].value;
         if (this.operands[0].typeStr.equals("string") && checkStr != null) {
            this.opPattern = Pattern.compile(checkStr);
         } // if            
      } // if

      if (designator != null && this.opPattern != null) {
         String subject = msg.get(designator)[0];
         if (subject != null && subject.length() > 0) {
            Matcher matcher = this.opPattern.matcher(subject);
            String checkStr = this.opPattern.pattern();
            boolean retnVal = false,
                    success = false;
            if (matcher.matches()) {
               success = true;
               if (qualify) {
                  retnVal = true;
               } else if (exclude) {
                  retnVal = false;
               } // if - else if
            } else {
               if (qualify) {
                  retnVal = false;
               } else if (exclude) {
                  retnVal = true;
               } // if - else if               
            } // if - else

            if (retnVal == false) {
               this.logDebug( "Message "
                           +  msg.IDString()
                           +  ": "
                           +  this.opName
                           +  " "
                           + (success ? "succeeded" : "failed")
                           + " for "
                           +  designator
                           +  ":"
                           +  checkStr
                           +  " vs. "
                           +  subject);
            } // if

            return retnVal;
         } // if
      } // if

      return false;
   } // qualify
   

   String assign(HL7Message msg) {
      if (this.opName.equals("assign")) {
         String designator = this.resultDesignator;
         String subject = "";

         if (  this.operands != null
         &&    this.operands[0] != null
         &&    this.operands[0].typeStr.equals("string") ) {
            subject = this.operands[0].value;
         } // if

         if (subject != null) {
            msg.set(designator, subject);
            return(subject);
         } // if         
      } // if
     
      return null;
   } // assign
   

   boolean newSegment(HL7Message msg) {
      if (this.opName.equals("newsegment")) {
         String designator = this.resultDesignator;
         String arg = this.operands[0] == null ? null : this.operands[0].value;
         String segStr = designator;
         if (segStr == null) {
            segStr = arg;
         } else if (arg != null && segStr != null && arg.length() > segStr.length()) {
            segStr = arg;
         } // if

         if (segStr == null || segStr.length() < 3) {
            return false;
         } // if

         msg.addSegment(segStr);
         return true;
      } // if

      return false;
   } // newSegment


   private String normalizeDesignator(String designatorStr, HL7Message msg) {
      String returnStr = designatorStr;
      HL7Designator hl7Designator = new HL7Designator(designatorStr);
      if (hl7Designator.getSegIndex() <= 0) {
         hl7Designator.setSegIndex(msg.countSegment(hl7Designator.getSegID()) - 1);
         returnStr = hl7Designator.toString();
      } // if

      return returnStr;
   } // normalizeDesignator


   String appoint(HL7Message msg) {
      if (this.opName.equals("appoint")) {
         String designator = this.resultDesignator;
         String subject = null;

         if (  this.operands != null
         &&    this.operands[0] != null
         &&    this.operands[0].typeStr.equals("string") ) {
            subject = this.operands[0].value;
         } // if

         if (subject != null) {
            msg.set(normalizeDesignator(designator, msg), subject);
            return(subject);
         } // if
      } // if

      return null;
   } // appoint


   boolean swap(HL7Message msg) {
      if (this.opName.equals("swap")) {
         if (this.operands[0] == null || this.operands[1] == null) {
            return false;
         } // if
            
         if (msg.get(this.operands[0].value) == null || msg.get(this.operands[1].value) == null) {
            return(false);
         } // if
            
         msg.swap(this.operands[0].value, this.operands[1].value); 
         
         return(true);
      } // if
      
      return(false);
   } // swap
   
   
   boolean replace(HL7Message msg) {
      if (this.opName.equals("replace")) {
         String designator = this.resultDesignator;
         if (designator == null || designator.length() < 1) { // no designator?
            return false;
         } // if
            
         String subject = msg.get(designator)[0];
         
         String search = null;
         String replacement = null;
         int    opCnt = this.operands.length;
         for (int jIndex = 0; jIndex < opCnt; ++jIndex) {
            if (this.operands[jIndex] != null) {
               // first time through this operation, set the search pattern.
               if (this.operands[jIndex].typeStr.equals("search") && this.opPattern == null) {
                  search = this.operands[jIndex].value;
                  if (search != null && search.length() > 0) {
                     this.opPattern = Pattern.compile(search);
                  } // if
               } else {
                  replacement = this.operands[jIndex].value;
               }
            } // if
         } // for
            
         if (replacement == null) {
            replacement = "";
         } // if

         String result = null;
         if (  subject != null && subject.length() > 0 
         &&    this.opPattern != null) {
            Matcher matcher = this.opPattern.matcher(subject); 
            result = matcher.replaceAll(replacement);
         } // if

         /* Note that regex search and replace only occurs when 
          *  - the subject (destination) is not empty
          *  - the search string is not empty
          * In all other situations the specified replacement is assigned to the designated item (destination).
          */
         if (result == null) {
            result = replacement;
         } // if

         msg.set(designator, result);
         return true;
      } // if
            
      return(false);
   } // replace
   
   
   boolean scrub(HL7Message msg) {
      if (this.opName.equals("scrub")) {
         String manifest[] = msg.census(this.resultDesignator);
         if (manifest != null) {
            String toBeScrubbed;
            for (int censusIndex = 0; censusIndex < manifest.length; ++censusIndex) {
               toBeScrubbed = msg.get(manifest[censusIndex])[0];
               if ( toBeScrubbed != null && toBeScrubbed.length() > 0) {
                  String replUCAlpha = toBeScrubbed.replaceAll("[A-Z]", "X");
                  String replLCAlpha = replUCAlpha.replaceAll("[a-z]", "x");
                  String replDigits = replLCAlpha.replaceAll("[0-9]", "9");
                  msg.set(manifest[censusIndex], replDigits);
              } // if   
            } // for
            
            return true;
         } // if       
      } // if
            
      return(false);
   } // scrub
   
   
   boolean copy(HL7Message msg) {
      if (this.opName.equals("copy")) {
         String toDesignator = this.resultDesignator;
         if (toDesignator == null || toDesignator.length() < 1) { // no designator?
            return false;
         } // if
         
         String fromDesignator = null;
         if (this.operands[0].typeStr.equals("string|designator") ) {
            fromDesignator = this.operands[0].value;
         } // if
            
         String subject = msg.get(fromDesignator)[0];
         msg.set(toDesignator, subject);
         
         return(true);
      } // if
            
      return(false);      
   } // copy
   
   
   boolean remove(HL7Message msg) {
      if (this.opName.equals("remove")) {
         String designator = this.resultDesignator;
         if (designator == null || designator.length() < 1) { // no designator?
            return false;
         } // if
            
         msg.remove(designator);
         return(true);
      } // if
            
      return(false);      
   } // remove
   
   
   // operation "aliases"
   boolean IsQualified(HL7Message msg) {
      return(this.qualify(msg));
   } // IsQualified
} // HL7Operation


/**
 * A HL7Transform is an ordered group of operations to be applied to a HL7 transaction message (HL7Message).
 * Each HL7Transform consists of one or more operations:<ul>
 * <li><code>qualify</code>: specifies transaction qualification criteria.<br>eg; <code>&lt;qualify designator="MSH.9.2"&gt;A01|A02|A03&lt;/qualify&gt;</code>
 * <li><code>exclude</code>: specifies transaction exclusion criteria.<br>eg; <code>&lt;exclude designator="MSH.9.2"&gt;A01|A02|A03&lt;/exclude&gt;</code>
 * <li><code>assign</code>: specifies &quot;hard-coded&quot; assignments.<br>eg; <code>&lt;assign designator="MSH.5"&gt;Test&lt;/assign&gt;</code>
 * <li><code>appoint</code>: specifies assignments to the last segment without requiring the segment index.<br>eg; <code>&lt;appoint designator="OBX.5"&gt;Test&lt;/assign&gt;</code>
 * <li><code>replace</code>: specifies regular expression based replacement.<br>eg; <code>&lt;replace designator="PID.3.1" search="^0+"&gt;&lt;/replace&gt;</code>
 * <li><code>swap</code>: specifies swapping of transaction content items.<br>eg; <code>&lt;swap&gt;OBR.2, OBR.3&lt;/swap&gt;</code>
 * <li><code>scrub</code>: specifies encoding of personal identity items.<br>eg; <code>&lt;scrub designator="PID.14" /&gt;</code>
 * <li><code>copy</code>: specifies duplication of item content.<br>eg; <code>&lt;copy designator="ORC.3.1"&gt;MSH.5&lt;/copy&gt;</code>
 * <li><code>remove</code>: specifies removal of specific content.<br>eg; <code>&lt;remove designator="PID.14" /&gt;</code>
 * <li><code>freshen</code>: specifies assignment of the creation date time to the message.<br>eg; <code>&lt;freshen/&gt;</code>
 * <li><code>newsegment</code>: specifies addition of a new segment to the message.<br>eg; <code>&lt;newsegment designator="OBX"/&gt;</code>
 * </ul>
 */
public class HL7Transform extends HL7SpecificationElement {
   String                  idStr;
   HL7Operation[]          operations;
   private static Logger   logger = null;
   
   /**
    * Reads the argument DOM node and creates an appropriate HL7Transform.
    * @param xForm The DOM node representing the XML HL7Transform specification. 
    * <ul>A HL7Transform XML specification may contain the following attributes, 
    * all of which, except ID, are taken to be qualification specifications
    * for the associated message item :
    * <li>ID<br>id - A unique identifier.
    * <li>MsgType<br>MessageType - MSH.9.1
    * <li>MsgEvent<br>MessageEvent - MSH.9.2
    * <li>SendingApplication - MSH.3
    * <li>SendingFacility - MSH.4
    * <li>ReceivingApplication - MSH.5
    * <li>ReceivingFacility - MSH.6
    * <li>OrderControl - ORC.1
    */
   public HL7Transform(Node xForm) {
      initializeHL7Transform(xForm);
   } // HL7Transform


   /**
    * Instantiates an HL7Transform from the argument URI
    */
   public HL7Transform(URI uri) throws Exception {
      this.initialize("HL7Transform", uri);
      initializeHL7Transform(this.root);
   } // HL7Transform


   public HL7Transform(String xmlString) {
      this.initialize("HL7Transform", xmlString);
      initializeHL7Transform(this.root);
   } // HL7Transform

   
   private void initializeHL7Transform(Node xForm) {
      /* HL7Transform.logger = Logger.getLogger(this.getClass());
      HL7Transform.logger.setLevel(Level.TRACE); */

      // Handle attributes
      if (xForm.hasAttributes() ) {
         Node tmpNode;
         NamedNodeMap map = xForm.getAttributes();
         /*
         for (int mapIndex = 0; mapIndex < map.getLength(); ++mapIndex) {
            Node mapItem = map.item(mapIndex);
            System.out.println("name:" + mapItem.getNodeName() + ", value:" + mapItem.getNodeValue() );
         } // for 
         */
         
         if ( ( (tmpNode = map.getNamedItem("ID") )!= null) 
         || ((tmpNode = map.getNamedItem("id") )!= null) ) {
            this.idStr = tmpNode.getNodeValue();
         } // if 

         if ( ( (tmpNode = map.getNamedItem("MsgType") ) != null)
         ||   ( (tmpNode = map.getNamedItem("MessageType") ) != null) ) {
            HL7Operation opMsgTypeQual = new HL7Operation("qualify", "MSH.9.1"); 
            opMsgTypeQual.addOperand(new Operand("string", tmpNode.getNodeValue()));
            this.AddOperation(opMsgTypeQual);
         } // if

         if ( ( (tmpNode = map.getNamedItem("MsgEvent") ) != null)
         ||   ( (tmpNode = map.getNamedItem("MessageEvent") ) != null) ) {
            HL7Operation opMsgEventQual = new HL7Operation("qualify", "MSH.9.2");
            opMsgEventQual.addOperand(new Operand("string", tmpNode.getNodeValue()));
            this.AddOperation(opMsgEventQual);
         } // if

         if ( (tmpNode = map.getNamedItem("SendingApplication") ) != null) {
            HL7Operation opSendAppQual = new HL7Operation("qualify", "MSH.3");
            opSendAppQual.addOperand(new Operand("string", tmpNode.getNodeValue()));
            this.AddOperation(opSendAppQual);
         } // if

         if ( (tmpNode = map.getNamedItem("SendingFacility") ) != null) {
            HL7Operation opSendFacQual = new HL7Operation("qualify", "MSH.4");
            opSendFacQual.addOperand(new Operand("string", tmpNode.getNodeValue()));
            this.AddOperation(opSendFacQual);
         } // if

         if ( (tmpNode = map.getNamedItem("ReceivingApplication") ) != null) {
            HL7Operation opRecvAppQual = new HL7Operation("qualify", "MSH.5");
            opRecvAppQual.addOperand(new Operand("string", tmpNode.getNodeValue()));
            this.AddOperation(opRecvAppQual);
         } // if

         if ( (tmpNode = map.getNamedItem("ReceivingFacility") ) != null) {
            HL7Operation opRecvFacQual = new HL7Operation("qualify", "MSH.6");
            opRecvFacQual.addOperand(new Operand("string", tmpNode.getNodeValue()));
            this.AddOperation(opRecvFacQual);
         } // if

         if ( (tmpNode = map.getNamedItem("OrderControl") ) != null) {
            HL7Operation opOrderCtlQual = new HL7Operation("qualify", "ORC.1");
            opOrderCtlQual.addOperand(new Operand("string", tmpNode.getNodeValue()));
            this.AddOperation(opOrderCtlQual);
         } // if
      } // if
      
      if (xForm.hasChildNodes()) {
         NodeList kids = xForm.getChildNodes();
         int numKids = kids.getLength();
         for (int index = 0; index < numKids; ++index) {
            Node kid = kids.item(index);   
            if (kid.getNodeType() != Node.TEXT_NODE && !kid.getNodeName().startsWith("#")) {
               this.AddOperation(new HL7Operation(kid) );
            } // if
         } // for
      } // if
   } // initializeHL7Transform
   
   
   private void AddOperation(HL7Operation opern) {
      int opListLength = 0;
      if (this.operations != null) {
         opListLength = this.operations.length;
      } // if
      HL7Operation[] newOpList = new HL7Operation[opListLength + 1];
      
      for (int index = 0; index < opListLength; ++index) {
         newOpList[index] = this.operations[index];
      } // for
      
      newOpList[opListLength] = opern;
      this.operations = newOpList;
   } // AddOperation
   

   /**
    * Creates a formatted dump of the context HL7Transform.
    */
   public void dump() {
      this.logDebug("HL7Transform idStr:" + this.idStr);

      int numOps = this.operations.length;
      for (int index = 0; index < numOps; ++index) {
         this.operations[index].dump();
      } // for
   } // dump
   
   
   /**
    * Checks the argument messgae against any qualifications specified by the context HL7Transform.
    * @param msg The argument HL7Message object.
    * @return false if the message fails to qualify against any of the qualifications, otherwise true.
    */
   public boolean IsQualified(HL7Message msg) {
      int opCount = this.operations.length;
      
      for (int index = 0; index < opCount; ++index) {
         if (  this.operations[index].opName.equals("qualify") 
         ||    this.operations[index].opName.equals("exclude") ) {
            if (this.operations[index].IsQualified(msg) != true) {
               return false;
            } // if
         } // if 
      } // for
      
      return true;
   } // IsQualified
   
   
   /**
    * Modifies the argument HL7Message object, in accordance with the qualifications and operations of the context
    * HL7Transform.
    * @param msg The message to qualify and modify.
    * @return The modified message as a HL7Message object.
    * @throws java.lang.Exception
    */
   public HL7Message Render(HL7Message msg) throws HL7IOException {
      int opCount = this.operations.length;
      
      for (int index = 0; index < opCount; ++index) {
         if (this.operations[index].opName.equals("qualify")) {
            continue;
         } else if (this.operations[index].opName.equals("exclude") ) {
            continue;
         } else if (this.operations[index].opName.equals("assign") ) {
            this.operations[index].assign(msg);
         } else if (this.operations[index].opName.equals("appoint") ) {
            this.operations[index].appoint(msg);
         } else if (this.operations[index].opName.startsWith("newseg") ) {
            this.operations[index].newSegment(msg);
         } else if (this.operations[index].opName.equals("swap") ) {
            this.operations[index].swap(msg);
         } else if (this.operations[index].opName.equals("replace") ) {
            this.operations[index].replace(msg);
         } else if (this.operations[index].opName.equals("scrub") ) {
            this.operations[index].scrub(msg);
         } else if (this.operations[index].opName.equals("remove") ) {
            this.operations[index].remove(msg);
         } else if (this.operations[index].opName.equals("copy") ) {
            this.operations[index].copy(msg);
         } else if (this.operations[index].opName.equals("freshen") ) {
            msg.fresh();
         } else {
            this.logDebug("HL7Transform.Render:Unexpected operation:" + this.operations[index].opName);
         } // if - else if, else if, ,,, else 
      } // for
      
      return new HL7Message(msg.toString());
   } // Render      
} // HL7Transform
