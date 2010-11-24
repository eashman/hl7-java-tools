/*
 *  $Id$
 *  $HeadURL$
 *
 *  This code is derived from public domain sources.
 *  Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7FieldRepetition.java : Provides access to HL7 message field repetition level items.
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
 *
 * @author scott
 */
public class HL7FieldRepetition implements HL7Element {
   private   HL7ElementLevel              level;
   protected ArrayList<HL7Component>      components = null;
   private boolean                        touched;


   public HL7FieldRepetition() {
      level = new HL7ElementLevel(HL7ElementLevel.REPETITION);
   } // HL7FieldRepetition


   public HL7FieldRepetition(String repetitionStr, HL7Encoding encoders) {
      this();
      _set(repetitionStr, encoders);
   } // HL7FieldRepetition


   public boolean hasComponent(int index) {
      return hasComponents()
         &&  index >= 0
         &&  index < components.size();
   } // hasSubComponent


   public HL7Component getComponent(int index) {
      if (hasComponent(index)) return components.get(index);
      return null;
   } // getComponent


   public HL7Element getElement(int index) {
      return getComponent(index);
   } // getElement

   
   public int getLevel() {
      return level.get();
   } // getLevel


   public boolean wasTouched() {
      return touched;
   } // wasTouched


   private void _set(String msgText, HL7Encoding encoders) {
      HL7ElementLevel nextLevel = new HL7ElementLevel(HL7ElementLevel.COMPONENT);
      ArrayList<String>  elements = encoders.hl7Split(msgText, nextLevel);
      components = new ArrayList<HL7Component>();
      for (String elementStr : elements) {
         HL7Component element = new HL7Component(elementStr, encoders);
         components.add(element);
      } // for

      touched = true;
   } // set


   public void set(String msgText, HL7Encoding encoders) { this._set(msgText, encoders); }


   public String toHL7String(HL7Encoding encoders) {
      if (!hasComponents()) return "";

      ArrayList<String> compStrings = new ArrayList<String>();
      for (HL7Component comp : components) compStrings.add(comp.toHL7String(encoders));

      return encoders.hl7Join(compStrings, level.next());
   } // toString


   public String toXMLString(int repIndex) {
      if (!hasContent()) return "";

      String tag = "Repetition";
      StringBuffer returnBuffer =  new StringBuffer("<")
              .append(tag)
              .append(" id=\"")
              .append(Integer.toString(repIndex))
              .append("\">");

      if (hasSimpleContent()) {
         returnBuffer.append(getSimpleContent());
      } else {
         int componentDesignator = 1;
         for (HL7Component component : components) {
            if (component.hasContent() ) {
               returnBuffer.append(component.toXMLString(componentDesignator));
            } // if
            ++componentDesignator;
         } // for
      } // if - else

      returnBuffer.append("</").append(tag).append(">");
      return returnBuffer.toString();
   } // toXMLString


   public boolean hasContent() {
      if (hasComponents()) {
         for (HL7Component comp : components) if (comp.hasContent()) return true;
      } // if

      return false;
   } // hasContent


   public boolean hasSimpleContent() {
      if (hasComponents() ) {
         if (  components.size() < 2
         &&    components.get(0).hasSimpleContent()) {
            return true;
         } // if
      } // if

      return false;
   } // hasSimpleContent


   public String getSimpleContent() {
      if (hasSimpleContent()) return components.get(0).getSimpleContent();
      return "";
   } // if


   public boolean hasComponents() {
      return components != null && !components.isEmpty();
   } // hasComponents


   HL7Component pickComponent(int componentIndex, boolean create) {
      if (!hasComponent(componentIndex)) {
         if (!create) return null;

         for (int newIndex = componentCount(); newIndex <= componentIndex; ++newIndex) {
            addComponent();
         } // for
      } // if

      return getComponent(componentIndex);
   } // pickComponent


   private int componentCount() {
      if (!this.hasComponents()) return 0;
      return components.size();
   } // componentCount


   private void addComponent() {
      if (components == null) components = new ArrayList<HL7Component>();
      components.add(new HL7Component());
   } // addComponent

   public void addComponent(int index) {
      if (hasComponent(index)) return;

      if (components == null) components = new ArrayList<HL7Component>();
      while (index >= components.size()) addComponent();
   } // addComponent

} // HL7FieldRepetition
