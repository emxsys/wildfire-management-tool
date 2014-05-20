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
package com.emxsys.wmt.globe.markers;

import com.emxsys.wmt.core.capabilities.DeleteCapability;
import com.emxsys.wmt.core.capabilities.LockCapability;
import com.emxsys.gis.api.marker.Marker;
import com.emxsys.wmt.globe.Globe;
import com.emxsys.util.FilenameUtils;
import com.emxsys.util.ModuleUtil;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.PropertiesAction;
import org.openide.cookies.EditCookie;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 * This class is a logical representation of a BasicMarkerDataObject and its BasicMarker.
 *
 * @author Bruce Schubert
 */
public class BasicMarkerNode extends DataNode {

    private final BasicMarker marker;
    private final InstanceContent content;
    private Image image = null;
    private final PropertyChangeListener markerListener = new MarkerPropertyChangeListener();
    private final PropertyChangeListener dataListener = new DataObjectPropertyChangeListener();
    private static final Logger logger = Logger.getLogger(BasicMarkerNode.class.getName());

    static {
        DataNode.setShowFileExtensions(false);
    }

    /**
     * Constructor for a Node representing a BasicMarker that's backed by a DataObject.
     *
     * @param dataObject the DataObject that backs this node
     * @param marker the BasicMarker represented by this node
     * @param cookieSet the DataObject's cookieSet that manages the SaveCookie capability
     */
    public BasicMarkerNode(DataObject dataObject, BasicMarker marker, Lookup cookieSet) {
        this(dataObject, marker, cookieSet, new InstanceContent());
    }

    /**
     * Constructs the node with a proxy lookup comprised of the DataObject's cookieSet and this
     * node's capabilities.
     *
     * @param dataObject to be represented by this node
     * @param marker to be represented by this node
     * @param cookieSet lookup from DataObject that provides Savable capability
     * @param content dynamic content for this node's lookup.
     */
    private BasicMarkerNode(DataObject dataObject, BasicMarker marker, Lookup cookieSet,
                            InstanceContent content) {
        super(dataObject, Children.LEAF, new ProxyLookup(new AbstractLookup(content), cookieSet));
        this.marker = marker;
        this.content = content;

        // Populate our node's lookup with capabilities and API recommendations (this and DataObject)
        //this.content.add(this); -- Don't do this! Creates duplication in lookupAll(Node.class) queries
        this.content.add(this.marker);
        this.content.add(dataObject);
        this.content.add(new DeleteSupport());
        this.content.add(new LockSupport());
        this.content.add(new EditSupport());

        // Listen for name changes, etc.
        marker.addPropertyChangeListener(WeakListeners.propertyChange(this.markerListener, marker));
        dataObject.addPropertyChangeListener(WeakListeners.propertyChange(this.dataListener, dataObject));
    }

    public BasicMarker getMarker() {
        return marker;
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
            new CenterOnAction("Go To"),
            null,
            SystemAction.get(CopyAction.class),
            SystemAction.get(CutAction.class),
            null,
            ModuleUtil.getAction("Edit", "com.emxsys.wmt.core.actions.EditAction"),
            ModuleUtil.getAction("Edit", "com.emxsys.wmt.core.actions.DeleteAction"),
            null,
            SystemAction.get(PropertiesAction.class)
        };
    }

    @Override
    public Action getPreferredAction() {
        // Default action is Goto.
        // Unless we click on marker in the Globe, then we Edit.
        if (Globe.getInstance().getGlobeTopComponent().isFocusOwner()) {
            return ModuleUtil.getAction("Edit", "com.emxsys.wmt.core.actions.EditAction");
        }
        return new CenterOnAction("Go To");
    }

    /**
     * Disables the inline rename editor.
     *
     * @return false
     */
    @Override
    public boolean canRename() {
        return false;
    }

    @Override
    public String getDisplayName() {
        String displayName = super.getDisplayName();        // may include or hide ext based on base class settings
        return FilenameUtils.decodeFilename(displayName);    // decode URL to plain text
    }

    @Override
    public void setName(String name) {
        // The Marker fires a property change event that invokes updateName to update Node and DataObject
        this.marker.setName(name);
    }

    private void updateName(String name) {
        // Update the Node and the encapulated DataObject
        super.setName(name);
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

    /**
     * Gets the icon from the backing Marker image.
     *
     * @param type ignored
     * @return an icon that matches the marker image.
     */
    @Override
    public Image getIcon(int type) {
        if (this.image == null) {
            this.image = getImageFromMarker();
        }
        Image markerImage = (this.image == null ? super.getIcon(type) : this.image);
        // Add a lock 'badge' if locked
        Image lockedImage = null;
        LockCapability lockable = getLookup().lookup(LockCapability.class);
        if (lockable != null && lockable.isLocked()) {
            Image badgeImage = ImageUtilities.loadImage("com/emxsys/wmt/globe/images/lock_badge.png");
            if (badgeImage != null) {
                lockedImage = ImageUtilities.mergeImages(markerImage, badgeImage, 16, 8);
            }
        }
        return lockedImage == null ? markerImage : lockedImage;
    }

    /**
     * @return a 16x16 Image based on the marker's image
     */
    private Image getImageFromMarker() {
        final int IMG_WIDTH = 16;
        final int IMG_HEIGHT = 16;
        Image originalImage = this.marker.getImage();
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
    void doDelete(boolean confirm) {
        boolean shouldDelete = !confirm;
        if (confirm) {
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                    "Are you sure you want to delete " + marker.getName() + "?",
                    "Delete Marker",
                    NotifyDescriptor.YES_NO_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE);
            Object result = DialogDisplayer.getDefault().notify(nd);
            shouldDelete = result.equals(NotifyDescriptor.YES_OPTION);
        }
        if (shouldDelete) {
            marker.delete();
        }
    }

    private class DeleteSupport implements DeleteCapability, Node.Cookie {

        @Override
        public void delete() {
            // Do a delete with confirmation
            doDelete(true);
        }

        @Override
        public boolean isDeleted() {
            return marker.isDeleted();
        }
    }

    /**
     * Use EditCookie (versus Editable) so other NetBeans API (like search) can find the editor for
     * this marker.
     */
    private class EditSupport implements EditCookie {

        @Override
        public void edit() {
            marker.edit();
        }
    }

    private class LockSupport implements LockCapability, Node.Cookie {

        @Override
        public void setLocked(boolean locked) {
            marker.setMovable(!locked);
            fireIconChange();
        }

        @Override
        public boolean isLocked() {
            return !marker.isMovable();
        }
    }

    /**
     * The is the Go To action.
     */
    private class CenterOnAction extends AbstractAction {

        private CenterOnAction(String name) {
            super(name);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Globe.getInstance().centerOn(marker.getPosition());
        }
    }

    /**
     * Listens to changes on the Marker.
     */
    private class MarkerPropertyChangeListener implements PropertyChangeListener {

        MarkerPropertyChangeListener() {
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            switch (event.getPropertyName()) {
                case Marker.PROP_MARKER_NAME:
                    // Update Node and DataObject from marker (display name)
                    updateName(FilenameUtils.encodeFilename(marker.getName()));
                    break;
                case Marker.PROP_MARKER_SYMBOL:
                    // Get a fresh image
                    image = getImageFromMarker();
                    fireIconChange();
                    break;
                case BasicMarker.PROP_MARKER_MOVABLE:
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
