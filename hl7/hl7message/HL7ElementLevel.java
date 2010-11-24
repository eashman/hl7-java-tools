/*
 *  $Id$
 *  $HeadURL$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7ElementLevel.java : A class of bounded ordered HL7 hierarchy levels.
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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 *
 * @author scott
 */
class HL7ElementLevel implements Iterator {
   public static final int SEGMENT      = 1,
                           FIELD        = 2,
                           REPETITION   = 3,
                           COMPONENT    = 4,
                           SUBCOMPONENT = 5;
   private int value;

   public HL7ElementLevel(int level) {
      _set(level);
   } // HL7ElementLevel
   
   private void _set(int level) {
      if (level < HL7ElementLevel.SEGMENT || level > HL7ElementLevel.SUBCOMPONENT) {
         throw new IllegalArgumentException("Illegal level:" + Integer.toString(level));
      } // if

      value = level;
   } // set

   void set(int level) { _set(level); }

   int get() {
      return value;
   } // get

   public boolean hasNext() {
      if (value > COMPONENT) return false;
      return true;
   } // hasNext

   public HL7ElementLevel next() throws NoSuchElementException {
      if (hasNext()) return new HL7ElementLevel(value + 1);
      throw new NoSuchElementException();
   } // next

   public void remove() {
      throw new UnsupportedOperationException("Not supported.");
   } // remove

   static HL7Element newElementAt(HL7ElementLevel level) {
      switch (level.value) {
         case SEGMENT      : return new HL7Segment();
         case FIELD        : return new HL7Field();
         case REPETITION   : return new HL7FieldRepetition();
         case COMPONENT    : return new HL7Component();
         case SUBCOMPONENT : return new HL7SubComponent();
         default: throw new IllegalArgumentException("No such HL7 element level:"
                                                   + Integer.toString(level.value));
      } // switch
   } // newElementAt

} // HL7ElementLevel

