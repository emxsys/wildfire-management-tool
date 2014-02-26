/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     - Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     - Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.emxsys.wmt.util;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import org.openide.loaders.DataObject;


/**
 *
 * @author Bruce Schubert
 * @version $Id: DragDropUtil.java 457 2012-12-18 02:06:21Z bdschubert $
 */
public class DragDropUtil
{

    /**
     * Looks for a DataObject in a drag-n-drop Transferable object.
     *
     * @param trnsfr containing a DataFlavor matching {@code application/x-java-openide-dataobjectdnd}
     * @return the DataObject if found, else null
     */
    public static DataObject findDataObject(Transferable trnsfr)
    {
        // The following MIME type and representation class was hard to figure out.
        // I had to set a breakpoint here and examine the Transferable's contents 
        // to discover it. Note the representation class and the MIME type has to be 
        // supplied to the DataFlavor else it won't match the Transfereable's DataFlavor.
        DataFlavor DATA_OBJECT = new DataFlavor("application/x-java-openide-dataobjectdnd;"
            + "class=org.openide.loaders.DataObject", null);
        if (trnsfr.isDataFlavorSupported(DATA_OBJECT))
        {
            try
            {
                return (DataObject) trnsfr.getTransferData(DATA_OBJECT);
            }
            catch (Exception ex)
            {
            }
        }
        return null;
    }
}
