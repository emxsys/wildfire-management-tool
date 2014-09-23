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
 * ANY SpdECT, INSpdECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.emxsys.weather.panels;

import com.emxsys.visad.GeneralUnit;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.weather.api.WeatherPreferences;
import javax.swing.JOptionPane;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import visad.Real;
import visad.VisADException;

/**
 * Unit test and interactive layout test for the WindSpdChartPanel.
 * 
 * Comment out @Ignore to run the interactive layout test.
 * @author Bruce Schubert
 */
public class WindSpdChartPanelTest {

    public WindSpdChartPanelTest() {
    }

    @Ignore("interactive test")
    @Test
    public void testBehavior() {
        System.out.println("testBehavior - interactive");
        WindSpdChartPanel instance = new WindSpdChartPanel();
        instance.setTitle("Wind Speed @ 10 KPH to MPH");
        instance.setUnit(GeneralUnit.mph);
        instance.setWindSpeed(new Real(WeatherType.WIND_SPEED_KPH, 10.0));
        assertTrue("Form was invalidated by the user",
                JOptionPane.showConfirmDialog(
                        null, // frame
                        instance,
                        "Is Form Valid?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null) == JOptionPane.YES_OPTION);
        
        instance.setUnit(GeneralUnit.kph);
        assertTrue("Form was invalidated by the user",
                JOptionPane.showConfirmDialog(
                        null, // frame
                        instance,
                        "Does dial inidicate 10 KPH?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null) == JOptionPane.YES_OPTION);

        WeatherPreferences.setWindSpeedUom(WeatherPreferences.UOM_KTS);
        assertTrue("Form was invalidated by the user",
                JOptionPane.showConfirmDialog(
                        null, // frame
                        instance,
                        "Does dial inidicate Knots?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null) == JOptionPane.YES_OPTION);
    }

    @Test
    public void testSetWindSpeed() throws VisADException {
        System.out.println("setWindSpeed/getWindSpeed");
        Real speed = new Real(WeatherType.WIND_SPEED_KTS, 50);
        Real expResult = new Real(WeatherType.WIND_SPEED_KPH, speed.getValue(GeneralUnit.kph));
        
        WindSpdChartPanel instance = new WindSpdChartPanel();
        instance.setUnit(GeneralUnit.kph);
        instance.setWindSpeed(speed);
        
        Real result = instance.getWindSpeed();
        assertEquals(expResult.getValue(), result.getValue(), 0.0001);
    }

}
