/*
 * Copyright (c) 2011-2014, Bruce Schubert. <bruce@emxsys.com> 
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
package com.emxsys.wmt.cps.data;

import com.emxsys.wmt.cps.analytics.BehaveAnalyticsPrototype;
import com.emxsys.visad.filetype.NetCdfDataNode;
import com.emxsys.wildfire.api.WildfireType;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.LinkedList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.Lookup;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: BehaveDataNode.java 689 2013-05-27 15:29:33Z bdschubert $
 */
public class BehaveDataNode extends NetCdfDataNode {

    private BehaveDataObject behaveDataObject;

    /**
     * Constructor for a Node representing a Weather NetCDF file.
     *
     * @param dataObject for a FlatField that backs this node
     * @param cookieSet the DataObject's cookieSet that manages the SaveCookie capability
     */
    public BehaveDataNode(BehaveDataObject dataObject, Lookup cookieSet) {
        super(dataObject, cookieSet);
        this.behaveDataObject = dataObject;
    }

    @Override
    public Action[] getActions(boolean context) {
        LinkedList<Action> actions = new LinkedList<>();
        Collections.addAll(actions, super.getActions(context)); // base actions

        // TODO: insert this nodes actions into the collection
        actions.addFirst(new AbstractAction("Visualize!") {
            @Override
            public void actionPerformed(ActionEvent e) {
                BehaveAnalyticsPrototype.visualize(behaveDataObject, WildfireType.FLAME_LENGTH_SI);
            }
        });
        actions.addFirst(new AbstractAction("Animate!") {
            @Override
            public void actionPerformed(ActionEvent e) {
                BehaveAnalyticsPrototype.animate(behaveDataObject,
                        WildfireType.DIR_OF_SPREAD, WildfireType.RATE_OF_SPREAD_SI, WildfireType.FLAME_LENGTH_SI);
            }
        });

        Action[] allActions = new Action[0];
        return actions.toArray(allActions);
    }
}
