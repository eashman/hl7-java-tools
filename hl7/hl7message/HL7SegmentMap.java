/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.conxio.hl7.hl7message;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author scott
 */
public class HL7SegmentMap {
   private HashMap segmentHash;

   public HL7SegmentMap() {
      this.segmentHash = new HashMap();
   } // HL7SegmentMap


   public ArrayList<HL7Segment> get(String argStr) {
      return (ArrayList<HL7Segment>)this.segmentHash.get(argStr.substring(0, 3));
   } // get


   public void put(HL7Segment segment) {
      if (segment == null) { return; }

      String idStr = segment.getID();
      ArrayList<HL7Segment> segments = this.get(idStr);
      if (segments != null && !segments.isEmpty()) {
         segments.add(segment);
         return;
      } // if

      segments = new ArrayList<HL7Segment>();
      segments.add(segment);
      this.segmentHash.put(idStr, segments);
   } // put


} // HL7SegmentMap
