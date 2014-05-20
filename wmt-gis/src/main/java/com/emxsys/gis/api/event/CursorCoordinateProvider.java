/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.gis.api.event;

import com.emxsys.gis.api.Coord3D;
import com.emxsys.gis.api.event.CursorCoordinateListener;

/**
 * A CursorCoordinateProvider instance will generate a CursorCoordinateEvent when the cursor's 
 * geographical coordinate changes.
 *
 * @author Bruce Schubert
 */
public interface CursorCoordinateProvider {

    /**
     * Gets the current cursor coordinate.
     * @return the geographical cursor coordinate
     */
    Coord3D getCursorCoordinate();
    
    /**
     * Registers a CursorCoordinateListener on this provider. The listener will be notified when the
     * cursor coordinate changes.
     *
     * @param listener the listener to be registered.
     */
    void addCursorCoordinateListener(CursorCoordinateListener listener);


    /**
     * Unregisters a CursorChangeListener from this provider.
     *
     * @param listener the listener to be unregistered.
     */
    void removeCursorCoordinateListener(CursorCoordinateListener listener);
}
