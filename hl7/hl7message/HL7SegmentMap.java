/*
 *  $Id$
 *
 *  This code is derived from public domain sources.
 *  Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7SegmentMap.java : Provides keyed access to parsed HL7 message segment data.
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
