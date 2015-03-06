/*
 * Copyright (c) 2009-2015, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.symbology.palette;

import java.awt.datatransfer.DataFlavor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.util.Lookup;


/**
 * Following are general instructions for implementing a PaletteController for a TopComponent. <br>
 * <pre>
 * <ol>
 * <li>Populate layer.xml with a folder hierarchy representing the palette categories. The
 * PaletteController references this folder. The root of the Globe palette folder is
 * "Globe/Palette".
 * <li>Implement a PaletteActions class. This is supplied to the PaletteController.
 * <li>Add a PaletteController object to a TopComponent's lookup. See createPalette() which uses
 * the NetBeans PaletteFactory method. <br/>
 * POPULATE THE PALETTE: <br/>
 * <li> Add palette item files
 * </ol>
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class PaletteSupport {

    public static final String GLOBE_PALETTE_FOLDER = "Globe Palette"; // layer.xml config folder
    public static final DataFlavor GRAPHIC_DATA_FLAVOR = new DataFlavor(GraphicPaletteData.class, "Graphic");
    private static final Logger logger = Logger.getLogger(PaletteSupport.class.getName());

    /**
     * Creates the Palette.
     *
     * @return
     */
    public static PaletteController createPalette() {
        try {
            return PaletteFactory.createPalette(GLOBE_PALETTE_FOLDER, new DummyActions());
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "createPalette() failed.", e);
            return null;
        }
    }


    /**
     * Palette Actions class.
     */
    private static class DummyActions extends PaletteActions {

        DummyActions() {
        }

        @Override
        public Action[] getImportActions() {
            return null;
        }

        @Override
        public Action[] getCustomPaletteActions() {
            return null;
        }

        @Override
        public Action[] getCustomCategoryActions(Lookup category) {
            return null;
        }

        @Override
        public Action[] getCustomItemActions(Lookup item) {
            return null;
        }

        @Override
        public Action getPreferredAction(Lookup item) {
            return null;
        }
    }
}
