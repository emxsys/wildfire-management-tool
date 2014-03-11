/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.gis.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.NbBundle.Messages;

@Messages({
    "err.item.null=The item argument cannot be null.",
    "err.items.null=Items collection cannot be null.",
    "# {0} - catalog type",
    "err.item.incompatible=The item type is incompatable [{0}]. The item was not added.",
    "# {0} - catalog id",
    "err.item.already.exists=The item ID ({0}) already exists.",
    "# {0} - catalog id",
    "info.item.added=The {0} item was added.",
    "# {0} - catalog id",
    "info.item.removed=The {0} item was removed."
})
/**
 * This generic EntityCatalog manages a collection of unique {@link Items}. It provides property
 * change notifications when the catalog contents change. A EntityCatalog implementation, or its
 * individual items, may be mapped to persistent storage, and this class provides notifications that
 * the contents have changed so that 'dirty' flags can be set accordingly.
 *
 * @see Entity
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: EntityCatalog.java 529 2013-04-18 15:08:39Z bdschubert $
 */
public class EntityCatalog<T extends Entity> implements PropertyChangeListener {

    public static final String PROP_ITEMS_ADDED = "PROP_ITEMS_ADDED";
    public static final String PROP_ITEMS_CLEARED = "PROP_ITEMS_CLEARED";
    public static final String PROP_ITEM_ADDED = "PROP_ITEM_ADDED";
    public static final String PROP_ITEM_CHANGED = "PROP_ITEM_CHANGED";
    public static final String PROP_ITEM_REMOVED = "PROP_ITEM_REMOVED";
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private final Map<Long, T> itemMap = new TreeMap<>();
    private static final Logger logger = Logger.getLogger(EntityCatalog.class.getName());

    /**
     * Adds the item to this catalog.
     *
     * @param item the unique item to add.
     */
    public void add(T item) {
        if (item == null) {
            logger.severe(Bundle.err_item_null());
            throw new IllegalArgumentException(Bundle.err_item_null());
        }
        if (contains(item)) {
            logger.info(Bundle.err_item_already_exists(item.getName()));
            return;
        }

        doAddItem(item);

        logger.fine(Bundle.info_item_added(item.getName()));
        this.pcs.firePropertyChange(PROP_ITEM_ADDED, null, item);
    }

    /**
     * Adds the item to the internal collection and registers a listener on the item.
     *
     * @param item the item to be added.
     */
    protected void doAddItem(T item) {
        if (item == null) {
            String msg = Bundle.err_item_null();
            logger.severe(msg);
            throw new IllegalArgumentException(msg);
        }
        logger.log(Level.FINE, "Adding {0}", item.getName());
        this.itemMap.put(item.getUniqueID(), item);
        item.addPropertyChangeListener(this);
    }

    /**
     * Removes the item from this catalog. Fires a PROP_ITEM_REMOVED event.
     *
     * @param item the item to be removed.
     */
    public void remove(T item) {
        if (item == null) {
            logger.warning(Bundle.err_item_null());
            throw new IllegalArgumentException(Bundle.err_item_null());
        }

        if (!itemMap.containsKey(item.getUniqueID())) {
            return;
        }

        this.doRemoveItem(item);

        logger.fine(Bundle.info_item_removed(item.getName()));
        pcs.firePropertyChange(PROP_ITEM_REMOVED, item, null);
    }

    protected void doRemoveItem(T item) {
        if (item == null) {
            String msg = Bundle.err_item_null();
            logger.severe(msg);
            throw new IllegalArgumentException(msg);
        }
        // Remove from this catalog
        logger.log(Level.FINE, "Removing {0}", item.getName());
        item.removePropertyChangeListener(this);
        this.itemMap.remove(item.getUniqueID());

    }

    /**
     * Registers a PropertyChangeListener on this catalog. The listener will be notified when the
     * contents of the catalog change.
     *
     * @param listener the listener to be registered.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Unregisters a PropertyChangeListener from this catalog.
     *
     * @param listener the listener to be unregistered.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Gets the items in this catalog.
     *
     * @return a collection of T instances.
     * @see T
     */
    public Collection<? extends T> getItems() {
        return this.itemMap.values();
    }

    /**
     * Replaces the current collections of items. Fires PROP_ITEMS_CLEARED and PROP_ITEMS_ADDED
     * events.
     *
     * @param items a collection of T instances.
     */
    public void setItems(Collection<? extends T> items) {
        logger.info("Item collection being added...");
        synchronized (this) {
            if (items == null) {
                logger.severe(Bundle.err_items_null());
                throw new IllegalArgumentException(Bundle.err_items_null());
            }

            for (T item : this.itemMap.values()) {
                remove(item);
            }
            this.itemMap.clear();
            pcs.firePropertyChange(PROP_ITEMS_CLEARED, null, null);

            for (T item : items) {
                doAddItem(item);
            }
            pcs.firePropertyChange(PROP_ITEMS_ADDED, null, items);
        }
    }

    /**
     * Responds to changes in contained items by firing a PROP_ITEM_CHANGED event with the event
     * source to listeners.
     *
     * @param evt event from a T.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Entity) {
            // Notify node explorer(s)
            pcs.firePropertyChange(PROP_ITEM_CHANGED, null, evt.getSource());
        }
    }

    /**
     * Determines if this catalog contains the item.
     *
     * @param item item providing a unique ID.
     * @return true if the catalog contains an item matching the parameter's unique ID.
     */
    public boolean contains(T item) {
        return itemMap.containsKey(item.getUniqueID());
    }

    public void dispose() {
        this.itemMap.clear();
    }
}
