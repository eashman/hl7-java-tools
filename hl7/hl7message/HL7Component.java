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
      level = new HL7ElementLevel(HL7ElementLevel.COMPONENT);
   } // HL7Component


   public HL7Component(String componentStr, HL7Encoding encoders) {
      this();
      _set(componentStr, encoders);
   } // HL7Component


   public boolean wasTouched() {
      return touched;
   } // wasTouched


   private void _set(String msgText, HL7Encoding encoders) {
      subComponents = new ArrayList<HL7SubComponent>();
      touched = true;

      if (StringUtils.isEmpty(msgText)) return;

      HL7ElementLevel nextLevel = new HL7ElementLevel(HL7ElementLevel.SUBCOMPONENT);
      ArrayList<String>  subComps = encoders.hl7Split(msgText, nextLevel);   
      for (String subCompStr : subComps) subComponents.add(new HL7SubComponent(subCompStr));
   } // set


   public void set(String msgText, HL7Encoding encoders) {
      _set(msgText, encoders);
   } // set


   public String toHL7String(HL7Encoding encoders) {
      if (!hasSubComponents()) return "";

      ArrayList<String> elementStrings = new ArrayList<String>();
      for (HL7Element element : subComponents) elementStrings.add(element.toHL7String(encoders));

      return encoders.hl7Join(elementStrings, level.next());
   } // toString


   public String toXMLString(int componentIndex) {
      if (!hasContent())    return "";

      String tag = "Component";
      StringBuffer returnBuffer =  new StringBuffer("<")
              .append(tag)
              .append(" id=\"")
              .append(Integer.toString(componentIndex))
              .append("\">");

      if (hasSimpleContent()) {
         returnBuffer.append(getSimpleContent());
      } else {
         int subComponentIndex = 1;
         for (HL7SubComponent subComponent : subComponents) {
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
      return getSubComponent(index);
   } // getElement


   public boolean hasContent() {
      if (hasSubComponents()) {
         for (HL7SubComponent subComp : subComponents) if (subComp.hasContent()) return true;
      } // if

      return false;
   } // hasContent


   public boolean hasSimpleContent() {
      if (hasSubComponents() ) {
         if (  subComponents.size() < 2
         &&    subComponents.get(0).hasContent()) {
            return true;
         } // if
      } // if

      return false;
   } // hasSimpleContent


   public String getSimpleContent() {
      if (hasSimpleContent()) return subComponents.get(0).getContent();
      return "";
   } // getSimpleContent


   public boolean hasSubComponents() {
      return subComponents != null && !subComponents.isEmpty();
   } // hasSubComponents


   public boolean hasSubComponent(int index) {
      return hasSubComponents()
          && index >= 0
          && index < subComponents.size();
   } // hasSubComponent


   public HL7SubComponent getSubComponent(int index) {
      if (hasSubComponent(index)) return subComponents.get(index);
      return null;
   } // getSubComponent


   HL7SubComponent pickSubComponent(int subComponentIndex, boolean create) {
      if (!hasSubComponent(subComponentIndex)) {
         if (!create) return null;

         for (int index = subComponentCount(); index <= subComponentIndex; ++index) {
            addSubComponent();
         } // for
      } // if

      return getSubComponent(subComponentIndex);
   } // pickSubComponent


   private int subComponentCount() {
      if (subComponents == null) return 0;
      return subComponents.size();
   } // subComponentCount
   

   private void addSubComponent() {
      if (subComponents == null) subComponents = new ArrayList<HL7SubComponent>();
      subComponents.add(new HL7SubComponent());
   } // addSubComponent

} // HL7Component
