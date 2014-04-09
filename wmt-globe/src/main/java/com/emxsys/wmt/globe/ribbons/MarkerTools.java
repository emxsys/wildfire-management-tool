/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emxsys.wmt.globe.ribbons;

import com.emxsys.wmt.globe.markers.BasicMarker;
import com.terramenta.ribbon.RibbonManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.RibbonContextualTaskGroup;

/**
 * The MarkerTools instance listens for the existence of a BasicMarkerNode object on the lookup, and
 * if found it displays the "Marker Tools" contextual task group.
 * <p>
 * NOTE: The singleton must be instantiated for this contextual menu to work. You should call
 * MarkerTools.getInstance() in the Globe initializeResources() body.
 *
 * @author Bruce Schubert
 */
@Messages({
    "CTL_MarkerTools=Marker Tools"
})
public final class MarkerTools implements LookupListener {

    private RibbonContextualTaskGroup taskGroup;
    private final Lookup.Result<BasicMarker> lookupResult;

    @SuppressWarnings("LeakingThisInConstructor")
    private MarkerTools() {
        // Initilize the lookup listener
        this.lookupResult = Utilities.actionsGlobalContext().lookupResult(BasicMarker.class);
        this.lookupResult.addLookupListener(this);
        // Trigger initial show/hide
        resultChanged(null);
    }

    /**
     * Enables the task group if a single BasicMarkerNode is in the lookup.
     * @param ev ignored
     */
    @Override
    public void resultChanged(LookupEvent ev) {
        RibbonContextualTaskGroup group = getTaskGroup();
        if (group != null) {
            JRibbon ribbon = Lookup.getDefault().lookup(RibbonManager.class).getRibbon();
            // Enable and show the task pane when a single marker is selected.
            if (this.lookupResult.allInstances().size() == 1) {
                ribbon.setVisible(group, true);
                ribbon.setSelectedTask(group.getTask(0)); // use the first (and only) task.
            } else {
                ribbon.setVisible(group, false);
            }
        }
    }

    private RibbonContextualTaskGroup getTaskGroup() {
        if (this.taskGroup == null) {
            JRibbon ribbon = Lookup.getDefault().lookup(RibbonManager.class).getRibbon();
            if (ribbon == null) {
                throw new IllegalStateException("Ribbon cannot be null.");
            }
            // Find the Marker Tools task group
            for (int i = 0; i < ribbon.getContextualTaskGroupCount(); i++) {
                RibbonContextualTaskGroup group = ribbon.getContextualTaskGroup(i);
                if (group.getTitle().equalsIgnoreCase(Bundle.CTL_MarkerTools())) {
                    this.taskGroup = group;
                }
            }
        }
        return this.taskGroup;
    }

    /**
     * Gets the singleton instance.
     * @return singleton
     */
    public static MarkerTools getInstance() {
        return MarkerToolsHolder.INSTANCE;
    }

    private static class MarkerToolsHolder {

        private static final MarkerTools INSTANCE = new MarkerTools();
    }

}
