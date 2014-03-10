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
package com.emxsys.wmt.globe.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;


/**
 *
 * @author Bruce Schubert
 * @version $Id: ReticuleStatusLineComponent.java 381 2012-12-07 06:01:38Z bdschubert $
 */
public class ReticuleStatusLineComponent extends JLabel
{

    private static final Insets NULL_INSETS = new Insets(0, 0, 0, 0);
    private static final String MAX_LATITUDE_STRING = "___88\u00B0 88.88\' N__(-88.8888\u00B0)___";
    private static final String MAX_LONGITUDE_STRING = "___888\u00B0 88.88\' W__(-888.8888\u00B0)___";
    private static final String MAX_ELEVATION_STRING = "Elev 8,888,888 meters";
    private static final String MAX_SLOPE_STRING = "Slope 8888 deg";
    private static final String MAX_ASPECT_STRING = "Aspect 8888 deg";
    private static final Logger LOG = Logger.getLogger(ReticuleStatusLineComponent.class.getName());
    private Dimension minDimension;

    enum Type
    { // Type of component

        LATITUDE,
        LONGITUDE,
        ELEVATION,
        ASPECT,
        SLOPE
    }

    ReticuleStatusLineComponent(Type type)
    {
        switch (type)
        {
            case LATITUDE:
                initMinDimension(MAX_LATITUDE_STRING);
                break;
            case LONGITUDE:
                initMinDimension(MAX_LONGITUDE_STRING);
                break;
            case ELEVATION:
                initMinDimension(MAX_ELEVATION_STRING);
                break;
            case ASPECT:
                initMinDimension(MAX_ASPECT_STRING);
                break;
            case SLOPE:
                initMinDimension(MAX_SLOPE_STRING);
                break;
            default:
                throw new IllegalStateException();
        }
        setHorizontalAlignment(SwingConstants.CENTER);
    }


    @Override
    public Dimension getPreferredSize()
    {
        return minDimension;
    }


    private void initMinDimension(String... maxStrings)
    {
        FontMetrics fm = getFontMetrics(getFont());
        int minWidth = 0;
        for (String s : maxStrings)
        {
            minWidth = Math.max(minWidth, fm.stringWidth(s));
        }
        Border b = getBorder();
        Insets ins = (b != null) ? b.getBorderInsets(this) : NULL_INSETS;
        minWidth += ins.left + ins.right;
        int minHeight = fm.getHeight() + ins.top + ins.bottom;
        minDimension = new Dimension(minWidth, minHeight);
    }


}
