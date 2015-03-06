
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
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;


/**
 * Manages the properties of an item in the palette.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: GraphicPaletteData.java 209 2012-09-05 23:09:19Z bdschubert $
 */
public class GraphicPaletteData
{



    private Properties props;
    private Image icon16;
    private Image icon32;
    private static final String PROP_TYPE = "type";
    private static final String PROP_IDENTIFIER = "identifier";
    private static final String PROP_NAME = "display_name";
    private static final String PROP_COMMENT = "comment";
    private static final String PROP_ICON16 = "icon_small";
    private static final String PROP_ICON32 = "icon_large";
    private static final String PROP_POINTS = "points";


    GraphicPaletteData(Properties props)
    {
        this.props = props;
        loadIcons();
    }


    private void loadIcons()
    {
        String iconId = props.getProperty(PROP_ICON16);
        icon16 = ImageUtilities.loadImage(iconId);

        iconId = props.getProperty(PROP_ICON32);
        icon32 = ImageUtilities.loadImage(iconId);
    }


    public String getType()
    {
        return props.getProperty(PROP_TYPE);
    }


    public String getIdentifier()
    {
        return props.getProperty(PROP_IDENTIFIER);
    }


    public List<Point> getPoints()
    {
        ArrayList<Point> points = new ArrayList<Point>();
        
        try
        {
            String[][] strings = fetchArrayFromPropFile(PROP_POINTS, props);
            for (int i = 0; i < strings.length; i++)
            {
                String[] xy = strings[i];
                int x = Integer.parseInt(xy[0].trim());
                int y = Integer.parseInt(xy[1].trim());
                points.add(new Point(x, y));
            }
        }
        catch (Exception exception)
        {
            Exceptions.printStackTrace(exception);
        }
        return points;
    }


    public String getDisplayName()
    {
        return props.getProperty(PROP_NAME);
    }


    public String getComment()
    {
        return props.getProperty(PROP_COMMENT);
    }


    public String getIconBase()
    {
        return props.getProperty(PROP_ICON16);
    }


    public String getSmallImagePath()
    {
        return props.getProperty(PROP_ICON16);
    }


    public String getLargeImagePath()
    {
        return props.getProperty(PROP_ICON32);
    }


    public Image getSmallImage()
    {
        return icon16;
    }


    public Image getLargeImage()
    {
        return icon32;
    }


    /**
     * Creates two dimensional array from delineated string in properties file
     *
     * @param propertyName name of the property as in the file
     * @param propFile the instance of the Properties file that has the property
     * @return two dimensional array
     */
    private static String[][] fetchArrayFromPropFile(String propertyName, Properties propFile)
    {

        //get array split up by the semicolin
        String[] a = propFile.getProperty(propertyName).split(";");

        //create the two dimensional array with correct size
        String[][] array = new String[a.length][a.length];

        //combine the arrays split by semicolin and comma 
        for (int i = 0; i < a.length; i++)
        {
            array[i] = a[i].split(",");
        }
        return array;
    }
}
