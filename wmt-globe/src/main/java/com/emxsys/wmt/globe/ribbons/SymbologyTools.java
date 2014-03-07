/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emxsys.wmt.globe.ribbons;

import com.terramenta.globe.GlobeTopComponent;
import com.terramenta.ribbon.RibbonManager;
import java.util.Collection;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.RibbonContextualTaskGroup;

/**
 * The SymbologyTools instances listens for the existence of a symbology object on the lookup, and
 * if found it displays the "symbology tools" contextual task group.
 *
 * @author Bruce Schubert
 * @version $Id$
 */
public class SymbologyTools implements LookupListener {

    RibbonContextualTaskGroup taskGroup;
    private Lookup.Result<GlobeTopComponent> lookupResult;
    private Object context;

    private SymbologyTools() {
        // Find the task group
        RibbonManager ribbonMgr = Lookup.getDefault().lookup(RibbonManager.class);
        JRibbon ribbon = ribbonMgr.getRibbon();
        if (ribbon != null) {
            for (int i = 0; i < ribbon.getContextualTaskGroupCount(); i++) {
                RibbonContextualTaskGroup group = ribbon.getContextualTaskGroup(i);
                if (group.getTitle().equalsIgnoreCase("symbology tools")) {
                    this.taskGroup = group;
                    break;
                }
            }
        }

        // Initilize the lookup listener
        this.lookupResult = Utilities.actionsGlobalContext().lookupResult(GlobeTopComponent.class);
        this.lookupResult.addLookupListener(this);

        // Show/Hide the ribbon task group
        resultChanged(null);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        if (taskGroup != null) {
            JRibbon ribbon = Lookup.getDefault().lookup(RibbonManager.class).getRibbon();
            ribbon.setVisible(taskGroup, this.lookupResult.allInstances().size() > 0);
        }
    }

    public static SymbologyTools getInstance() {
        return SymbologyToolsHolder.INSTANCE;
    }

    private static class SymbologyToolsHolder {

        private static final SymbologyTools INSTANCE = new SymbologyTools();
    }

}
