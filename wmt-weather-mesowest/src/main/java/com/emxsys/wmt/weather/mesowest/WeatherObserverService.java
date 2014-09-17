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
package com.emxsys.wmt.weather.mesowest;

import com.emxsys.util.HttpUtil;
import com.emxsys.visad.SpatialDomain;
import com.emxsys.visad.TemporalDomain;
import com.emxsys.weather.api.services.WeatherObserver;
import com.emxsys.weather.api.WeatherModel;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;
import org.openide.util.NbBundle;
import visad.VisADException;
import visad.georef.LatLonPoint;

/**
 * MesoWest weather services.
 *
 * See: http://mesowest.org/api/
 *
 * The following is an exhaustive list of the variables which can be requested through the API.
 * <pre>
 * Sensor Variable List
 *
 * Variable                 Description         Unit
 * ========                 ===========         ====
 * air_temp                 Temperature         Fahrenheit
 * dew_point_temperature 	Dew Point           Fahrenheit
 * relative_humidity        Relative Humidity 	%
 * wind_speed               Wind Speed          Knots
 * wind_direction           Wind Direction      Degrees
 * wind_gust                Wind Gust           Knots
 * solar_radiation          Solar Radiation 	W/m**2
 * soil_temp                Soil Temperature 	Fahrenheit
 * sea_level_pressure       Sea_level pressure 	Mb
 * pressure_1500_meter      1500 m Pressure 	Mb
 * altimeter                Altimeter           inches Hg
 * pressure                 Pressure            Mb
 * water_temp               Water Temperature 	Fahrenheit
 * cloud_layer_1_code       Cloud_layer_1 height/coverage 	code
 * cloud_layer_2_code       Cloud_layer_2 height/coverage 	code
 * cloud_layer_3_code       Cloud_layer_3 height/coverage 	code
 * visibility               Visibility 	Statute miles
 * cloud_low_symbol         Low_cloud symbol 	code
 * cloud_mid_symbol         Mid_cloud symbol 	code
 * cloud_high_symbol        High_cloud symbol 	code
 * weather_cond_code        Weather conditions 	code
 * pressure_tendency        Pressure Tendency 	code
 * qc                       Quality check flag 	code
 * remark                   Remarks             text
 * raw_ob                   Raw observation 	text
 * sun_hours                Hours of sun        Hours
 * road_sensor_num          Road sensor         number
 * road_temp                Road Temperature 	Fahrenheit
 * road_subsurface_tmp      Road Subsurface Temperature 	Fahrenheit
 * road_freezing_temp       Road_Freezing Temperature 	Fahrenheit
 * road_surface_condition 	Road_Surface Conditions 	code
 * air_temp_high_6_hour 	6 Hr High Temperature 	Fahrenheit
 * air_temp_low_6_hour      6 Hr Low Temperature 	Fahrenheit
 * air_temp_high_24_hour 	24 Hr High Temperature 	Fahrenheit
 * air_temp_low_24_hour 	24 Hr Low Temperature 	Fahrenheit
 * peak_wind_speed          Peak_Wind Speed         Knots
 * peak_wind_direction      Peak_Wind Direction     Degrees
 * fuel_temp                Fuel Temperature        Fahrenheit
 * fuel_moisture_ten_hour 	10_hr_Fuel Moisture 	gm
 * ceiling                  Ceiling             feet
 * sonic_wind_speed         Sonic_Wind Speed 	Knots
 * pressure_change_code 	Pressure change 	code
 * precip_smoothed          Precipitation smoothed 	Inches
 * soil_temp_ir             IR_Soil Temperature 	Fahrenheit
 * temp_in_case             Temperature in_case 	Fahrenheit
 * soil_moisture            Soil Moisture       %
 * volt                     Battery voltage 	volts
 * created_time_stamp       Data Insert Date/Time 	minutes
 * last_modified            Data Update Date/Time 	minutes
 * snow_smoothed            Snow smoothed       Inches
 * precip_accum_ten_minute 	Precipitation 10min 	Inches
 * precip_accum_three_hour 	Precipitation 3hr       Inches
 * precip_accum_fifteen_minute 	Precipitation 15min 	Inches
 * precip_accum_one_hour 	Precipitation 1hr 	Inches
 * precip_accum_five_minute Precipitation 5min 	Inches
 * precip_accum_six_hour 	Precipitation 6hr 	Inches
 * precip_accum_24_hour 	Precipitation 24hr 	Inches
 * precip_accum_30_minute 	Precipitation 30 min 	Inches
 * precip_accum             Precipitation accumulated 	Inches
 * precip_accum_one_minute 	Precipitation 1min 	Inches
 * snow_depth               Snow depth          Inches
 * snow_accum               Snowfall            Inches
 * precip_storm             Precipitation storm 	Inches
 * precip                   Precipitation manual 	Inches
 * precip_accum             Precipitation 1hr manual 	Inches
 * precip_accum_five_minute Precipitation 5min manual 	Inches
 * precip_accum_ten_minute 	Precipitation 10min manual 	Inches
 * precip_accum_fifteen_minute 	Precipitation 15min manual 	Inches
 * precip_accum_three_hour 	Precipitation 3hr manual 	Inches
 * precip_accum_six_hour 	Precipitation 6hr manual 	Inches
 * precip_accum_24_hour 	Precipitation 24hr manual 	Inches
 * snow_accum_manual        Snow manual         Inches
 * snow_interval            Snow interval       Inches
 * T_water_temp             Water Temperature 	Fahrenheit
 * evapotranspiration       Evapotranspiration 	inches
 * snow_water_equiv         Snow water equivalent 	inches
 * precipitable_water_vapor Precipitable water vapor 	Inches
 * precip_accum             Precipitation (weighing_gauge) 	Inches
 * net_radiation            Net_Radiation (all_wavelengths) 	W/m**2
 * soil_moisture_tension 	Soil Moisture tension 	centibars
 * air_temp_wet_bulb        Wet bulb temperature 	Fahrenheit
 * air_temp_2m              Air_Temperature at_2_meters 	Fahrenheit
 * air_temp_10m             Air_Temperature at_10_meters 	Fahrenheit
 * soil_temp1_18            18_Inch Soil_Temperature 	Fahrenheit
 * soil_temp2_18            18_Inch Soil_Temperature2 	Fahrenheit
 * soil_temp_20             20_Inch Soil_Temperature 	Fahrenheit
 * net_radiation_sw         Net_Radiation (short_wave) 	W/m**2
 * net_radiation_lw         Net_Radiation (long_wave) 	W/m**2
 * sonic_air_temp           Sonic Temperature       Fahrenheit
 * sonic_wind_direction 	Sonic_Wind Direction 	Degrees
 * sonic_vertical_vel       Vertical_Velocity Sonic 	m/s
 * ground_temp              Ground Temperature 	Fahrenheit
 * sonic_zonal_wind_stdev 	Zonal_Wind Standard_Deviation 	m/s
 * sonic_meridonial_wind_stdev 	Meridional_Wind Standard_Deviation 	m/s
 * sonic_vertical_wind_stdev 	Vertical_Wind Standard_Deviation 	m/s
 * sonic_air_temp_stdev 	Temperature Standard_Deviation 	Centigrade
 * vertical_heat_flux       Vertical Heat_Flux      m/s C
 * friction_velocity        Friction Velocity       m/s
 * w_ratio                  SIGW/USTR               nondimensional
 * sonic_ob_count           Sonic_Obs Total         nondimensional
 * sonic_warn_count         Sonic Warnings          nondimensional
 * moisture_stdev           Moisture Standard_Deviation 	g/m**3
 * vertical_moisture_flux 	Vertical Moisture_Flux 	m/s g/m**3
 * M_dew_point_temperature 	Dew Point               Fahrenheit
 * virtual_temp             Virtual Temperature 	Fahrenheit
 * geopotential_height      Geopotential Height 	Feet
 * outgoing_radiation_sw 	Outgoing_Radiation (short_wave) 	W/m**2
 * </pre>
 * @author Bruce Schubert
 */
public class WeatherObserverService implements WeatherObserver {

    /** URI for MesoWest Stations. */
    protected static final String STATIONS_URI = "http://api.mesowest.net/stations?";

    /** URI for MesoWest Networks. */
    protected static final String NETWORKS_URI = "http://api.mesowest.net/networks?";

    /** URI for MesoWest Network Types. */
    protected static final String NETWORKTYPES_URI = "http://api.mesowest.net/networktypes?";

    /** App token for registered 'WMT' project */
    protected static final String APP_TOKEN = NbBundle.getBundle("com.emxsys.wmt.branding.Bundle").getString("MESOWEST_APP_TOKEN");

    protected static final String DATE_TIME = "date_time";
    protected static final String AIR_TEMP = "air_temp";
    protected static final String RELATIVE_HUMIDITY = "relative_humidity";
    protected static final String WIND_SPEED = "wind_speed";
    protected static final String WIND_DIRECTION = "wind_direction";

    /** Latest weather query at lat/lon - params: lat, lon, radius (miles) [and app token] */
    protected static final String LATEST_WX_RADIUS_QUERY = "&status=active"
            + "&jsonformat=2"
            + "&latestobs=1"
            + "&vars="
            + AIR_TEMP + ","
            + RELATIVE_HUMIDITY + ","
            + WIND_SPEED + ","
            + WIND_DIRECTION + ","
            + "&obtimezone=utc"
            + "&jsonformat=2"
            + "&radius=%1$f,%2$f,%3$f"
            + "&token=%4$s";

    /** Latest weather query at lat/lon - params: lat, lon, radius (miles), age (minutes) [and app
     * token] */
    protected static final String LATEST_WX_RADIUS_AGE_QUERY = "&status=active"
            + "&latestobs=1"
            + "&within=%4$d"
            + "&vars="
            + AIR_TEMP + ","
            + RELATIVE_HUMIDITY + ","
            + WIND_SPEED + ","
            + WIND_DIRECTION + ","
            + "&obtimezone=utc"
            + "&jsonformat=2"
            + "&radius=%1$f,%2$f,%3$f"
            + "&token=%5$s";

    /** Latest weather query for station - params: station_id, age (minutes) [and app token] */
    protected static final String LATEST_WX_STATION_QUERY = "&status=active"
            + "&latestobs=1"
            + "&stid=%1$s"
            + "&vars="
            + AIR_TEMP + ","
            + RELATIVE_HUMIDITY + ","
            + WIND_SPEED + ","
            + WIND_DIRECTION + ","
            + "&obtimezone=utc"
            + "&jsonformat=2"
            + "&token=%2$s";

    /** Recorded weather query with a bounding box - params: minLon, minLat, maxLon, maxLat, start,
     * end [and app token */
    protected static final String RECORDED_WX_BOUNDING_BOX_QUERY = "&status=active"
            + "&vars="
            + AIR_TEMP + ","
            + RELATIVE_HUMIDITY + ","
            + WIND_SPEED + ","
            + WIND_DIRECTION + ","
            + "&obtimezone=utc"
            + "&jsonformat=2"
            + "&bbox=%1$f,%2$f,%3$f,%4$f" // minLon,minLat,maxLon,maxLat
            + "&start=%5$s" // start time
            + "&end=%6$s" // end time
            + "&token=%7$s";                // app token

    /** Latest weather query with a bounding box - params: minLon, minLat, maxLon, maxLat [and app
     * token */
    protected static final String LATEST_WX_BOUNDING_BOX_QUERY = "&status=active"
            + "&vars="
            + AIR_TEMP + ","
            + RELATIVE_HUMIDITY + ","
            + WIND_SPEED + ","
            + WIND_DIRECTION + ","
            + "&latestobs=1"
            + "&obtimezone=utc"
            + "&jsonformat=2"
            + "&bbox=%1$f,%2$f,%3$f,%4$f" // minLon,minLat,maxLon,maxLat
            + "&within=%5$d" // age in minutes
            + "&token=%6$s";                // app token

    private static final Logger logger = Logger.getLogger(WeatherObserverService.class.getName());

    /**
     * Gets the latest weather observations within the age and inside the area of interest.
     * @param areaOfInterest The geo-spatial region to be queried.
     * @param age The acceptable age of a sensor value; defaults to 24 hours if null.
     * @return A {@code WeatherModel}: (Lat, Lon) -> (Time -> (Weather))
     */
    @Override
    public WeatherModel getLatestObservations(SpatialDomain areaOfInterest, Duration age) {
        try {
            LatLonPoint minLatLon = areaOfInterest.getMinLatLon();
            LatLonPoint maxLatLon = areaOfInterest.getMaxLatLon();

            // Build the query string and URL
            String query = String.format(LATEST_WX_BOUNDING_BOX_QUERY,
                    minLatLon.getLongitude().getValue(),
                    minLatLon.getLatitude().getValue(),
                    maxLatLon.getLongitude().getValue(),
                    maxLatLon.getLatitude().getValue(),
                    age != null ? age.toMinutes() : 3600,
                    APP_TOKEN);
            StringBuilder sb = new StringBuilder();
            sb.append(STATIONS_URI).append(query);

            String urlString = sb.toString();
            logger.fine(urlString);
            System.out.println(urlString);

            // Invoke the REST service and get the JSON results
            String jsonResult = HttpUtil.callWebService(new URL(urlString));
            //System.out.println(jsonResult);

            WeatherModel wxModel = JsonParser.parseSingleObservation(jsonResult);
            if (wxModel == null) {
                logger.log(Level.SEVERE, "getCurrentConditions failed to process this URL: {0}", urlString);
            }
            return wxModel;

        } catch (IOException | NumberFormatException | ParseException | VisADException | java.text.ParseException ex) {
            logger.log(Level.SEVERE, "getCurrenConditions failed: {0}", ex.getMessage());
            throw new RuntimeException("getCurrenConditions failed.", ex);
        }
    }

    /**
     * Gets the latest weather observations within the age and inside the area of interest.
     * @param areaOfInterest The geographical area of interest to be queried.
     * @param timeframe The temporal period to be queried.
     * @return A {@code WeatherModel}: (Lat, Lon) -> (Time -> (Weather))
     */
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
    @Override
    public WeatherModel getObservations(SpatialDomain areaOfInterest, TemporalDomain timeframe) {
        try {
            LatLonPoint minLatLon = areaOfInterest.getMinLatLon();
            LatLonPoint maxLatLon = areaOfInterest.getMaxLatLon();
            ZonedDateTime start;
            ZonedDateTime end;
            if (timeframe != null) {
                start = timeframe.getEarliest();
                end = timeframe.getLatest();
            } else {
                end = ZonedDateTime.now();
                start = end.minusHours(1);
            }

            // Build the query string and URL
            String query = String.format(RECORDED_WX_BOUNDING_BOX_QUERY,
                    minLatLon.getLongitude().getValue(),
                    minLatLon.getLatitude().getValue(),
                    maxLatLon.getLongitude().getValue(),
                    maxLatLon.getLatitude().getValue(),
                    start.format(DateTimeFormatter.ISO_INSTANT).replaceAll("\\D+", "").substring(0, 12), // only the digits thru minutes
                    end.format(DateTimeFormatter.ISO_INSTANT).replaceAll("\\D+", "").substring(0, 12),
                    APP_TOKEN);
            StringBuilder sb = new StringBuilder();
            sb.append(STATIONS_URI).append(query);

            String urlString = sb.toString();
            logger.fine(urlString);
            System.out.println(urlString);

            // Invoke the REST service and get the JSON results
            String jsonResult = HttpUtil.callWebService(new URL(urlString));
            //System.out.println(jsonResult);

            return JsonParser.parseTimeSeries(jsonResult);

        } catch (RuntimeException ex) {
            logger.severe(ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

}
