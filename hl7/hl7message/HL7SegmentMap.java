/*
 *  $Id$
 *  $HeadURL$
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
 * A hashed multi map class for indexing HL7 segments by ID.
 * @author scott
 */
class HL7SegmentMap {
   private HashMap segmentHash;

   HL7SegmentMap() {
      segmentHash = new HashMap();
   } // HL7SegmentMap


   /**
    * Retrieves the entry corresponding to the argument string.
    * @param argStr A segment ID string
    * @return the entry corresponding to the argument string.
    */
   ArrayList<HL7Segment> get(String argStr) {
      return (ArrayList<HL7Segment>)segmentHash.get(argStr.substring(0, 3));
   } // get


   /**
    * Indexes the argument HL7Segment in the map.
    * @param segment The HL7Segment object to be indexed.
    */
   void put(HL7Segment segment) {
      if (segment == null) return; 

      String idStr = segment.getID();
      ArrayList<HL7Segment> segments = get(idStr);
      if (segments != null && !segments.isEmpty()) {
         segments.add(segment);
         return;
      } // if

      segments = new ArrayList<HL7Segment>();
      segments.add(segment);
      segmentHash.put(idStr, segments);
   } // put


   /**
    * Determines whether the map contains one or more entries corresponding to
    * the argument key.
    * @param key The segment ID key.
    * @return true if the map contains one or more entries corresponding to the
    * argument key. Otherwise false.
    */
   boolean has(String key) {
      return segmentHash.containsKey(key);
   } // has


   /**
    * Determines whether the map has any entries.
    * @return true if the map contains one or more entries. Otherwise false.
    */
   boolean isEmpty() {
      return segmentHash.isEmpty();
   } // isEmpty

} // HL7SegmentMap
