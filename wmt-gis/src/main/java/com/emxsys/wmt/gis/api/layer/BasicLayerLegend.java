/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.gis.api.layer;

import com.emxsys.wmt.util.ImageUtil;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;


/**
 * This class provides a collection of ImageIcons that comprise a legend for a GisLayer. A GisLayer
 * that wants a legend should add an instance of this class to its lookup, and populate it with
 * icons.
 *
 * @see ImageIcon
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: BasicLayerLegend.java 234 2012-10-04 21:44:23Z bdschubert $
 */
public class BasicLayerLegend implements LayerLegend
{

    private static int DEFAULT_IMAGE_WIDTH = 16;
    private static int DEFAULT_IMAGE_HEIGHT = 16;
    private ArrayList<ImageIcon> icons = new ArrayList<ImageIcon>();
    private int imageWidth;
    private int imageHeight;


    /**
     * Constructor for 16x16 image icons.
     */
    public BasicLayerLegend()
    {
        this(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
    }


    /**
     * Constructor for user defined icon dimensions.
     *
     * @param imageWidth width of the icons.
     * @param imageHeight height of the icons.
     */
    public BasicLayerLegend(int imageWidth, int imageHeight)
    {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }


    /**
     * Gets the collection of icons.
     *
     * @return image icons.
     */
    @Override
    public List<ImageIcon> getIcons()
    {
        return icons;
    }


    /**
     * Adds a new image icon to the legend with the supplied color and description.
     *
     * @param color color for the image icon.
     * @param desc description for the image icon.
     */
    public void add(Color color, String desc)
    {
        BufferedImage image = ImageUtil.createRgbImage(this.imageWidth, this.imageHeight, color);
        ImageIcon imageIcon = new ImageIcon(image, desc);
        add(imageIcon);
    }


    /**
     * Adds the supplied image icon to the legend.
     *
     * @param icon image icon to add.
     */
    public void add(ImageIcon icon)
    {
        icons.add(icon);
    }
}
