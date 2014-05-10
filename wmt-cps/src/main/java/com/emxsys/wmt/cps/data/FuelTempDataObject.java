/*
 * Copyright (c) 2013, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.cps.data;

import com.emxsys.wmt.visad.filetype.NetCdfDataObject;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;


/**
 * The FuelTempDataObject is a specialized NetCdfDataObject that overrides the icon.
 *
 * @author Bruce Schubert
 */
@Messages(
    {
    "LBL_FuelTemp_LOADER=Files of Fuel Temperature"
})
// Using a complex MIME resolver which overrides default NetCDF .nc extention mimeResolver.
@MIMEResolver.Registration(
    displayName = "#LBL_FuelTemp_LOADER",
    resource = "FiregroundResolver.xml",
    position = 200)
@DataObject.Registration(
    mimeType = "application/x-fuel_temperature",
    iconBase = "com/emxsys/wmt/cps/images/Fire_Location.png",
    displayName = "#LBL_FuelTemp_LOADER",
    position = 300)
@ActionReferences(
    {
    @ActionReference(
        path = "Loaders/application/x-fuel_temperature/Actions",
        id =
        @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
        position = 100,
        separatorAfter = 200),
    @ActionReference(
        path = "Loaders/application/x-fuel_temperature/Actions",
        id =
        @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
        position = 300),
    @ActionReference(
        path = "Loaders/application/x-fuel_temperature/Actions",
        id =
        @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
        position = 400,
        separatorAfter = 500),
    @ActionReference(
        path = "Loaders/application/x-fuel_temperature/Actions",
        id =
        @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
        position = 600),
    @ActionReference(
        path = "Loaders/application/x-fuel_temperature/Actions",
        id =
        @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
        position = 700,
        separatorAfter = 800),
    @ActionReference(
        path = "Loaders/application/x-fuel_temperature/Actions",
        id =
        @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
        position = 900,
        separatorAfter = 1000),
    @ActionReference(
        path = "Loaders/application/x-fuel_temperature/Actions",
        id =
        @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
        position = 1100,
        separatorAfter = 1200),
    @ActionReference(
        path = "Loaders/application/x-fuel_temperature/Actions",
        id =
        @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
        position = 1300),
    @ActionReference(
        path = "Loaders/application/x-fuel_temperature/Actions",
        id =
        @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
        position = 1400)
})
public class FuelTempDataObject extends NetCdfDataObject
{
    private static final long serialVersionUID = 20130506L;
    private static final Logger logger = Logger.getLogger(FuelTempDataObject.class.getName());
    private static final RequestProcessor EXECUTOR = new RequestProcessor(FuelTempDataObject.class);


    static
    {
        logger.setLevel(Level.ALL);
    }


    /**
     * Constructor that proxies the cookieSet lookup with a dynamic lookup containing the FlatField.
     * The file contents are read in a worker thread which adds the FlatField object to the dynamic
     * lookup when it is finished.
     *
     * @param primaryFile representing FlatField
     * @param loader for FlatField
     * @throws DataObjectExistsException
     * @throws IOException
     */
    public FuelTempDataObject(FileObject primaryFile, MultiFileLoader loader) throws
        DataObjectExistsException, IOException
    {
        super(primaryFile, loader);
        registerEditor("application/x-fuel_temperature", false);
    }


    @Override
    protected Node createNodeDelegate()
    {
        return new FuelTempDataNode(this, super.lookup); // Don't call super.getLookup() else recursion
    }
}
