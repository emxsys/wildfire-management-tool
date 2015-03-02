/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.symbology;

import com.emxsys.gis.api.symbology.Graphic;
import com.emxsys.gis.api.symbology.StandardIdentity;
import com.emxsys.gis.api.symbology.Status;
import com.emxsys.util.FilenameUtils;
import com.emxsys.wmt.core.capabilities.*;
import com.emxsys.wmt.globe.symbology.BasicGraphic;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.api.actions.Editable;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 * A Node representation of a BasicGraphic.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: BasicGraphicNode.java 453 2012-12-17 15:39:53Z bdschubert $
 */
public class BasicGraphicNode extends DataNode implements PropertyChangeListener {

    private BasicGraphic graphic;
    private InstanceContent content;
    private PropertyChangeListener graphicListener = new GraphicPropertyChangeListener();
    private PropertyChangeListener dataListener = new DataObjectPropertyChangeListener();
    private static final Logger logger = Logger.getLogger(BasicGraphicNode.class.getName());

    /**
     * Constructor for a Node representing a BasicGraphic that's backed by a DataObject.
     *
     * @param dataObject the DataObject that backs this node
     * @param graphic the BasicGraphic represented by this node
     * @param cookieSet the DataObject's cookieSet that manages the SaveCookie capability
     */
    public BasicGraphicNode(DataObject dataObject, BasicGraphic graphic, Lookup cookieSet) {
        this(dataObject, graphic, cookieSet, new InstanceContent());
    }

    /**
     * Constructs the node with a proxy lookup comprised of the DataObject's cookieSet and this
     * node's capabilities.
     *
     * @param dataObject to be represented by this node
     * @param graphic to be represented by this node
     * @param cookieSet lookup from DataObject that provides Savable capability
     * @param content dynamic content for this node's lookup.
     */
    private BasicGraphicNode(DataObject dataObject, BasicGraphic graphic, Lookup cookieSet,
                             InstanceContent content) {
        super(dataObject, Children.LEAF, new ProxyLookup(new AbstractLookup(content), cookieSet));
        this.graphic = graphic;
        this.content = content;

        // TIP: Adding a Node to its own lookup creates duplication in downstream lookup queries, e
        //this.content.add(this);  -- Don't do this! // enables the Properties action. 
        // Populate our node's lookup with capabilities and API recommendations (this and DataObject)
        this.content.add(this.graphic);
        this.content.add(dataObject);
        this.content.add(new EditSupport());
        this.content.add(new DesignSupport());
        this.content.add(new DeleteSupport());
        this.content.add(new LockSupport());
        this.content.add(new StandardIdentitySupport());
        this.content.add(new StatusDeterminationSupport());
        // Add capability for deselecting 
        if (this.graphic.isSelected()) {
            this.content.add(new SelectableSupport());
        }

        // Listen for name changes, etc.
        graphic.addPropertyChangeListener(WeakListeners.propertyChange(this.graphicListener, graphic));
        dataObject.addPropertyChangeListener(WeakListeners.propertyChange(this.dataListener, dataObject));
    }

    @Override
    public Action[] getActions(boolean ignored_context) {
        return super.getActions(ignored_context);
//        return new Action[]
//            {
//                ModuleUtil.getAction("Edit", "com.emxsys.basicui.actions.DeselectAction"),
//                ModuleUtil.getAction("Edit", "com.emxsys.basicui.actions.EditAction"),
//                ModuleUtil.getAction("Edit", "com.emxsys.basicui.actions.DeleteAction"),
//                null,
//                ModuleUtil.getAction("System", "org.openide.actions.PropertiesAction"),
//            //SystemAction.get(PropertiesAction.class)
//            };
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
    public String getDisplayName() {
        String displayName = super.getDisplayName();        // may include or hide ext based on base class settings
        return FilenameUtils.decodeFilename(displayName);    // decode URL to plain text
    }

    @Override
    public void setName(String name) {
        // The Marker fires a property change event that invokes updateName to update Node and DataObject
        this.graphic.setName(name);
    }

    private void updateName(String name) {
        // Update the Node and the encapulated DataObject
        super.setName(name);
    }

    /**
     * Perform the delete.
     */
    private void doDelete(boolean confirm) {
        BasicGraphic graphic = this.graphic;
        boolean shouldDelete = !confirm;
        if (confirm) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    "Are you sure you want to delete " + (graphic.getName() != null ? graphic.getName() : "this graphic") + "?",
                    "Delete Graphic",
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE);
            Object result = DialogDisplayer.getDefault().notify(nd);
            shouldDelete = result.equals(NotifyDescriptor.YES_OPTION);
        }
        if (shouldDelete) {
            graphic.delete();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(BasicGraphic.PROP_GRAPHIC_SELECTED)) {
            if ((Boolean) evt.getNewValue() == true) {
                if (getLookup().lookup(SelectCapability.class) == null) {
                    this.content.add(new SelectableSupport());
                }
            } else {
                for (SelectCapability capability : getLookup().lookupAll(SelectCapability.class)) {
                    this.content.remove(capability);
                }
            }
        }

    }

    private class SelectableSupport implements SelectCapability {

        @Override
        public boolean isSelected() {
            return graphic.isSelected();
        }

        @Override
        public void setSelected(boolean selected) {
            graphic.setSelected(selected);
        }
    }

    private class EditSupport implements Editable {

        @Override
        public void edit() {
            graphic.edit();
        }
    };

    private class DesignSupport implements DesignCapability {

        @Override
        public void design() {
            graphic.editShape();
        }
    };

    private class DeleteSupport implements DeleteCapability {

        @Override
        public void delete() {
            // Do a delete with confirmation
            BasicGraphicNode.this.doDelete(true);
        }

        @Override
        public boolean isDeleted() {
            return graphic.isDeleted();
        }
    }

    private class LockSupport implements LockCapability {

        @Override
        public void setLocked(boolean locked) {
            graphic.setMovable(!locked);
        }

        @Override
        public boolean isLocked() {
            return !graphic.isMovable();
        }
    };

    private class StandardIdentitySupport implements FriendlyAffiliationCapability,
            NeutralAffiliationCapability,
            HostileAffiliationCapability {

        @Override
        public void makeAffiliationFriendly() {
            graphic.setStandardIdentity(StandardIdentity.FRIEND);
        }

        @Override
        public void makeAffiliationNeutral() {
            graphic.setStandardIdentity(StandardIdentity.NEUTRAL);
        }

        @Override
        public void makeAffiliationHostile() {
            graphic.setStandardIdentity(StandardIdentity.HOSTILE);
        }
    }

    private class StatusDeterminationSupport implements PresentStatusCapability,
            PlannedStatusCapability {

        @Override
        public void makeStatusExistingOrPresent() {
            graphic.setStatus(Status.PRESENT_EXISTING);
        }

        @Override
        public void makeStatusPlannedOrAnticipated() {
            graphic.setStatus(Status.ANTICIPATED_PLANNED);
        }
    }

    /**
     * Listens to changes on the Marker.
     */
    private class GraphicPropertyChangeListener implements PropertyChangeListener {

        GraphicPropertyChangeListener() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equals(Graphic.PROP_GRAPHIC_NAME)) {
                // Update Node and DataObject from marker (display name)
                updateName(FilenameUtils.encodeFilename(graphic.getName()));
            } //            else if (event.getPropertyName().equals(Marker.PROP_GRAPHIC_SYMBOL))
            //            {
            //                // Get a fresh image
            //                image = getImageFromMarker();
            //                fireIconChange();
            //            }
            else if (event.getPropertyName().equals(BasicGraphic.PROP_GRAPHIC_MOVABLE)) {
                // Update the lock badge
                fireIconChange();
            }
        }
    }

    /**
     * Listens to changes on the DataObject.
     */
    private class DataObjectPropertyChangeListener implements PropertyChangeListener {

        DataObjectPropertyChangeListener() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equals(DataObject.PROP_MODIFIED)) {
                // Fire event that triggers getHtmlDisplayName to that name reflects modified status
                fireDisplayNameChange(null, null);
            }
        }
    }
}
