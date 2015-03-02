/*
 * Copyright (c) 2009-2015, Bruce Schubert. <bruce@emxsys.com>
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

import com.emxsys.gis.api.symbology.StandardIdentity;
import com.emxsys.gis.api.symbology.Status;
import com.emxsys.gis.api.symbology.Symbol;
import com.emxsys.util.FilenameUtils;
import com.emxsys.wmt.core.capabilities.*;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
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
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 * A Node representation of a BasicSymbol. Provides actions and an icons.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: BasicSymbolNode.java 453 2012-12-17 15:39:53Z bdschubert $
 */
public class BasicSymbolNode extends DataNode implements PropertyChangeListener {

    private BasicSymbol symbol;
    private InstanceContent content;
    private Image image;
    private PropertyChangeListener symbolListener = new SymbolPropertyChangeListener();
    private PropertyChangeListener dataListener = new DataObjectPropertyChangeListener();
    private static final Logger logger = Logger.getLogger(BasicSymbolNode.class.getName());

    /**
     * Constructor for a Node representing a BasicSymbol that's backed by a DataObject.
     *
     * @param dataObject the DataObject that backs this node
     * @param symbol the BasicSymbol represented by this node
     * @param cookieSet the DataObject's cookieSet that manages the SaveCookie capability
     */
    public BasicSymbolNode(DataObject dataObject, BasicSymbol symbol, Lookup cookieSet) {
        this(dataObject, symbol, cookieSet, new InstanceContent());
    }

    /**
     * Constructs the node with a proxy lookup comprised of the DataObject's cookieSet and this
     * node's capabilities.
     *
     * @param dataObject to be represented by this node
     * @param symbol to be represented by this node
     * @param cookieSet lookup from DataObject that provides Savable capability
     * @param content dynamic content for this node's lookup.
     */
    private BasicSymbolNode(DataObject dataObject, BasicSymbol symbol, Lookup cookieSet,
                            InstanceContent content) {
        super(dataObject, Children.LEAF, new ProxyLookup(new AbstractLookup(content), cookieSet));
        this.symbol = symbol;
        this.content = content;

        // Populate our node's lookup with capabilities and API recommendations (this and DataObject)
        //this.content.add(this); -- Don't do this! Creates duplication in lookupAll(Node.class) queries
        this.content.add(dataObject);
        this.content.add(this.symbol);
        this.content.add(new EditSupport());
        this.content.add(new DeleteSupport());
        this.content.add(new LockSupport());
        this.content.add(new StandardIdentitySupport());
        this.content.add(new StatusDeterminationSupport());
        // Add capability for deselecting 
        if (this.symbol.isSelected()) {
            this.content.add(new SelectableSupport());
        }

        // Listen for name changes, etc.
        symbol.addPropertyChangeListener(WeakListeners.propertyChange(this.symbolListener, symbol));
        dataObject.addPropertyChangeListener(WeakListeners.propertyChange(this.dataListener, dataObject));
    }

    @Override
    public Action[] getActions(boolean context) {
        return super.getActions(context);

//        return new Action[]
//            {
//                ModuleUtil.getAction("Edit", "com.emxsys.basicui.actions.DeleteAction"),
//                SystemAction.get(PropertiesAction.class)
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
        // The Symbol fires a property change event that invokes 
        // updateName to update Node and DataObject
        this.symbol.setName(name);
    }

    private void updateName(String name) {
        // Update the Node and the encapulated DataObject
        super.setName(name);
    }

    /**
     * Gets the icon from the backing Marker image.
     *
     * @param type ignored
     * @return an icon that matches the marker image.
     */
    @Override
    public Image getIcon(int type) {
        if (this.image == null) {
            this.image = getImageFromSymbol();
        }
        Image markerImage = (this.image == null ? super.getIcon(type) : this.image);
        // Add a lock 'badge' if locked
        Image lockedImage = null;
        LockCapability lockable = getLookup().lookup(LockCapability.class);
        if (lockable != null && lockable.isLocked()) {
            Image badgeImage = ImageUtilities.loadImage("com/emxsys/worldwind/resources/lock_badge.png");
            if (badgeImage != null) {
                lockedImage = ImageUtilities.mergeImages(markerImage, badgeImage, 16, 8);
            }
        }
        return lockedImage == null ? markerImage : lockedImage;
    }

    /**
     * @return a 16x16 Image based on the marker's image
     */
    private Image getImageFromSymbol() {
        final int IMG_WIDTH = 16;
        final int IMG_HEIGHT = 16;
        Image originalImage = this.symbol.getImage();
        if (originalImage == null) {
            logger.warning("marker.getImage() returned null."); //NOI18
            return null;
        }
        // Resize the image
        if (originalImage.getHeight(null) > IMG_HEIGHT) {
            BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
            g.dispose();
            return resizedImage;
        } else {
            return originalImage;
        }
    }

    /**
     * Perform the delete.
     */
    private void doDelete(boolean confirm) {
        boolean shouldDelete = !confirm;
        if (confirm) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    "Are you sure you want to delete " + (symbol.getName() != null ? symbol.getName() : "this symbol") + "?",
                    "Delete Symbol",
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE);
            Object result = DialogDisplayer.getDefault().notify(nd);
            shouldDelete = result.equals(NotifyDescriptor.YES_OPTION);
        }
        if (shouldDelete) {
            symbol.delete();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(BasicSymbol.PROP_SYMBOL_SELECTED)) {
            if ((Boolean) evt.getNewValue() == true) {
                if (getLookup().lookup(SelectCapability.class) == null) {
                    this.content.add(new BasicSymbolNode.SelectableSupport());
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
            return symbol.isSelected();
        }

        @Override
        public void setSelected(boolean selected) {
            symbol.setSelected(selected);
        }
    }

    private class EditSupport implements Editable {

        @Override
        public void edit() {
            throw new UnsupportedOperationException("edit() not implemented yet.");
        }
    };

    private class DeleteSupport implements DeleteCapability {

        @Override
        public void delete() {
            // Do a delete with confirmation
            BasicSymbolNode.this.doDelete(true);
        }

        @Override
        public boolean isDeleted() {
            return symbol.isDeleted();
        }
    }

    private class LockSupport implements LockCapability {

        @Override
        public void setLocked(boolean locked) {
            symbol.setMovable(!locked);
        }

        @Override
        public boolean isLocked() {
            return !symbol.isMovable();
        }
    };

    private class StandardIdentitySupport implements
            FriendlyAffiliationCapability,
            NeutralAffiliationCapability,
            HostileAffiliationCapability,
            UnknownAffiliationCapability {

        @Override
        public void makeAffiliationFriendly() {
            symbol.setStandardIdentity(StandardIdentity.FRIEND);
        }

        @Override
        public void makeAffiliationNeutral() {
            symbol.setStandardIdentity(StandardIdentity.NEUTRAL);
        }

        @Override
        public void makeAffiliationHostile() {
            symbol.setStandardIdentity(StandardIdentity.HOSTILE);
        }

        @Override
        public void makeAffiliationUnknown() {
            symbol.setStandardIdentity(StandardIdentity.UNKNOWN);
        }
    }

    private class StatusDeterminationSupport implements 
            PresentStatusCapability,
            PlannedStatusCapability {

        @Override
        public void makeStatusExistingOrPresent() {
            symbol.setStatus(Status.PRESENT_EXISTING);
        }

        @Override
        public void makeStatusPlannedOrAnticipated() {
            symbol.setStatus(Status.ANTICIPATED_PLANNED);
        }
    }

    /**
     * Listens to changes on the Marker.
     */
    private class SymbolPropertyChangeListener implements PropertyChangeListener {

        SymbolPropertyChangeListener() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            switch (event.getPropertyName()) {
                case Symbol.PROP_SYMBOL_NAME:
                    // Update Node and DataObject from marker (display name)
                    updateName(FilenameUtils.encodeFilename(symbol.getName()));
                    break;
                case BasicSymbol.PROP_SYMBOL_IMAGE:
                    // Get a fresh image
                    image = getImageFromSymbol();
                    fireIconChange();
                    break;
                case BasicSymbol.PROP_SYMBOL_MOVABLE:
                    // Update the lock badge
                    fireIconChange();
                    break;
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
