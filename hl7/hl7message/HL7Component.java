/*
 *  $Id$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7Component.java : An class for HL7 message component level elements,
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
import org.apache.commons.lang.StringUtils;
/**
 *
 * @author scott
 */
public class HL7Component implements HL7Element {
   private   HL7ElementLevel              level;
   protected ArrayList<HL7SubComponent>   subComponents = null;
   private boolean                        touched;


   public HL7Component() {
      this.level = new HL7ElementLevel(HL7ElementLevel.COMPONENT);
   } // HL7Component


   public HL7Component(String componentStr, HL7Encoding encoders) {
      this();
      this._set(componentStr, encoders);
   } // HL7Component


   public void setLevel(int level) {
      this.level.set(level);
   } // setLevel


   public int getLevel() {
      return this.level.get();
   } // getLevel


   public boolean wasTouched() {
      return this.touched;
   } // wasTouched


   private void _set(String msgText, HL7Encoding encoders) {
      this.subComponents = new ArrayList<HL7SubComponent>();
      this.touched = true;

      if (StringUtils.isEmpty(msgText)) return;

      HL7ElementLevel nextLevel = new HL7ElementLevel(HL7ElementLevel.SUBCOMPONENT);
      ArrayList<String>  subComps = encoders.hl7Split(msgText, nextLevel);
      
      for (String elementStr : subComps) {
         HL7SubComponent element = new HL7SubComponent();
         element.set(elementStr, encoders);
         this.subComponents.add(element);
      } // for
   } // set


   public void set(String msgText, HL7Encoding encoders) {
      this._set(msgText, encoders);
   } // set


   public String toHL7String(HL7Encoding encoders) {
      if (!this.hasConstituents()) {
         return "";
      } // if

      ArrayList<String> elementStrings = new ArrayList<String>();
      for (HL7Element element : this.subComponents) {
         elementStrings.add(element.toHL7String(encoders));
      } // for

      return encoders.hl7Join(elementStrings, this.level.next());
   } // toString


   public String toXMLString(int componentIndex) {
      if (!this.hasContent())    return "";

      String tag = "Component";
      StringBuffer returnBuffer =  new StringBuffer("<")
              .append(tag)
              .append(" id=\"")
              .append(Integer.toString(componentIndex))
              .append("\">");

      if (this.hasSimpleContent()) {
         returnBuffer.append(this.getSimpleContent());
      } else {
         int subComponentIndex = 1;
         for (HL7SubComponent subComponent : this.subComponents) {
            if (subComponent.hasContent() ) {
               returnBuffer.append(subComponent.toXMLString(subComponentIndex));
            } // if
            ++subComponentIndex;
         } // for
      } // if - else

      returnBuffer.append("</").append(tag).append(">");
      return returnBuffer.toString();
   } // toXMLString


   public HL7Element getElement(int index) {
      return this.getSubComponent(index);
   } // getElement


   public boolean hasContent() {
      if (this.hasConstituents()) {
         for (HL7SubComponent subComp : this.subComponents) {
            if (subComp.hasContent()) {
               return true;
            } // if
         } // if
      } // if

      return false;
   } // hasContent


   public boolean hasSimpleContent() {
      if (this.hasConstituents() ) {
         if (this.subComponents.size() < 2 && this.subComponents.get(0).hasContent()) {
            return true;
         } // if
      } // if

      return false;
   } // hasSimpleContent


   public String getSimpleContent() {
      if (this.hasSimpleContent()) {
         return this.subComponents.get(0).getContent();
      } // if

      return "";
   }


   public boolean hasConstituents() {
      if (this.subComponents == null || this.subComponents.isEmpty()) {
         return false;
      } // if

      return true;
   } // hasConstituents


   public String getContent() {
      return null;
   } // getContent


   public boolean hasSubComponent(int index) {
      if (  this.subComponents == null
      ||    index < 0
      ||    index >= this.subComponents.size()) {
         return false;
      } // if

      return true;
   } // hasSubComponent


   public HL7SubComponent getSubComponent(int index) {
      if (this.hasSubComponent(index)) {
         return this.subComponents.get(index);
      } // if

      return null;
   } // getSubComponent


   HL7Element pickSubComponent(int subComponent, boolean create) {
      if (!this.hasSubComponent(subComponent)) {
         if (!create) {
            return null;
         } // if

         for (int index = this.subComponentCount(); index <= subComponent; ++index) {
            this.addSubComponent();
         } // for
      } // if

      return this.getSubComponent(subComponent);
   } // pickSubComponent


   private int subComponentCount() {
      if (this.subComponents == null) {
         return 0;
      } // if

      return this.subComponents.size();
   } // subComponentCount
   

   private void addSubComponent() {
      if (this.subComponents == null) {
         this.subComponents = new ArrayList<HL7SubComponent>();
      } // if

      this.subComponents.add(new HL7SubComponent());
   } // addSubComponent

} // HL7Component
