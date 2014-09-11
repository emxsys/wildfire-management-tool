/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.cps.views;

import com.emxsys.wmt.cps.views.forces.PreheatForcePanel;
import com.emxsys.wmt.cps.views.forces.SlopeForcePanel;
import com.emxsys.wmt.cps.views.forces.WindForcePanel;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.Dimension;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays the CPS Primary Forces.
 */
@ConvertAsProperties(dtd = "-//com.emxsys.wmt.cps.views//Forces//EN", autostore = false)
@TopComponent.Description(
        preferredID = ForcesTopComponent.PREFERRED_ID,
        iconBase = "com/emxsys/wmt/cps/images/cps-icon.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ForcesAction",
        preferredID = ForcesTopComponent.PREFERRED_ID
)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "com.emxsys.wmt.cps.views.ForcesTopComponent")
@RibbonActionReference(path = "Menu/Window/Show",
        position = 200,
        priority = "top",
        description = "#CTL_ForcesAction_Hint",
        tooltipTitle = "#CTL_ForcesAction_TooltipTitle",
        tooltipBody = "#CTL_ForcesAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/cps/images/cps-icon32.png")
@Messages({
    "CTL_ForcesTopComponent=Forces",
    "CTL_ForcesTopComponent_Hint=The CPS Primary Forces window.",
    "CTL_ForcesAction=Primary Forces",
    "CTL_ForcesAction_Hint=Show the CPS Primary Forces.",
    "CTL_ForcesAction_TooltipTitle=Show CPS Primary Forces",
    "CTL_ForcesAction_TooltipBody=Activates the Primary Forces window used for visualizing "
    + "the primary forces influencing fire behavior.",
    "CTL_ForcesAction_TooltipFooter=Press F1 for more help."
})
public final class ForcesTopComponent extends TopComponent {

    private static final Logger logger = Logger.getLogger(ForcesTopComponent.class.getName());
    public static final String PREFERRED_ID = "ForcesTopComponent";
    private PreheatForcePanel preheatPanel;
    private WindForcePanel windPanel;
    private SlopeForcePanel slopePanel;

    public static ForcesTopComponent getInstance() {
        return (ForcesTopComponent) WindowManager.getDefault().findTopComponent(PREFERRED_ID);
    }

    public ForcesTopComponent() {
        //
        logger.fine(PREFERRED_ID + " initializing....");

        initComponents();
        createPanels();

        setName(Bundle.CTL_ForcesTopComponent());
        setToolTipText(Bundle.CTL_ForcesTopComponent_Hint());
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

        logger.config(PREFERRED_ID + " initialized.");
    }

    private void createPanels() {
        preheatPanel = new PreheatForcePanel();
        windPanel = new WindForcePanel();
        slopePanel = new SlopeForcePanel();
        // Layout the panels to the Grid Layout
        add(preheatPanel);
        add(windPanel);
        add(slopePanel);
    }

    public void addAirTempPropertyChangeListener(PropertyChangeListener listener) {
        this.preheatPanel.pcs.addPropertyChangeListener(PreheatForcePanel.PROP_AIRTEMP, listener);
    }

    public void addWindDirPropertyChangeListener(PropertyChangeListener listener) {
        this.windPanel.pcs.addPropertyChangeListener(WindForcePanel.PROP_WINDDIR, listener);
    }

    public void addWindSpeedPropertyChangeListener(PropertyChangeListener listener) {
        this.windPanel.pcs.addPropertyChangeListener(WindForcePanel.PROP_WINDSPEED, listener);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        setLayout(new java.awt.GridLayout(3, 1));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        super.setPreferredSize(preferredSize); //To change body of generated methods, choose Tools | Templates.
        System.out.println(preferredSize.toString());

    }

}
