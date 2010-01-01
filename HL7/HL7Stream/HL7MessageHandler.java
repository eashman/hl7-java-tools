/*
 *  $Id: HL7MessageHandler.java 66 2009-12-23 20:15:19Z scott $
 *
 *  This code is derived from public domain sources. Commercial use is allowed.
 *  However, all rights remain permanently assigned to the public domain.
 *
 *  HL7MessageHandler.java : An HL7 message dispatcher interface.
 *
 *  Copyright (C) 2009  Scott Herman
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

package us.conxio.HL7.HL7Stream;

/**
 *
 * @author scott herman <scott.herman@unconxio.us>
 */
import us.conxio.HL7.HL7Message.*;

/**
 * A interface for HL7Message dispatch handlers.
 * @author hermans
 */
public interface HL7MessageHandler {
   /**
    * The only required method is dispatch, which may utilize the argumnet
    * HL7Message object in any way.
    * @param msg a reference to the argument HL7Message object.
    * @return a count of 0 or more dispatch occurrences related to the context
    * dispatch invocation.
    * @throws us.conxio.HL7.HL7Stream.HL7IOException
    */
   public int dispatch(HL7Message msg) throws HL7IOException;
} // HL7MessageHandler
