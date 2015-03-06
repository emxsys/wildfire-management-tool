/*
 * Copyright (c) 2012-2015, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.symbology.editor;

import com.emxsys.wmt.globe.symbology.BasicSymbol;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525TacticalSymbol;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 * Provides capability to edit the attributes of a MILSTD2525C Symbol.
 *
 * @author Bruce Schubert
 */
@NbBundle.Messages({
    "CTL_DialogTitleAdd=New Tactical Symbol",
    "CTL_DialogTitleEdit=Edit Tactical Symbol"
})
public class SymbolEditor {

    private BasicSymbol symbol;
    private boolean isNew = false;

    public SymbolEditor(BasicSymbol symbol) {
        this.symbol = symbol;
    }

    public boolean edit() {
        boolean success = false;
        MilStd2525TacticalSymbol impl = symbol.getLookup().lookup(MilStd2525TacticalSymbol.class);
        MilStd2525TacticalSymbol implCopy = new MilStd2525TacticalSymbol(impl.getIdentifier(), impl.getPosition(), impl.copy());

        // Create the dialog content panel
        SymbolEditorPane dialogPane = new SymbolEditorPane(
                symbol.getName(),
                symbol.getCoordinates(),
                symbol.isMovable(),
                implCopy);

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

        // Update the symbol
        if (result != null && result == DialogDescriptor.OK_OPTION) {
            if (!symbol.getName().equals(dialogPane.getSymbolName())) {
                symbol.setName(dialogPane.getSymbolName());
            }
            if (symbol.isMovable() != dialogPane.isMovable()) {
                symbol.setMovable(dialogPane.isMovable());
            }
            //symbol.replaceTacticalSymbol(implCopy);

            success = true;
        }
        return success;

    }
}
