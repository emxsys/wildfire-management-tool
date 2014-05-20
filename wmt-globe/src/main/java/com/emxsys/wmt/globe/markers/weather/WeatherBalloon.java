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
package com.emxsys.wmt.globe.markers.weather;

import com.emxsys.wmt.globe.util.Positions;
import com.emxsys.weather.api.PointForecastPresenter;
import com.emxsys.weather.api.WeatherProvider;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BalloonAttributes;
import gov.nasa.worldwind.render.BasicBalloonAttributes;
import gov.nasa.worldwind.render.GlobeBrowserBalloon;
import gov.nasa.worldwind.render.Size;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The WeatherBalloon class is responsible for displaying HTML forecast presentations attached
 * to a WeatherMarker.
 * 
 * @author Bruce Schubert
 */
public final class WeatherBalloon extends GlobeBrowserBalloon {

    private String html;
    private WeatherProvider provider;
    private static final Logger logger = Logger.getLogger(WeatherBalloon.class.getName());


    public WeatherBalloon(Position position, WeatherProvider provider) {
        super("Not supported.", position);
        this.provider = provider;

        BalloonAttributes attrs = new BasicBalloonAttributes();
        attrs.setSize(new Size(Size.NATIVE_DIMENSION, 0d, null, Size.NATIVE_DIMENSION, 0d, null));
        //attrs.setSize(Size.fromPixels(512, 256));
        setAttributes(attrs);

        // Setup the balloon to restore the last looked at page.
        setVisibilityAction(AVKey.VISIBILITY_ACTION_RETAIN);
        setVisible(false);
        setAlwaysOnTop(true);
    }

    public void setProvider(WeatherProvider provider) {
        this.provider = provider;
        updatePage();
    }

    
    @Override
    public void setVisible(boolean visible) {
        if (!isVisible() && visible) {
            // Deferred loading of web page
            if (html == null) {
                updatePage();
            }
        }
        super.setVisible(visible);
    }

    @Override
    public void setPosition(Position newPos) {
        // Hide the balloon when moving the marker.
        if (isVisible()) {
            setVisible(false);
        }

        Position oldPos = getPosition();
        super.setPosition(newPos);

        // Update the web page if the positon changes
        if (!oldPos.equals(newPos)) {
            updatePage();
        }
    }
    
    

    private void updatePage() {
        Position curPos = getPosition();
        if (provider == null) {
            setText(html = "No weather provider.");
            return;
        }
        PointForecastPresenter presenter = provider.getLookup().lookup(PointForecastPresenter.class);
        if (presenter == null) {
            setText(html = "No point forecaster available.");
            return;
        }
        html = presenter.getPresentation(Positions.toGeoCoord2D(curPos));
        setText(html);
    }

}
