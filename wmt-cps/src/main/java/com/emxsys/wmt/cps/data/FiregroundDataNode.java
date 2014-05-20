/*
 * Copyright (c) 2011-2012, Bruce Schubert. <bruce@emxsys.com> 
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

import com.emxsys.wildfire.api.Fireground;
import java.awt.Image;
import java.util.List;
import javax.swing.Action;
import org.openide.loaders.DataNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * This class provides a DataNode for the FiregroundDataObject (fireground.xml file).
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@Messages("firegroundNodeDisplayName=Definition")
public class FiregroundDataNode extends DataNode {

    public FiregroundDataNode(FiregroundDataObject fdo, Lookup lookup) {
        super(fdo, Children.create(
                new FiregroundChildren(fdo.getLookup().lookup(Fireground.class)), true),
                lookup);
    }

    @Override
    public String getDisplayName() {
        //return super.getDisplayName();
        return Bundle.firegroundNodeDisplayName();

    }

    @Override
    public String getHtmlDisplayName() {
        return super.getHtmlDisplayName();
    }

    @Override
    public Image getIcon(int type) {
        return super.getIcon(type);
    }

    @Override
    public Action[] getActions(boolean context) {
        return super.getDataObject().getLoader ().getActions ();
    }

    
    /**
     * ChildFactory
     */
    private static class FiregroundChildren extends ChildFactory<Fireground> {

        private final Fireground fireground;

        public FiregroundChildren(Fireground fireground) {
            this.fireground = fireground;
        }

        @Override
        protected boolean createKeys(List<Fireground> toPopulate) {
            toPopulate.add(fireground);
            return true;
        }

        @Override
        protected Node[] createNodesForKey(Fireground key) {
            return new Node[]{
                new FiregroundExtentsNode(key), //                    new FlatFieldNode(key.getFuelModel()),
            //                    new FlatFieldNode(key.getFirePredictions()),
            //                    new FlatFieldNode(key.getFireSignatures()),
            //                    new FlatFieldNode(key.getFuelMoisture()),
            //                    new FlatFieldNode(key.getFuelTemperature()),
            //                    new FlatFieldNode(key.getWxForecasts()),
            //                    new FlatFieldNode(key.getWxObservations())
            };
            //new FlatFieldNode(key.getTerrain());
        }
    }
}
