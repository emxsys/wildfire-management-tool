/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.globe.symbology;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525Constants;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525IconRetriever;
import java.awt.Image;
import java.awt.image.BufferedImage;
import org.openide.util.RequestProcessor;

/**
 * This singleton class generates icons representing MilStd2525C symbols.
 *
 * @author Bruce Schubert
 */
public class SymbolIconRetriever {

    private final MilStd2525IconRetriever iconRetriever;
    private final RequestProcessor requestProcessor = new RequestProcessor(SymbolIconRetriever.class);

    /**
     * Constructs the icon retriever.
     */
    private SymbolIconRetriever() {
        // Create an icon retriever using the path specified in the config file, or the default path.
        String iconRetrieverPath = Configuration.getStringValue(
                AVKey.MIL_STD_2525_ICON_RETRIEVER_PATH, MilStd2525Constants.DEFAULT_ICON_RETRIEVER_PATH);
        this.iconRetriever = new MilStd2525IconRetriever(iconRetrieverPath);
    }

    /**
     * Gets a BufferedImage representing the supplied symbol.
     *
     * @param symbol contains the MilStd2525 identifier and modifiers
     * @return Buffered Image icon representing the symbol
     */
    public Image getSymbolImage(BasicSymbol symbol) {
        AVList params = new AVListImpl();

        BufferedImage image = iconRetriever.createIcon(symbol.getIdentifier(), params);
        return image;
        //                // Create an icon with the default parameters.
        //                BufferedImage image = iconRetriever.createIcon("SFAPMFQM--GIUSA", params);
        //
        //                // Create a unframed icon.
        //                params.setValue(SymbologyConstants.SHOW_FRAME, false);
        //                image = iconRetriever.createIcon("SFAPMFQM--GIUSA", params);
        //
        //                // Create a framed icon with no fill.
        //                params.setValue(SymbologyConstants.SHOW_FRAME, true);
        //                params.setValue(SymbologyConstants.SHOW_FILL, false);
        //                image = iconRetriever.createIcon("SFAPMFQM--GIUSA", params);
        //
        //                // Create an icon with a custom color.
        //                params.setValue(AVKey.COLOR, Color.GREEN);
        //                params.setValue(SymbologyConstants.SHOW_FRAME, true);
        //                params.setValue(SymbologyConstants.SHOW_FILL, true);
        //                image = iconRetriever.createIcon("SFAPMFQM--GIUSA", params);
    }

    /**
     * Updates the image in the supplied symbol in a background thread.
     *
     * @param symbol to be updated in a callback.
     */
    public void postUpdateSymbolImage(final BasicSymbol symbol) {
        requestProcessor.post(() -> {
            symbol.setImage(getSymbolImage(symbol));
        });
    }

    /**
     * Gets the singleton.
     *
     * @return the singleton instance
     */
    public static SymbolIconRetriever getDefault() {
        return SymbolIconRetrieverHolder.INSTANCE;
    }

    private static class SymbolIconRetrieverHolder {

        private static final SymbolIconRetriever INSTANCE = new SymbolIconRetriever();
    }
}
