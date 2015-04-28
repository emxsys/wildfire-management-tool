/*
 * Copyright (c) 2015, Bruce Schubert <bruce@emxsys.com>
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
package com.emxsys.wmt.web;

import com.emxsys.gis.api.BasicTerrain;
import com.emxsys.util.XmlUtil;
import com.emxsys.weather.api.BasicWeather;
import com.emxsys.weather.api.WeatherType;
import com.emxsys.wildfire.api.BasicFuelModel;
import com.emxsys.wildfire.api.BasicFuelMoisture;
import com.emxsys.wildfire.api.WeatherConditions;
import com.emxsys.wildfire.api.WildfireType;
import static com.emxsys.wildfire.api.WildfireType.ASPECT;
import static com.emxsys.wildfire.api.WildfireType.ELEVATION;
import static com.emxsys.wildfire.api.WildfireType.SLOPE;
import com.emxsys.wildfire.behavior.SurfaceFire;
import com.emxsys.wildfire.behavior.SurfaceFireProvider;
import com.emxsys.wildfire.behavior.SurfaceFuel;
import com.emxsys.wildfire.behavior.SurfaceFuelProvider;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.test.framework.JerseyTest;
import javax.ws.rs.core.MediaType;
import static javax.ws.rs.core.MediaType.*;
import org.junit.Test;
import static org.junit.Assert.*;
import visad.Real;

/**
 *
 * @author Bruce Schubert
 */
public class SurfaceFireResourceTest extends JerseyTest {

    public SurfaceFireResourceTest() throws Exception {
        super("com.emxsys.wmt.web");
    }

    @Test
    public void testCreateSurfaceFire() {
        System.out.println("TESTING: createSurfaceFire");

        // Get FuelModel
        ClientResponse fuelModelResponse = super.webResource.path("fuelmodels/6")
                .accept(MediaType.APPLICATION_JSON)
                .get(ClientResponse.class);
        assertTrue("Status: expected 200 but got " + fuelModelResponse.getStatus(), fuelModelResponse.getStatus() == 200);
        BasicFuelModel fuelModel = fuelModelResponse.getEntity(BasicFuelModel.class);
        
        // Get FuelMoisture
        ClientResponse fuelMoistureResponse = super.webResource.path("fuelmoisture")
                .queryParam("conditions", "hot_and_dry")
                .accept(APPLICATION_JSON)
                .get(ClientResponse.class);
        assertTrue("Status: expected 200 but got " + fuelMoistureResponse.getStatus(), fuelMoistureResponse.getStatus() == 200);
        BasicFuelMoisture fuelMoisture = fuelMoistureResponse.getEntity(BasicFuelMoisture.class);
        
        // Get the SurfaceFuel
        FormDataMultiPart formDataFuel = new FormDataMultiPart();
        formDataFuel.field("fuelModel", fuelModel, APPLICATION_JSON_TYPE);
        formDataFuel.field("fuelMoisture", fuelMoisture, APPLICATION_JSON_TYPE);
        ClientResponse fuelResponse = super.webResource.path("surfacefuel")
                .type(MULTIPART_FORM_DATA_TYPE)
                .accept(APPLICATION_JSON)
                .post(ClientResponse.class, formDataFuel);
        assertTrue("Status: expected 200 but got " + fuelResponse.getStatus(), fuelResponse.getStatus() == 200);

        // Test the SurfaceFire
        // Fuel param
        SurfaceFuel fuel = fuelResponse.getEntity(SurfaceFuel.class);
        // Weather param
        BasicWeather weather = new BasicWeather();
        weather.setWindSpeed(new Real(WeatherType.WIND_SPEED_MPH, 5));
        weather.setWindDirection(new Real(WeatherType.WIND_DIR, 90));
        // Terrain param
        BasicTerrain terrain = new BasicTerrain();
        terrain.setAspect(new Real(ASPECT, 235));
        terrain.setSlope(new Real(SLOPE, 20));   
        terrain.setElevation(new Real(ELEVATION, 100));   
        
        SurfaceFireProvider provider = new SurfaceFireProvider();
        SurfaceFire expResult = provider.getFireBehavior(fuel, weather, terrain);
        //System.out.println("Fuel:\n" + fuel.toString());  
        //System.out.println("Weather:\n" + weather.toString());  
        //System.out.println("Terrain:\n" + terrain.toString());  
        
        FormDataMultiPart formDataFire = new FormDataMultiPart();
        formDataFire.field("fuel", fuel, APPLICATION_JSON_TYPE);
        formDataFire.field("weather", weather, APPLICATION_JSON_TYPE);
        formDataFire.field("terrain", terrain, APPLICATION_JSON_TYPE);      
        ClientResponse fireResponse = super.webResource.path("surfacefire")
                .type(MULTIPART_FORM_DATA_TYPE)
                .accept(TEXT_PLAIN)
                .post(ClientResponse.class, formDataFire);
        assertTrue("Status: expected 200 but got " + fireResponse.getStatus(), fireResponse.getStatus() == 200);
        assertTrue("Expecting: " + TEXT_PLAIN + " but found: " + fireResponse.getType(),
                fireResponse.getType().equals(TEXT_PLAIN_TYPE));
        String result = fireResponse.getEntity(String.class);
        assertNotNull(result);
        System.out.println("Expected Fire Behavior:\n" + expResult.toString());  
        System.out.println("Resulting Fire Behavior:\n" + result);
        assertTrue(result.equals(expResult.toString()));
//        SurfaceFire entity = fireResponse.getEntity(SurfaceFire.class);
//        System.out.println("SurfaceFire Result >>>>\n" + entity.toString());  
//        System.out.println("SurfaceFire Expected >>>>\n" + expResult.toString());  
//        assertTrue(entity.equals(expResult));
        
    }
    @Test
    public void testGetXml() {
        System.out.println("TESTING: getXml");

        // FuelModel
        BasicFuelModel fuelModel = BasicFuelModel.from(6);
        // Get FuelMoisture
        BasicFuelMoisture fuelMoisture = BasicFuelMoisture.fromWeatherConditions(WeatherConditions.HOT_AND_DRY);
        // SurfaceFuel param
        SurfaceFuel fuel = SurfaceFuel.from(fuelModel, fuelMoisture);
        // Weather param
        BasicWeather weather = new BasicWeather();
        weather.setWindSpeed(new Real(WeatherType.WIND_SPEED_MPH, 5));
        weather.setWindDirection(new Real(WeatherType.WIND_DIR, 90));
        // Terrain param
        BasicTerrain terrain = new BasicTerrain();
        terrain.setAspect(new Real(ASPECT, 235));
        terrain.setSlope(new Real(SLOPE, 20));   
        terrain.setElevation(new Real(ELEVATION, 100));   
        
        SurfaceFireProvider provider = new SurfaceFireProvider();
        SurfaceFire expResult = provider.getFireBehavior(fuel, weather, terrain);
        //System.out.println("Fuel:\n" + fuel.toString());  
        //System.out.println("Weather:\n" + weather.toString());  
        //System.out.println("Terrain:\n" + terrain.toString());  
        
        FormDataMultiPart formDataFire = new FormDataMultiPart();
        formDataFire.field("fuel", fuel, APPLICATION_JSON_TYPE);
        formDataFire.field("weather", weather, APPLICATION_JSON_TYPE);
        formDataFire.field("terrain", terrain, APPLICATION_JSON_TYPE);      
        ClientResponse response = super.webResource.path("surfacefire")
                .type(MULTIPART_FORM_DATA_TYPE)
                .accept(APPLICATION_XML)
                .post(ClientResponse.class, formDataFire);
        assertTrue("Status: expected 200 but got " + response.getStatus(), response.getStatus() == 200);
        assertTrue("Expecting: " + APPLICATION_XML + " but found: " + response.getType(),
                response.getType().equals(APPLICATION_XML_TYPE));
//        System.out.println("SurfaceFire Result >>>>\n" + XmlUtil.format(response.getEntity(String.class)));  
        SurfaceFire result = response.getEntity(SurfaceFire.class);
        
        System.out.println("SurfaceFire Result >>>>\n" + result.toString());  
        System.out.println("SurfaceFire Expected >>>>\n" + expResult.toString());  
        assertTrue(result.equals(expResult));
        assertTrue(result.toString().equals(expResult.toString()));
        
    }

}
