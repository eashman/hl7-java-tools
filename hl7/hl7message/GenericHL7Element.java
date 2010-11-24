/*
 *  $Id$
 *  $HeadURL$
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  GenericHL7Element.java : An class for generic HL7 message elements,
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

/**
 * Note: This class is currently unused.
 * @author scott
 */
class GenericHL7Element extends AbstractHL7Element implements HL7Element {

   public GenericHL7Element(HL7ElementLevel level) {
      super(level.get());
   } // GenericHL7Element constructor

} // GenericHL7Element
