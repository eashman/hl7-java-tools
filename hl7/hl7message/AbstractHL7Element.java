/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  AbstractHL7Element.java : An abstract class for HL7 Message elements,
 *  providing structured access to message data content, and constituent items.
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

/**
 * Note: This class is currently unused.
 *
 * @author scott
 */
abstract class AbstractHL7Element implements HL7Element {
   private   HL7ElementLevel        level;
   protected ArrayList<HL7Element>  constituents = null;
   protected String                 content;
   private boolean                  touched;

   
   AbstractHL7Element(int levelArg) {
      level = new HL7ElementLevel(levelArg);
   } // AbstractHL7Element


   void setLevel(int levelArg) {
      level.set(levelArg);
   } // setLevel


   int getLevel() {
      return level.get();
   } // getLevel


   boolean wasTouched() {
      return touched;
   } // wasTouched


   public void set(String msgText, HL7Encoding encoders) {
      HL7ElementLevel nextLevel = null;
      if (level.hasNext() ) {
         nextLevel = level.next();
      } else {
         content = msgText;
         return;
      } // if - else

      ArrayList<String>  elements = encoders.hl7Split(msgText, nextLevel);
      constituents = new ArrayList<HL7Element>();
      for (String elementStr : elements) {
         HL7Element element = HL7ElementLevel.newElementAt(nextLevel);
         element.set(elementStr, encoders);
         constituents.add(element);
      } // for
   } // set


   public String toHL7String(HL7Encoding encoders) {
      if (!(this.content == null || this.content.isEmpty())) {
         return this.content;
      } // if

      if (this.constituents == null || this.constituents.isEmpty()) {
         return "";
      } // if

      ArrayList<String> elementStrings = new ArrayList<String>();
      for (HL7Element element : this.constituents) {
         elementStrings.add(element.toHL7String(encoders));
      } // for

      return encoders.hl7Join(elementStrings, this.level);
   } // toString


   public HL7Element getElement(int index) {
      if (this.constituents == null || this.constituents.isEmpty()) {
         return null;
      } // if

      return this.constituents.get(index);
   } // getElement


   public boolean hasContent() {
      if (this.content != null) {
         return true;
      } // if

      return false;
   } // hasContent


   public boolean hasConstituents() {
      if (this.constituents != null && this.constituents.size() > 0) {
         return true;
      } // if

      return false;
   } // hasConstituents

   public String getContent() {
      return this.content;
   } // getContent
   
} // AbstractHL7Element