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

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.GisType;
import com.emxsys.wmt.util.HttpUtil;
import com.emxsys.wmt.util.ImageUtil;
import com.emxsys.wmt.visad.GeneralUnit;
import com.emxsys.wmt.visad.Reals;
import com.emxsys.wmt.weather.api.AbstractWeatherProvider;
import com.emxsys.wmt.weather.api.WeatherProvider;
import static com.emxsys.wmt.weather.api.WeatherType.*;
import java.net.URL;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import visad.Field;
import visad.FlatField;
import visad.FunctionType;
import visad.Irregular2DSet;
import visad.Real;
import visad.RealTupleType;
import visad.RealType;
import visad.VisADException;

/**
 * MesoWest weather services.
 *
 * @author Bruce Schubert
 */
@ServiceProvider(service = WeatherProvider.class)
public class MesoWestWeatherProvider extends AbstractWeatherProvider {

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
            + "&jsonformat=2"
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
    /** MathType to represent the weather: air_temp, RH, wind_spd, wind_dir */
    protected static final RealTupleType WX_RANGE = Reals.newRealTupleType(
            new RealType[]{AIR_TEMP_F, REL_HUMIDITY, WIND_SPEED_KTS, WIND_DIR});
    protected static final int AIR_TEMP_IDX = WX_RANGE.getIndex(AIR_TEMP_F);
    protected static final int HUMIDITY_IDX = WX_RANGE.getIndex(REL_HUMIDITY);
    protected static final int WIND_SPD_IDX = WX_RANGE.getIndex(WIND_SPEED_KTS);
    protected static final int WIND_DIR_IDX = WX_RANGE.getIndex(WIND_DIR);
    /** Singleton */
    private static MesoWestWeatherProvider instance;
    private static final Logger logger = Logger.getLogger(MesoWestWeatherProvider.class.getName());

    /**
     * Get the singleton instance.
     * @return the singleton YahooPlaceProvider found on the global lookup.
     */
    static public MesoWestWeatherProvider getInstance() {
        if (instance == null) {
            // Calls default constructor
            Lookup.Result<WeatherProvider> result = Lookup.getDefault().lookupResult(WeatherProvider.class);
            for (Lookup.Item<WeatherProvider> item : result.allItems()) {
                if (item.getType().equals(MesoWestWeatherProvider.class)){
                    return (MesoWestWeatherProvider) item.getInstance();
                }
            }
        }
        return instance;
    }

    /**
     * Do not call! Used by @ServiceProvider.
     *
     * Use getInstance() or Lookup.getDefault().lookup(MesoWestWeatherProvider.class) instead.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public MesoWestWeatherProvider() {
        // Catches the second invocation, which should indicate incorrect usage.
        if (instance != null) {
            throw new IllegalStateException("Do not call constructor. Use getInstance() or Lookup.");
        }
        instance = this;
    }


    /**
     * Gets the latest weather observations within the area of interest.
     * @param coord The center of the area of interest.
     * @param radius The radius of the area of interest; the value will be converted to miles.
     * @return A {@code FlatField}: ( (lat, lon ) -> ( air_temp, RH, wind_spd, wind_dir ) )
     */
    public Field getLatestWeather(Coord2D coord, Real radius) {
        try {
            // Build the query string and URL
            String query = String.format(LATEST_WX_RADIUS_QUERY,
                    coord.getLatitudeDegrees(), coord.getLongitudeDegrees(),
                    radius.getValue(GeneralUnit.mile), APP_TOKEN);
            StringBuilder sb = new StringBuilder();
            sb.append(STATIONS_URI);
            sb.append(query);
            URL url = new URL(sb.toString());
            logger.info(url.toString());
            
            // Invoke the REST service and get the JSON results
            String jsonResult = HttpUtil.callWebService(url);
            
            //System.out.println(jsonResult);
            return parseJsonResults(jsonResult);
        
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the latest weather observations within the age and inside the area of interest.
     * @param coord The center of the area of interest.
     * @param radius The radius of the area of interest; the value will be converted to miles.
     * @return A {@code FlatField}: ( (lat, lon ) -> ( air_temp, RH, wind_spd, wind_dir ) )
     */
    public Field getLatestWeather(Coord2D coord, Real radius, Duration age) {
        try {
            // Build the query string and URL
            String query = String.format(LATEST_WX_RADIUS_AGE_QUERY,
                    coord.getLatitudeDegrees(), coord.getLongitudeDegrees(),
                    radius.getValue(GeneralUnit.mile), age.toMinutes(), APP_TOKEN);
            StringBuilder sb = new StringBuilder();
            sb.append(STATIONS_URI).append(query);
            String urlString = sb.toString();

            // Invoke the REST service and get the JSON results
            logger.info(urlString);
            String jsonResult = HttpUtil.callWebService(urlString);
            //System.out.println(jsonResult);
            return parseJsonResults(jsonResult);

        } catch (RuntimeException ex) {
            logger.severe(ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    private Field parseJsonResults(String jsonResult) throws NumberFormatException, ParseException, VisADException, java.text.ParseException, RemoteException {
        // Parse the JSON. JSONParser is not thread safe.
        JSONObject results;
        synchronized (JSONParser.class) {
            JSONParser parser = new JSONParser();
            results = (JSONObject) parser.parse(jsonResult);
        }
        JSONObject summary = (JSONObject) results.get("SUMMARY");
        Number numObjects = (Number) summary.get("NUMBER_OF_OBJECTS");

        // Create the station domain samples
        float[][] latLonSamples = new float[2][numObjects.intValue()];

        // Create the wx range samples, and init with "missing" values
        double[][] wxSamples = new double[WX_RANGE.getDimension()][numObjects.intValue()];
        for (int dim = 0; dim < WX_RANGE.getDimension(); dim++) {
            Arrays.fill(wxSamples[dim], Double.NaN);
        }

        JSONObject units = (JSONObject) results.get("UNITS");
        JSONArray stations = (JSONArray) results.get("STATION");
        int i = 0;
        for (Iterator it = stations.iterator(); it.hasNext();) {
            JSONObject station = (JSONObject) it.next();
            JSONObject obs = (JSONObject) station.get("OBSERVATIONS");
            String name = (String) station.get("NAME");
            String lat = (String) station.get("LATITUDE");
            String lon = (String) station.get("LONGITUDE");
            //System.out.println(name + " : " + lat + ", " + lon);

            // Store the domain sample
            latLonSamples[0][i] = Float.valueOf(lat);
            latLonSamples[1][i] = Float.valueOf(lon);

            // The sensor values come to us in a two element array. The first element is an index 
            // into the date_time array, the second element is the value. Sometimes a stale element 
            // is stuck in the data. We detect this by examining the values in the date_time array.
            // Create an array of date_time "ageHrs" to validate usefullness of sensor values.
            JSONArray times = (JSONArray) obs.get(DATE_TIME);
            long[] ageHrs = new long[times.size()];
            for (int j = 0; j < times.size(); j++) {
                String time = (String) times.get(j);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime then = LocalDateTime.parse(time, formatter);
                LocalDateTime now = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime();
                ageHrs[j] = Duration.between(then, now).toHours();
            }

            // Store the wx range samples that exist in this station
            JSONArray sensors = (JSONArray) station.get("SENSOR_VARIABLES");
            for (Object obj : sensors) {
                String sensor = (String) (obj);
                if (sensor.equals(DATE_TIME)) {
                    continue;
                }
                Number dateIndex = (Number) ((JSONArray) obs.get(sensor)).get(0);
                Number value = (Number) ((JSONArray) obs.get(sensor)).get(1);
                // Skip stale observations that are truely erroneous
                if (ageHrs[dateIndex.intValue()] > 24) {
                    logger.log(Level.INFO, "Ignoring {0} sensor variable {1}. Appears to be stale: {2} ({3} hours old).",
                            new Object[]{name, sensor, times.get(dateIndex.intValue()), ageHrs[dateIndex.intValue()]});
                    continue;
                }
                switch (sensor) {
                    case AIR_TEMP:
                        wxSamples[AIR_TEMP_IDX][i] = value.doubleValue();
                        break;
                    case RELATIVE_HUMIDITY:
                        wxSamples[HUMIDITY_IDX][i] = value.doubleValue();
                        break;
                    case WIND_SPEED:
                        wxSamples[WIND_SPD_IDX][i] = value.doubleValue();
                        break;
                    case WIND_DIRECTION:
                        wxSamples[WIND_DIR_IDX][i] = value.doubleValue();
                        break;
                    default:
                        logger.log(Level.INFO, "Sensor + [{0}] value [{1}] not processed.",
                                new Object[]{sensor, value.toString()});
                }
            }
            ++i;
        }
        // Create the domain Set, with 5 columns and 6 rows, using an
        // Gridded2DDoubleSet(MathType type, double[][] samples, lengthX)
        Irregular2DSet domainSet = new Irregular2DSet(GisType.LATLON, latLonSamples);

        // Create a MathType for the function ( (lat, lon ) -> ( air_temp, RH, wind_spd, ... ) )
        FunctionType stationWxFunc = new FunctionType(GisType.LATLON, WX_RANGE);

        // Create a FlatField
        // Use FlatField(FunctionType type, Set domain_set)
        FlatField values_ff = new FlatField(stationWxFunc, domainSet);

        // ...and put the weather values above into it
        values_ff.setSamples(wxSamples);
        return values_ff;
    }

    @Override
    public ImageIcon getImageIcon() {
        return ImageUtil.createImageIconFromResource("images/mesowest.png", getClass());
    }

}
