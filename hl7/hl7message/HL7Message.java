/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7message;

import java.util.ArrayList;


/**
 *
 * @author scott
 */
public class HL7Message {
   private HL7Encoding           encoders;
   private ArrayList<HL7Segment> segments;
   private HL7SegmentMap         segmentMap;
   private static final int      CR = 0x0d;

   public HL7Message() { }


   public HL7Message(String hl7Msg) {
      this._parse(hl7Msg);
   } // HL7Message


   private HL7Encoding extractEncoding(String hl7Msg) {
      if (!(hl7Msg.startsWith("MSH") || hl7Msg.startsWith("BHS"))) {
         throw new IllegalArgumentException("HL7 Message contains neither MSH nor BHS.");
      } // if

      return new HL7Encoding(hl7Msg.substring(3, 8));
   } // extractEncoding


   public void addSegments(String segs) {
      if (segs == null) {
         return;
      } // if
      
      String[] segmentStrings = segs.split("\r");

      int segmentCount = segmentStrings.length;
      if (segmentCount > 0) {
         if (this.segmentMap == null) {
            this.segmentMap = new HL7SegmentMap();
         } // if

         if (this.segments == null) {
            this.segments = new ArrayList<HL7Segment>();
         } // if
      } // if

      for (int index = 0; index < segmentCount; ++index) {
         HL7Segment segment = new HL7Segment(segmentStrings[index], this.encoders);
         this.segments.add(segment);
         this.segmentMap.put(segment);
      } // for
   } // addSegments


   public void addSegment(HL7Segment seg) {
      if (seg == null) return;
      
      if (this.segmentMap == null) this.segmentMap = new HL7SegmentMap();
      if (this.segments == null) this.segments = new ArrayList<HL7Segment>();

      this.segments.add(seg);
      this.segmentMap.put(seg);
   } // addSegment

   private void _parse(String msg) {
      this.encoders = this.extractEncoding(msg);
      this.addSegments(msg);
   } // parse


   public void parse(String msg) { this._parse(msg); }

   public String toHL7String(HL7Encoding encode) {
      StringBuilder msgBuffer = new StringBuilder();
      for (HL7Segment seg : this.segments) {
         msgBuffer.append(seg.toHL7String(encode) ).append((char)CR);
      } // for

      return msgBuffer.toString();
   } // toHL7String


   public String toHL7String() {
      return this.toHL7String(this.encoders);
   } // toHL7String


   private HL7Segment pickSegment(String segID, int segIndex, boolean create) {
      ArrayList<HL7Segment> segs = this.segmentMap.get(segID);
      int index;
      if (segIndex != HL7Designator.UNSPECIFIED) {
         index = segIndex;
      } else {
         index = segs.size() - 1;
      } // if - else

      if (index >= segs.size()) {
         return null;
      } // if

      return segs.get(index);
   } // pickSegment


   private HL7Element pick(String segID, int segIndex, int sequence, int repetition, int component, int subComponent, boolean create) {
      HL7Segment segment = this.pickSegment(segID, segIndex, create);
      if (segment == null) {
         return null;
      } // if


      if (sequence == HL7Designator.UNSPECIFIED) {
         return segment;
      } // if

      HL7Field field = segment.pickSequence(sequence, create);
      if (field == null) {
         return null;
      } // if

      if (repetition == HL7Designator.UNSPECIFIED && component == HL7Designator.UNSPECIFIED) {
         return field;
      } // if

      HL7FieldRepetition hl7Rep = field.pickRepetition(repetition, create);
      if (hl7Rep == null) {
         return null;
      } // if

      if (component == HL7Designator.UNSPECIFIED) {
         return hl7Rep;
      } // if

      HL7Component hl7Comp = hl7Rep.pickComponent(component, create);
      if (hl7Comp == null) {
         return null;
      } // if

      if (subComponent == HL7Designator.UNSPECIFIED) {
         return hl7Comp;
      } // if

      return hl7Comp.pickSubComponent(subComponent, create);
   } // pick


   private HL7Element pick(HL7Designator designator) {
      return this.pick( designator.getSegID(),
                        designator.getSegIndex(),
                        designator.getSequence(),
                        designator.getRepetition(),
                        designator.getComponent(),
                        designator.getSubComponent(),
                        false);
   } // pick


   private HL7Element pick(HL7Designator designator, boolean create) {
      return this.pick( designator.getSegID(),
                        designator.getSegIndex(),
                        designator.getSequence(),
                        designator.getRepetition(),
                        designator.getComponent(),
                        designator.getSubComponent(),
                        create);
   } // pick


   public String get(HL7Designator designator) {
      HL7Element element = this.pick(designator);
      if (element == null) {
         return null;
      } // if
      
      return element.toHL7String(this.encoders);
   } // get


   public String get(String designatorStr) {
      return this.get(new HL7Designator(designatorStr));
   } // get

   
   public void set(HL7Designator designator, String valueStr) {
      this.pick(designator, true).set(valueStr, this.encoders);
   } // set


   public void set(String designatorStr, String valueStr) {
      this.set(new HL7Designator(designatorStr), valueStr);
   } // set


   boolean hasSegment(String segID) {
      if (segID == null || segID.length() < 3) {
         return false;
      } // if

      return (this.segmentMap.get(segID.substring(0, 3)) == null)
              ? false
              : true;
   } // hasSegment


   int countSegment(String segID) {
      if (segID == null || segID.length() < 3) {
         return 0;
      } // if

      ArrayList<HL7Segment> segs = this.segmentMap.get(segID);
      if (segs == null) {
         return 0;
      } // if

      return segs.size();
   } // countSegment


   public String controlID() {
      if (this.hasSegment("MSH")) {
         return this.get("MSH.10");
      } else if (this.hasSegment("BHS")) {
         return this.get("BHS.11");
      } // if - else if

      return "";
   } // controlID


   public void fresh() {
      String tStr = HL7Time.get().toString();
      if (this.hasSegment("MSH")) {
         this.set("MSH.7", tStr);
      } else if (this.hasSegment("BHS")) {
         this.set("BHS.7", tStr);
      } // if - else if
   } // fresh


   public HL7Message Acknowledgment(boolean ack,
                                    String errorCondition,
                                    HL7Designator errorLocation,
                                    String errorCode) {
      String   sendingApplication = null,
               sendingFacility = null,
               receivingApplication = null,
               receivingFacility = null,
               msgCtlID = null;

      if (this.hasSegment("MSH")) {
         sendingApplication = this.get("MSH.3");
         sendingFacility = this.get("MSH.4");
         receivingApplication = this.get("MSH.5");
         receivingFacility = this.get("MSH.6");
         msgCtlID = this.get("MSH.10");
      } else if (this.hasSegment("BHS")) {
         sendingApplication = this.get("BHS.3");
         sendingFacility = this.get("BHS.4");
         receivingApplication = this.get("BHS.5");
         receivingFacility = this.get("BHS.6");
         msgCtlID = this.get("BHS.11");
      } else if (this.hasSegment("BTS")) {
         sendingApplication = "";
         sendingFacility = "";
         receivingApplication = "";
         receivingFacility = "";
         msgCtlID = "";
      } else {
         ack = false;
         if (errorCondition == null) {
            errorCondition = "Not a valid message.";
         } else {
            errorCondition = new StringBuffer(errorCondition).append(" Not a valid message.").toString();
         }
      } // if - else if - else

      // create new message for acknowledgement
      HL7Message ackMsg = new HL7Message("MSH"
                                       + this.encoders.toString()
                                       + this.encoders.getFieldSeparator()
                                       + receivingApplication
                                       + this.encoders.getFieldSeparator()
                                       + receivingFacility
                                       + this.encoders.getFieldSeparator()
                                       + sendingApplication
                                       + this.encoders.getFieldSeparator()
                                       + sendingFacility
                                       + this.encoders.getFieldSeparator());
      // Message DateTime
      String now = HL7Time.get();
      ackMsg.set("MSH.7", now);

      // Message type
      ackMsg.set("MSH.9.1", "ACK");
      ackMsg.set("MSH.9.2", "");
      String msgType = this.get("MSH.9");
      if (msgType == null) {
         ack = false;
         if (errorCondition == null) {
            errorCondition = "Message Type not defined.";
         } else {
            errorCondition = new StringBuffer(errorCondition).append(" Message Type not defined.").toString();
         }
      } // if

      // Control ID
      StringBuilder   msgCtlIDStr = new StringBuilder();
      if (msgCtlID == null) {
         msgCtlID = "";
      } // if
      msgCtlIDStr.append(msgCtlID);
      msgCtlIDStr.append(".AK");
      ackMsg.set("MSH.10", msgCtlIDStr.toString());

      // build MSA
      String ackCode;
      if (ack == true) {
         ackCode = "AA";
      } else {
         ackCode = "AE";
      } // if - else

      StringBuilder msaSegmentBuffer = new StringBuilder();
      msaSegmentBuffer.append("MSA");
      msaSegmentBuffer.append(ackMsg.encoders.getFieldSeparator());
      msaSegmentBuffer.append(ackCode);
      msaSegmentBuffer.append(ackMsg.encoders.getFieldSeparator());
      msaSegmentBuffer.append(msgCtlID);
      msaSegmentBuffer.append(ackMsg.encoders.getFieldSeparator());
      msaSegmentBuffer.append(ackMsg.encoders.getFieldSeparator());
      // msaSegmentBuffer.append("\r");
      ackMsg.set("MSA", msaSegmentBuffer.toString());

      if (!msgType.startsWith("NMQ") ) {
         return (ackMsg);
      } // if

      // If it's a NMQ add the NCK segment.
      StringBuilder nckSegmentBuffer = new StringBuilder();
      nckSegmentBuffer.append("NCK");
      nckSegmentBuffer.append(ackMsg.encoders.getFieldSeparator());
      nckSegmentBuffer.append(now);
      nckSegmentBuffer.append(ackMsg.encoders.getFieldSeparator());
      // nckSegmentBuffer.append("\r");
      ackMsg.set("NCK", nckSegmentBuffer.toString());

      ackMsg.set("MSH.9.1", "NMR");
      ackMsg.set("MSH.9.2", "");

      return(ackMsg);

   } // Acknowledgment


   public String idString() {
      StringBuilder idStrBuffer = new StringBuilder();

      if (this.hasSegment("MSH")) {
         idStrBuffer.append(this.get("MSH.3"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("MSH.4"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("MSH.5"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("MSH.6"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("MSH.7"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("MSH.9.1"));

         String eventType = this.get("MSH.9.2");
         if (eventType != null && !eventType.isEmpty() ) {
            idStrBuffer.append(this.encoders.getFieldSeparator());
            idStrBuffer.append(eventType);
         } // if

         String ctlID = this.get("MSH.10");
         if (ctlID != null && !ctlID.isEmpty() ) {
            idStrBuffer.append(this.encoders.getFieldSeparator());
            idStrBuffer.append(ctlID);
         } // if
      } else if (this.hasSegment("BHS")) {
         idStrBuffer.append(this.get("BHS.3"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("BHS.4"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("BHS.5"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("BHS.6"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("BHS.7"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("BHS.9"));
         idStrBuffer.append(this.encoders.getFieldSeparator());
         idStrBuffer.append(this.get("BHS.11"));
      } else if (this.hasSegment("BHS")) {
         idStrBuffer.append("BTS");
      } else {
         idStrBuffer.append("???");
      }
      return(idStrBuffer.toString() );
   } // idString

   
   void remove(String location) {
      this.set(location, "");
   } // remove


   void swap(String location1, String location2) {
      String str1 = this.get(location1);
      String str2 = this.get(location2);
      this.set(location1, str2);
      this.set(location2, str1);
   } // swap


   public String toXMLString() {
      String tag = "HL7Message";
      StringBuffer returnBuffer =  new StringBuffer("<").append(tag).append(">");
      
      for (HL7Segment segment : this.segments) {
         returnBuffer.append(segment.getID().equals("MSH")
                           ? segment.toXMLString(this.encoders)
                           : segment.toXMLString());
      } // for

      returnBuffer.append("</").append(tag).append(">");
      return returnBuffer.toString();
   } // toXMLString

   public void addSegments(ArrayList<HL7Segment> segments) {
      if (segments == null) return;
      for (HL7Segment seg : segments) this.addSegment(seg);
   } // addSegments



} // HL7Message
