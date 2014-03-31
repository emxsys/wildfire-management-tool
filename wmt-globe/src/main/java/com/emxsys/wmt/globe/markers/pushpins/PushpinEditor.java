/*
 * Copyright (c) 2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.markers.pushpins;

import gov.nasa.worldwind.render.PointPlacemark;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle.Messages;

@Messages({
    "CTL_DialogTitleAdd=Add Pushpin",
    "CTL_DialogTitleEdit=Edit Pushpin"
})
/**
 * This class displays a simple editor for a Pushpin marker.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class PushpinEditor {

    public static boolean edit(Pushpin pushpin, boolean isNew) {
        // Create the dialog content panel
        PointPlacemark placemark = pushpin.getLookup().lookup(PointPlacemark.class);
        PushpinEditorPane dialogPane = new PushpinEditorPane(
                pushpin.getName(),
                pushpin.getPosition(),
                pushpin.isMovable(),
                placemark.getAttributes());

        // Wrap the panel in a standard dialog...
        DialogDescriptor descriptor = new DialogDescriptor(
                dialogPane,
                isNew ? Bundle.CTL_DialogTitleAdd() : Bundle.CTL_DialogTitleEdit(),
                true, // Modal?
                DialogDescriptor.OK_CANCEL_OPTION,
                DialogDescriptor.OK_OPTION,
                null);
        // ... and present to the user
        Object result = DialogDisplayer.getDefault().notify(descriptor);

        // Update the pushpin
        if (result != null && result == DialogDescriptor.OK_OPTION) {
            // Update name
            if (!pushpin.getName().equals(dialogPane.getMarkerName())) {
                pushpin.setName(dialogPane.getMarkerName());
            }
            // Update movable flag
            if (pushpin.isMovable() != dialogPane.isMovable()) {
                pushpin.setMovable(dialogPane.isMovable());
            }
            // Update attributes if image changed
            String oldImageAddress = placemark.getAttributes().getImageAddress();
            String newImageAddress = dialogPane.getMarkerRenderingAttributes().getImageAddress();
            if (oldImageAddress == null || oldImageAddress.isEmpty() || !oldImageAddress.equals(newImageAddress)) {
                // fires a property change event
                pushpin.setAttributes(dialogPane.getMarkerRenderingAttributes());
            }
            return true;
        }
        return false;
    }
}
