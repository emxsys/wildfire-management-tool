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
package com.emxsys.wmt.visad;


import com.emxsys.wmt.core.capabilities.PanDownCapability;
import com.emxsys.wmt.core.capabilities.PanLeftCapability;
import com.emxsys.wmt.core.capabilities.PanRightCapability;
import com.emxsys.wmt.core.capabilities.PanUpCapability;
import com.emxsys.wmt.core.capabilities.RotateCcwCapability;
import com.emxsys.wmt.core.capabilities.RotateCwCapability;
import com.emxsys.wmt.core.capabilities.ZoomInCapability;
import com.emxsys.wmt.core.capabilities.ZoomOutCapability;

import java.awt.event.ActionEvent;

import visad.KeyboardBehavior;
import visad.java2d.KeyboardBehaviorJ2D;
import visad.java3d.KeyboardBehaviorJ3D;


/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: DisplayController.java 423 2012-12-10 21:30:24Z bdschubert $
 */
public class DisplayController
        implements PanUpCapability, PanDownCapability,
                   PanLeftCapability, PanRightCapability,
                   RotateCcwCapability, RotateCwCapability,
                   ZoomInCapability, ZoomOutCapability
{

    private KeyboardBehavior kb;
    private boolean mode2D;


    public DisplayController(KeyboardBehavior kb)
    {
        this.kb = kb;
        this.mode2D = kb instanceof KeyboardBehaviorJ2D;
    }



    @Override
    public void panUp(ActionEvent event)
    {
        kb.execFunction(mode2D ? KeyboardBehavior.TRANSLATE_DOWN : KeyboardBehaviorJ3D.ROTATE_X_POS);
    }


    @Override
    public void panDown(ActionEvent event)
    {
        kb.execFunction(mode2D ? KeyboardBehavior.TRANSLATE_UP : KeyboardBehaviorJ3D.ROTATE_X_NEG);
    }


    @Override
    public void panLeft(ActionEvent event)
    {
        kb.execFunction(mode2D ? KeyboardBehavior.TRANSLATE_RIGHT : KeyboardBehaviorJ3D.ROTATE_Y_NEG);
    }


    @Override
    public void panRight(ActionEvent event)
    {
        kb.execFunction(mode2D ? KeyboardBehavior.TRANSLATE_LEFT : KeyboardBehaviorJ3D.ROTATE_Y_POS);
    }


    @Override
    public void rotateCounterClockwise(ActionEvent event)
    {
        kb.execFunction(KeyboardBehaviorJ3D.ROTATE_Z_NEG);
    }


    @Override
    public void rotateClockwise(ActionEvent event)
    {
        kb.execFunction(KeyboardBehaviorJ3D.ROTATE_Z_POS);
    }


    @Override
    public void zoomIn(ActionEvent event)
    {
        kb.execFunction(KeyboardBehavior.ZOOM_IN);
    }


    @Override
    public void zoomOut(ActionEvent event)
    {
        kb.execFunction(KeyboardBehavior.ZOOM_OUT);
    }
}
