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
package com.emxsys.wmt.cps;

import com.emxsys.wmt.gis.api.Coord3D;
import com.emxsys.wmt.gis.api.Terrain;
import com.terramenta.ribbon.RibbonActionReference;
import java.awt.Dimension;
import java.time.ZonedDateTime;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import visad.Real;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.emxsys.wmt.cps//PrimaryForces//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "PrimaryForcesTopComponent",
        iconBase = "com/emxsys/wmt/cps/images/cps-icon.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "com.emxsys.wmt.cps.PrimaryForcesTopComponent")
@RibbonActionReference(path = "Menu/Window/Show",
        position = 200,
        priority = "top",
        description = "#CTL_PrimaryForcesAction_Hint",
        tooltipTitle = "#CTL_PrimaryForcesAction_TooltipTitle",
        tooltipBody = "#CTL_PrimaryForcesAction_TooltipBody",
        tooltipIcon = "com/emxsys/wmt/cps/images/cps-icon32.png")
//        tooltipFooter = "#CTL_PrimaryForcesAction_TooltipFooter",
//        tooltipFooterIcon = "com/terramenta/images/help.png")
@Messages({
    "CTL_PrimaryForcesTopComponent=Primary Forces",
    "CTL_PrimaryForcesTopComponent_Hint=The CPS Primary Forces window.",
    "CTL_PrimaryForcesAction=Primary Forces",
    "CTL_PrimaryForcesAction_Hint=Show the CPS Primary Forces.",
    "CTL_PrimaryForcesAction_TooltipTitle=Show CPS Primary Forces",
    "CTL_PrimaryForcesAction_TooltipBody=Activates the Primary Forces window used for visualizing "
    + "the primary forces influencing fire behavior.",
    "CTL_PrimaryForcesAction_TooltipFooter=Press F1 for more help."
})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_PrimaryForcesAction",
        preferredID = PrimaryForcesTopComponent.PREFERRED_ID
)
public final class PrimaryForcesTopComponent extends TopComponent {

    public static final String PREFERRED_ID = "PrimaryForcesTopComponent";
    private PreheatPanel preheatPanel;
    private WindPanel windPanel;
    private SlopePanel slopePanel;

    public PrimaryForcesTopComponent() {
        initComponents();
        createPanels();
        setName(Bundle.CTL_PrimaryForcesTopComponent());
        setToolTipText(Bundle.CTL_PrimaryForcesTopComponent_Hint());
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);

    }

    public void updateCharts(Coord3D coord, Terrain terrain) {
        slopePanel.updateCharts(terrain);
    }

    public void updateCharts(ZonedDateTime time, Real sunAzimuth) {
        preheatPanel.updateCharts(time, sunAzimuth);
    }

    private void createPanels() {
        preheatPanel = new PreheatPanel();
        windPanel = new WindPanel();
        slopePanel = new SlopePanel();
        // Layout the page
        jSplitPane1.setTopComponent(preheatPanel);
        jSplitPane2.setTopComponent(windPanel);
        jSplitPane2.setBottomComponent(slopePanel);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();

        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.333);

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane2.setResizeWeight(0.5);
        jSplitPane1.setBottomComponent(jSplitPane2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
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
