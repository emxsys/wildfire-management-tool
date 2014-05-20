/*
 * Copyright (c) 2011-2012, Bruce Schubert. <bruce@emxsys.com> 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * - Neither the name of Bruce Schubert, Emxsys nor the names of its 
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
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
package com.emxsys.wmt.cps.data;

import com.emxsys.visad.filetype.NetCdfDataNode;
import javax.swing.Action;
import org.openide.util.Lookup;


/**
 * A DataNode subclass that represents a TerrainDataObject.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: TerrainDataNode.java 689 2013-05-27 15:29:33Z bdschubert $
 */
public class TerrainDataNode extends NetCdfDataNode
{
    private TerrainDataObject terrainDataObject;


    /**
     * Constructor for a Node representing a Terrain NetCDF file.
     *
     * @param dataObject for a FlatField that backs this node
     * @param cookieSet the DataObject's cookieSet that manages the SaveCookie capability
     */
    public TerrainDataNode(TerrainDataObject dataObject, Lookup cookieSet)
    {
        super(dataObject, cookieSet);
        this.terrainDataObject = dataObject;
    }


    @Override
    public Action[] getActions(boolean context)
    {
        return super.getActions(context);
    }
}
