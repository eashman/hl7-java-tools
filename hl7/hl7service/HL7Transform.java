/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7Transform.java : An ordered group of operations to be applied to a HL7
 *                      transaction message (HL7Message), in order to effect a
 *                      transformation of the message.
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

/**
 *
 * @author scott herman <scott.herman@unconxio.us>
 */


import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import us.conxio.XMLUtilities.AttributeMap;

import us.conxio.hl7.hl7message.HL7Message;


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
public class HL7Transform extends HL7ServiceElement {
   private ArrayList<HL7MessageTransformOperation> operations = null;
   private AttributeMap                            attributes = null;

   public static final String NAME_HL7TRANSFORM = "hl7transform";
   
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
      try {
         initializeHL7Transform(xForm);
      } catch (IOException ex) {
         throw new IllegalArgumentException(null, ex);
      } // try - catch
   } // HL7Transform


   /**
    * Instantiates an HL7Transform from the argument URI
    */
   public HL7Transform(URI uri) throws Exception {
      this.initialize("HL7Transform", uri);
      initializeHL7Transform(root);
   } // HL7Transform


   public HL7Transform(String xmlString) throws Exception {
      this.initialize("HL7Transform", xmlString);
      initializeHL7Transform(root);
   } // HL7Transform

   
   private void initializeHL7Transform(Node xForm) throws IOException {
      this.root = xForm;
      if (xForm.hasAttributes() ) parseAttributes(xForm);
      if (xForm.hasChildNodes())  parseChildElements(xForm);
   } // initializeHL7Transform
   
   
   private void parseAttributes(Node node) {
      if (!node.hasAttributes()) return;
      if (attributes == null) attributes = new AttributeMap(node);
      if (attributes.entryCount() < 1) return;
      setID();

      addOperationForAttribute(  "msgtype",
                                 HL7MessageTransformOperation.OPERATION_NAME_QUALIFY,
                                 "MSH.9.1",
                                 HL7MessageTransformOperation.OPERAND_TYPE_STRING);
      addOperationForAttribute(  "messagetype",
                                 HL7MessageTransformOperation.OPERATION_NAME_QUALIFY,
                                 "MSH.9.1",
                                 HL7MessageTransformOperation.OPERAND_TYPE_STRING);

      addOperationForAttribute(  "msgevent",
                                 HL7MessageTransformOperation.OPERATION_NAME_QUALIFY,
                                 "MSH.9.2",
                                 HL7MessageTransformOperation.OPERAND_TYPE_STRING);
      addOperationForAttribute(  "messageevent",
                                 HL7MessageTransformOperation.OPERATION_NAME_QUALIFY,
                                 "MSH.9.2",
                                 HL7MessageTransformOperation.OPERAND_TYPE_STRING);

      addOperationForAttribute(  "sendingapplication",
                                 HL7MessageTransformOperation.OPERATION_NAME_QUALIFY,
                                 "MSH.3",
                                 HL7MessageTransformOperation.OPERAND_TYPE_STRING);

      addOperationForAttribute(  "sendingfacility",
                                 HL7MessageTransformOperation.OPERATION_NAME_QUALIFY,
                                 "MSH.4",
                                 HL7MessageTransformOperation.OPERAND_TYPE_STRING);

      addOperationForAttribute(  "receivingapplication",
                                 HL7MessageTransformOperation.OPERATION_NAME_QUALIFY,
                                 "MSH.5",
                                 HL7MessageTransformOperation.OPERAND_TYPE_STRING);

      addOperationForAttribute(  "receivingfacility",
                                 HL7MessageTransformOperation.OPERATION_NAME_QUALIFY,
                                 "MSH.6",
                                 HL7MessageTransformOperation.OPERAND_TYPE_STRING);

      addOperationForAttribute(  "ordercontrol",
                                 HL7MessageTransformOperation.OPERATION_NAME_QUALIFY,
                                 "ORC.1",
                                 HL7MessageTransformOperation.OPERAND_TYPE_STRING);
   } // parseAttributes


   private void addOperationForAttribute(String attribName, String opName, String designator, String operandType) {
      if (attributes.hasKey(attribName)) {
         addOperation(new HL7MessageTransformOperation(opName, designator)
                           .addOperand(operandType, attributes.get(attribName)));
      } // if
   } // addOperationForAttribute


   private void parseChildElements(Node xForm) throws IOException {
      NodeList kids = xForm.getChildNodes();
      int numKids = kids.getLength();
      for (int index = 0; index < numKids; ++index) {
         Node kid = kids.item(index);
         if (kid.getNodeType() == Node.TEXT_NODE) continue;

         String kidName = kid.getNodeName().toLowerCase();
         if (kidName.startsWith("#")) continue;


         if (HL7MessageTransformOperation.haveMethod(kidName)) {
            addOperation(new HL7MessageTransformOperation(kid) );
         } // if
      } // for
   } // parseChildElements


   public void addOperation(HL7MessageTransformOperation opern) {
      if (operations == null) operations = new ArrayList<HL7MessageTransformOperation>();
      operations.add(opern);
   } // addOperation
   

   /**
    * Creates a formatted dump of the context HL7Transform.
    */
   public void dump() {
      getLogger().debug("idStr:" + idString);
      for (HL7MessageTransformOperation op : operations) op.dump();
   } // dump
   
   
   /**
    * Checks the argument message against any qualifications specified by the context HL7Transform.
    * @param msg The argument HL7Message object.
    * @return false if the message fails to qualify against any of the qualifications, otherwise true.
    */
   public boolean isQualified(HL7Message msg) {
      if (operations == null) return true;

      for (HL7MessageTransformOperation op : operations) {
         if (op.isQualificationOperation() && !op.isQualified(msg))  return false;
      } // for

      return true;
   } // isQualified
   
   
   /**
    * Modifies the argument HL7Message object, in accordance with the 
    * non-qualification operations of the context HL7Transform.
    * @param msg The message to qualify and modify.
    * @return The modified message as a HL7Message object.
    * @throws java.lang.Exception
    */
   public HL7Message render(HL7Message msg) {
      if (msg == null) return null;

      HL7Message opMsg = new HL7Message(msg.toHL7String());

      for (HL7MessageTransformOperation op : operations) {
         if (op.isQualificationOperation()) continue;

         op.transform(opMsg);
      } // for

      return opMsg;
   } // render


   public static HL7Message render(HL7Message msg, String opName, String designatorStr) {
      return new HL7MessageTransformOperation(opName, designatorStr).transform(msg);
   } // render

   
   public static HL7Message render( HL7Message  msg,
                                    String      opName,
                                    String      designatorStr,
                                    String      opType,
                                    String      opValue) {
      return new HL7MessageTransformOperation(opName, designatorStr)
                  .addOperand(opType, opValue)
                  .transform(msg);
   } // render

} // HL7Transform
