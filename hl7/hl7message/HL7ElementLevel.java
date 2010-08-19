/*
 *  $Id$
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

import java.util.NoSuchElementException;

/**
 *
 * @author scott
 */
public class HL7ElementLevel {
   public static final int SEGMENT      = 1,
                           FIELD        = 2,
                           REPETITION   = 3,
                           COMPONENT    = 4,
                           SUBCOMPONENT = 5;
   private int value;

   public HL7ElementLevel(int level) {
      this._set(level);
   } // HL7ElementLevel
   
   private void _set(int level) {
      if (level < HL7ElementLevel.SEGMENT || level > HL7ElementLevel.SUBCOMPONENT) {
         throw new IllegalArgumentException("Illegal level:" + Integer.toString(level));
      } // if

      value = level;
   } // set

   public void set(int level) { this._set(level); }

   public int get() {
      return this.value;
   } // get

   public boolean hasNext() {
      if (this.value > COMPONENT) {
         return false;
      } // if

      return true;
   } // hasNext

   public HL7ElementLevel next() throws NoSuchElementException {
      if (this.hasNext()) {
         return new HL7ElementLevel(this.value + 1);
      } // if

      throw new NoSuchElementException();
   } // next
} // HL7ElementLevel

