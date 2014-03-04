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
package com.emxsys.wmt.visad.filetype;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import javax.swing.Action;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import visad.DataImpl;

/**
 * This DataNode subclass represents a NetCDF DataObject.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: NetCdfDataNode.java 635 2013-05-14 13:26:28Z bdschubert $
 */
public class NetCdfDataNode extends DataNode {

    private NetCdfDataObject ncDataObject;
    private String nodeType = "";
    private final InstanceContent content;
    private LookupListener lookupListener = new SimpleLookupListener();
    private PropertyChangeListener propertyListener = new SimplePropertyChangeListener();
    private final Lookup.Result<DataImpl> lookupResult;

    /**
     * Constructor for a Node representing a NetCDF file.
     *
     * @param dataObject for a FlatField that backs this node
     * @param cookieSet the DataObject's cookieSet that manages the SaveCookie capability
     */
    public NetCdfDataNode(NetCdfDataObject dataObject, Lookup cookieSet) {
        this(dataObject, cookieSet, new InstanceContent());
    }

    /**
     * Constructs the node with a proxy lookup comprised of the DataObject's cookieSet and this
     * node's capabilities.
     *
     * @param dataObject with FlatField to be represented by this node
     * @param cookieSet lookup from DataObject that provides Savable capability, DataObject and
     * FlatField
     * @param content dynamic content for this node's lookup.
     */
    private NetCdfDataNode(NetCdfDataObject dataObject, Lookup cookieSet, InstanceContent content) {
        super(dataObject, Children.LEAF, new ProxyLookup(new AbstractLookup(content), cookieSet));
        this.ncDataObject = dataObject;
        this.content = content;

        // Used in case we need to know when a DataImpl is added to the DataObject
        this.lookupResult = cookieSet.lookupResult(DataImpl.class);
        this.lookupResult.addLookupListener(lookupListener);
        this.lookupListener.resultChanged(null);

        // Listen for changes in state
        dataObject.addPropertyChangeListener(WeakListeners.propertyChange(propertyListener, dataObject));
    }

    @Override
    public String getDisplayName() {
        return getName() + " : " + this.nodeType;
    }

    /**
     * Makes the name bold when it needs to be saved.
     *
     * @return a stylized name if the underlying DataObject is modified.
     */
    @Override
    public String getHtmlDisplayName() {
        // The ExplorerView first calls this method for a node name,
        // then it calls getDisplyName if null is returned.
        SaveCookie capability = getLookup().lookup(SaveCookie.class);
        if (capability != null) {
            return "<b>" + getDisplayName() + "</b>"; //NOI18N
        }
        return null;
    }

    @Override
    public Action[] getActions(boolean context) {
        return super.getActions(context);
//        return new Action[]
//        {
//        };
    }

    private class SimpleLookupListener implements LookupListener {

        @Override
        public void resultChanged(LookupEvent ignored) {
            Collection<? extends DataImpl> instances = lookupResult.allInstances();
            DataImpl dataImpl = instances.isEmpty() ? null : instances.iterator().next();
            nodeType = dataImpl == null ? "No Data" : dataImpl.getType().prettyString();
            // Fire event that triggers getHtmlDisplayName 
            fireDisplayNameChange(null, null);
        }
    }

    private class SimplePropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(NetCdfDataObject.PROP_DATA_STATE)) {
                DataState state = (DataState) evt.getNewValue();
                if (state == DataState.INITIALIZED) {
                    nodeType = ncDataObject.getData().getType().prettyString();
                }
                else {
                    nodeType = state.toString();
                }
                // Fire event that triggers getHtmlDisplayName 
                fireDisplayNameChange(null, null);
            }
        }
    }
}
