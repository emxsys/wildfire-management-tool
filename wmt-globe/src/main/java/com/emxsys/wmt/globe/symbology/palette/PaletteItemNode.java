/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.BeanInfo;
import java.io.IOException;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.datatransfer.ExTransferable;

/**
 * This class returns the appropriate DnD DATA_FLAVOR in the node's drag operation.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class PaletteItemNode extends DataNode {

    Object data;
    private Image smallImage;
    private Image largeImage;

    public PaletteItemNode(DataObject obj, Object data) {
        super(obj, Children.LEAF);
        this.data = data;
        if (data instanceof GraphicPaletteData) {
            setDisplayName(((GraphicPaletteData) data).getDisplayName());
            setShortDescription(((GraphicPaletteData) data).getComment());
            this.smallImage = ((GraphicPaletteData) data).getSmallImage();
            this.largeImage = ((GraphicPaletteData) data).getLargeImage();
        }
        if (this.smallImage == null) {
            this.smallImage = ImageUtilities.loadImage("com/emxsys/wmt/globe/images/favorite.png");
        }
        if (this.largeImage == null) {
            this.largeImage = ImageUtilities.loadImage("com/emxsys/wmt/globe/images/favorite24.png");
        }
    }

    @Override
    public Image getIcon(int type) {
        if (type == BeanInfo.ICON_COLOR_16x16
                || type == BeanInfo.ICON_MONO_16x16) {
            return this.smallImage;
        } else {
            return this.largeImage;
        }
    }

    /**
     * Allows a symbol node to be dragged from the palette
     *
     * @return
     * @throws IOException
     */
    @Override
    public Transferable drag() throws IOException {
        ExTransferable retValue = ExTransferable.create(super.drag());
        // Add data to the Transferable:
        if (data instanceof GraphicPaletteData) {
            retValue.put(new ExTransferable.Single(PaletteSupport.GRAPHIC_DATA_FLAVOR) {

                @Override
                protected Object getData() throws IOException, UnsupportedFlavorException {
                    return PaletteItemNode.this.data;
                }
            });
        }
        return retValue;
    }
}
