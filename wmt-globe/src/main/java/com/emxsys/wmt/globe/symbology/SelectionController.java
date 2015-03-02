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

import com.emxsys.wmt.globe.Globe;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.pick.PickedObjectList;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Logger;
import org.openide.windows.WindowManager;

/**
 * This class handles mouse events and highlights and selects objects in the WorldWindow.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public abstract class SelectionController<T> implements SelectListener, MouseListener {

    private MouseEvent lastSelectMouseEvent;
    protected T lastPicked = null;
    protected T lastSelected = null;
    private static final Logger logger = Logger.getLogger(SelectionController.class.getName());

    /**
     * Gets the last item that was picked (highlighted)
     *
     * @return
     */
    public T getLastPicked() {
        return lastPicked;
    }

    /**
     * Gets the last item that was selected (clicked on).
     *
     * @return
     */
    public T getLastSelected() {
        return lastSelected;
    }

    /**
     * Defers attaching the SelectionController to the WorldWindow view until the UI is ready. FYI:
     * at startup, symbols could be loaded from a project file before the top component is loaded.
     */
    public void attachToViewer() {
        // Listen for events signaling that this symbol has been selected
        WindowManager.getDefault().invokeWhenUIReady(() -> {
            Globe.getInstance().getWorldWindManager().getWorldWindow().addSelectListener(SelectionController.this);
            Globe.getInstance().getWorldWindManager().getWorldWindow().getInputHandler().addMouseListener(SelectionController.this);
        });
    }

    /**
     * Handles the SelectEvent by delegating to highlight, select, open and contextMenu.
     *
     * @param event
     */
    @Override
    public void selected(SelectEvent event) {
        switch (event.getEventAction()) {
            case SelectEvent.ROLLOVER:
                highlight(event, event.getTopObject());
                break;
            // Can do LEFT_CLICK or LEFT_PRESS
            case SelectEvent.LEFT_PRESS:
                select(event, event.getTopObject());
                break;
            case SelectEvent.LEFT_DOUBLE_CLICK:
                open(event, event.getTopObject());
                break;
            // Could do RIGHT_CLICK instead
            case SelectEvent.RIGHT_PRESS:
                select(event, event.getTopObject());
                contextMenu(event, event.getTopObject());
                break;
        }
    }

    /**
     * Called by selected() on ROLL_OVER
     *
     * @param event forwarded from selected
     * @param obj picked as the top object
     */
    protected void highlight(SelectEvent event, Object obj) {
        // Do nothing if same thing selected.
        if (this.lastPicked == obj) {
            return; // same thing selected
        }

        // First, turn off currently highlighted symbol, if not it is currently selected
        if (this.lastPicked != null) {
            if (doSetHighlight(event, this.lastPicked, false)) {
                this.lastPicked = null;
            }
        }
        // Now select the current symbol
        T pickedItem = getInstance(obj);
        if (pickedItem != null) {
            if (doSetHighlight(event, pickedItem, true)) {
                this.lastPicked = pickedItem;
            }
        }
    }

    /**
     * Called by selected() on LEFT_CLICK.
     *
     * @param event forwarded from selected()
     * @param obj picked as the top object
     */
    protected void select(SelectEvent event, Object obj) {
        this.lastSelectMouseEvent = event.getMouseEvent();
        if (this.lastSelected == obj) {
            return; // same thing selected
        }
        // Turn off previous selection when something else is selected (can be vetoed)
        if (this.lastSelected != null) {
            // Can be vetoed!
            if (doSetSelected(event, this.lastSelected, false)) {
                this.lastSelected = null;
            }
        }
        // Now select the current item
        T selectedItem = getInstance(obj);
        if (selectedItem != null) {
            if (doSetSelected(event, selectedItem, true)) {
                this.lastSelected = selectedItem;
            }
        }
    }

    /**
     * Called by selected() on LEFT DOUBLE CLICK.
     *
     * @param event forwarded from selected()
     * @param obj picked as the top object
     */
    protected void open(SelectEvent event, Object obj) {
        T selectedItem = getInstance(obj);
        if (selectedItem != null) {
            doOpen(event, selectedItem);
        }
    }

    /**
     * Called by selected() on RIGHT CLICK.
     *
     * @param event forwarded from selected()
     * @param obj picked as the top object
     */
    protected void contextMenu(SelectEvent event, Object obj) {
        T selectedItem = getInstance(obj);
        if (selectedItem != null) {
            doContextMenu(event, selectedItem);
        }
    }

    /**
     * Gets a cast instance of the parameterized type from the object.
     *
     * @param obj object to cast.
     * @return an instance of the parameterized type.
     */
    abstract protected T getInstance(Object obj);

    /**
     * Implements the highlight change.
     *
     * @return true if highlight change succeeded, or false if it was vetoed.
     */
    abstract protected boolean doSetHighlight(SelectEvent event, T obj, boolean value);

    /**
     * Implements the selection change.
     *
     * @return true if selection change succeeded, or false if it was vetoed.
     */
    abstract protected boolean doSetSelected(SelectEvent event, T obj, boolean value);

    /**
     * Implements the open event, e.g., edit the selected item.
     *
     * @param event
     * @param obj
     */
    abstract protected void doOpen(SelectEvent event, T obj);

    /**
     * Shows the context menu for the selected item.
     *
     * @param event
     * @param obj
     */
    abstract protected void doContextMenu(SelectEvent event, T obj);

    /**
     * Deselects the current item if a mouse click occurs some distance away from the last click on
     * the item.
     * @param e
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        if (this.lastSelectMouseEvent != null && this.lastSelected != null) {
            // Deselection logic
            int THRESHOLD = 20; // pixels
            Point p1 = e.getPoint();
            Point p2 = this.lastSelectMouseEvent.getPoint();
            if (Math.abs(p1.x - p2.x) > THRESHOLD || Math.abs(p1.y - p2.y) > THRESHOLD) {
                SelectEvent dummy = new SelectEvent(this, SelectEvent.LEFT_PRESS, e, new PickedObjectList());
                if (doSetSelected(dummy, this.lastSelected, false)) {
                    this.lastSelected = null;
                }
            }
            // Preempt navigation 
            e.consume();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
