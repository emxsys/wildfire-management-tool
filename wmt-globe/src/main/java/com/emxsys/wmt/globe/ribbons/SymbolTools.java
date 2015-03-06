/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emxsys.wmt.globe.ribbons;

import com.emxsys.wmt.globe.symbology.BasicSymbol;
import com.emxsys.wmt.globe.symbology.BasicSymbolNode;
import com.terramenta.ribbon.RibbonManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.api.ribbon.RibbonContextualTaskGroup;

/**
 * The SymbolTools instance listens for the existence of a BasicSymbolNode object on the lookup, and
 * if found it displays the "Symbol Tools" contextual task group.
 * <p>
 * NOTE: The singleton must be instantiated for this contextual menu to work. You should call
 * {@code SymbolTools.getInstance()} in the {@code Globe.initializeResources()} body.
 * <p>
 * See the {@code Ribbon/TaskPanes/SymbolTools} folder in {@code layer.xml} and
 * {@code generated-layer.xml} for content.
 * 
* @author Bruce Schubert
 */
@NbBundle.Messages({
    "CTL_SymbolTools=Symbol Tools"
})

public final class SymbolTools implements LookupListener {

    private RibbonContextualTaskGroup taskGroup;
    private final Lookup.Result<BasicSymbol> lookupResult;

    @SuppressWarnings("LeakingThisInConstructor")
    private SymbolTools() {
        // Initilize the lookup listener
        this.lookupResult = Utilities.actionsGlobalContext().lookupResult(BasicSymbol.class);
        this.lookupResult.addLookupListener(this);
        // Trigger initial show/hide
        resultChanged(null);
    }

    /**
     * Enables the task group if a single BasicSymbolNode is in the lookup.
     * @param ev ignored
     */
    @Override
    public void resultChanged(LookupEvent ev) {
        RibbonContextualTaskGroup group = getTaskGroup();
        if (group != null) {
            JRibbon ribbon = Lookup.getDefault().lookup(RibbonManager.class).getRibbon();
            // Enable and show the task pane when a single symbololgy is selected.
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
            this.taskGroup = RibbonUtil.getContextualTaskGroup(Bundle.CTL_SymbolTools());
        }
        return this.taskGroup;
    }

    /**
     * Gets the singleton instance.
     * @return singleton
     */
    public static SymbolTools getInstance() {
        return SymbologyToolsHolder.INSTANCE;
    }

    private static class SymbologyToolsHolder {

        private static final SymbolTools INSTANCE = new SymbolTools();
    }

}
