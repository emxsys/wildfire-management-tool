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
package com.emxsys.wmt.weather.nws;

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.GeoCoord2D;
import com.emxsys.wmt.weather.api.PointForecaster;
import com.emxsys.wmt.weather.api.PointForecastPresenter;
import com.emxsys.wmt.weather.api.Weather;
import java.rmi.RemoteException;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import visad.DateTime;
import visad.Field;
import visad.Set;
import visad.VisADException;

/**
 *
 * @author Bruce Schubert
 */
public class NwsWeatherProviderTest {

    public NwsWeatherProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class NwsWeatherProvider.
     */
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        // 1st instance tests lookup
        NwsWeatherProvider result = NwsWeatherProvider.getInstance();
        assertNotNull(result);
        
        // 2nd call of constructor should throw exeception
        exception.expect(IllegalStateException.class);
        new NwsWeatherProvider();
    }

     /**
     * Test of getPointForecast method, of class NwsWeatherProvider.
     */
    @Test
    public void testGetPointForecast() throws VisADException, RemoteException {
        System.out.println("getPointForecast");
        NwsWeatherProvider provider = NwsWeatherProvider.getInstance();
        PointForecaster forecaster = provider.getLookup().lookup(PointForecaster.class);
        assertNotNull(forecaster);
        
        Coord2D coord = GeoCoord2D.fromDegrees(34.25, -119.2);
        Field forecast = forecaster.getForecast(coord);
        assertNotNull(forecast);
        
        System.out.println(forecast.getDomainSet().getType());
        double[][] times = forecast.getDomainSet().getDoubles();
        for (double time : times[0]) {
            System.out.println(new DateTime(time));
        }
        System.out.println(forecast.longString());

    }

    /**
     * Test of getPointForecastPage method, of class NwsWeatherProvider.
     */
    @Test
    public void testGetPointForecastPage() throws VisADException, RemoteException {
        System.out.println("getPointForecastPage");
        NwsWeatherProvider provider = NwsWeatherProvider.getInstance();
        PointForecastPresenter presentation = provider.getLookup().lookup(PointForecastPresenter.class);
        assertNotNull(presentation);
        
        Coord2D coord = GeoCoord2D.fromDegrees(34.25, -119.2);
        String html = presentation.getPresentation(coord);
        assertNotNull(html);
        
        System.out.println(html);
    }

}
