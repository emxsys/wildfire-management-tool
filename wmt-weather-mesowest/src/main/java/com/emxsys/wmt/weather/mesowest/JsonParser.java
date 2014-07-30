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

import com.emxsys.weather.api.WeatherModel;
import com.emxsys.weather.api.WeatherType;
import static com.emxsys.weather.api.WeatherType.AIR_TEMP_INDEX;
import static com.emxsys.weather.api.WeatherType.FIRE_WEATHER;
import static com.emxsys.weather.api.WeatherType.REL_HUMIDITY_INDEX;
import static com.emxsys.weather.api.WeatherType.WIND_DIR_INDEX;
import static com.emxsys.weather.api.WeatherType.WIND_SPEED_INDEX;
import static java.lang.Double.max;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import visad.DateTime;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.Gridded1DDoubleSet;
import visad.Irregular2DSet;
import visad.Real;
import visad.RealTupleType;
import static visad.RealTupleType.LatitudeLongitudeTuple;
import visad.RealType;
import visad.Set;
import visad.Unit;
import visad.VisADException;

/**
 *
 * <pre>
 * JSON reference & description
 * ============================
 * json.STATION[0].OBSERVATIONS.pressure
 *  STATION lists each returned station as a seperate, but identically structured, object
 *  OBSERVATIONS is an object containing individual variables as keys
 *      *** Caution *** this can be confusing:
 *      For single data queries, this two-element list is [0]: date_time index and [1] data value.
 *      Otherwise, this contains a list of values whose index corresponds to the index of date_time.
 *
 * Example, time-series (multiple observations):
 *   json.UNITS [object/dict]
 *       wind_speed: knots
 *       air_temp: fahrenheit
 *       date_time: yyyy-mm-dd hh:mm
 *       solar_radiation: w/m**2
 *       wind_gust: knots
 *       volt: volts
 *       wind_direction: degrees
 *       relative_humidity: %
 *       pressure: mb
 *       precip_accum: inches
 *       precip_accum_five_minute: inches
 *   json.STATION [list (1)]
 *       0 [object/dict]
 *           STATUS: ACTIVE
 *           MNET_ID: 153
 *           ELEVATION: 4806
 *           NAME: WBB/U UTAH
 *           STID: WBB
 *           SENSOR_VARIABLES [list (3)]
 *           0: date_time
 *           1: air_temp
 *           2: pressure
 *           LONGITUDE: -111.84755
 *           STATE: UT
 *           OBSERVATIONS [object/dict]
 *               date_time [list (179)]
 *                   0: 2013-12-01 00:05
 *                   1: 2013-12-01 00:10
 *                   2: 2013-12-01 00:15
 *                   3: 2013-12-01 00:20
 *                   4: 2013-12-01 00:25
 *                   5: 2013-12-01 00:30
 *                   6: 2013-12-01 00:35
 *                   7: 2013-12-01 00:40
 *                   8: 2013-12-01 00:45
 *                   9: 2013-12-01 00:50
 *                   [...]
 *                   170: 2013-12-01 14:15
 *                   171: 2013-12-01 14:20
 *                   172: 2013-12-01 14:25
 *                   173: 2013-12-01 14:30
 *                   174: 2013-12-01 14:35
 *                   175: 2013-12-01 14:40
 *                   176: 2013-12-01 14:45
 *                   177: 2013-12-01 14:50
 *                   178: 2013-12-01 14:55
 *               pressure [list (179)]
 *                   0: 859.1
 *                   1: 859.17
 *                   2: 859.21
 *                   3: 859.2
 *                   4: 859.25
 *                   5: 859.39
 *                   6: 859.41
 *                   7: 859.42
 *                   8: 859.43
 *                   9: 859.45
 *                   [...]
 *                   170: 860.67
 *                   171: 860.65
 *                   172: 860.7
 *                   173: 860.7
 *                   174: 860.71
 *                   175: 860.65
 *                   176: 860.8
 *                   177: 860.84
 *                   178: 860.74
 *               air_temp [list (179)]
 *                   [...]
 *       LATITUDE: 40.76623
 *       TIMEZONE: US/Mountain
 *       ID: 1
 *       TIME_IS: UTC
 *   json.SUMMARY [object/dict]
 *       NUMBER_OF_OBJECTS: 1
 *       RESPONSE_CODE: 1
 *       RESPONSE_MESSAGE: OK
 *       RESPONSE_TIME: 22.9580402374 ms
 * </pre
 * @author Bruce Schubert
 */
class JsonParser {

    private static final String DATE_TIME = "date_time";
    private static final String AIR_TEMP = "air_temp";
    private static final String RELATIVE_HUMIDITY = "relative_humidity";
    private static final String WIND_SPEED = "wind_speed";
    private static final String WIND_DIRECTION = "wind_direction";

    private static final Logger logger = Logger.getLogger(JsonParser.class.getName());

    public static WeatherModel parseSingleObservation(String jsonResult) throws NumberFormatException, ParseException, VisADException, java.text.ParseException, RemoteException {
        JSONObject results;

        // Parse the JSON string. JSONParser is not thread safe.
        synchronized (JSONParser.class) {
            JSONParser parser = new JSONParser();
            results = (JSONObject) parser.parse(jsonResult);
        }

        JSONObject summary = (JSONObject) results.get("SUMMARY");
        Number numObjects = (Number) summary.get("NUMBER_OF_OBJECTS");
        Number respCode = (Number) summary.get("RESPONSE_CODE");
        String respMsg = (String) summary.get("RESPONSE_MESSAGE");
        String respTime = (String) summary.get("RESPONSE_TIME");

        if (respCode.intValue() != 1) {
            logger.log(Level.SEVERE, "parseSingleObservation failed: {0}", respMsg);
            return null;
        }

        // Parse the units so we can do proper unit conversions later
        JSONObject units = (JSONObject) results.get("UNITS");
        String airTempUom = (String) units.get(AIR_TEMP);
        String windSpdUom = (String) units.get(WIND_SPEED);
        if (!airTempUom.equalsIgnoreCase("fahrenheit")) {
            throw new IllegalStateException("Unexpected air temp UOM: " + airTempUom);
        }
        if (!windSpdUom.equalsIgnoreCase("knots")) {
            throw new IllegalStateException("Unexpected wind speed UOM: " + airTempUom);
        }
        Unit[] defaultUnits = FIRE_WEATHER.getDefaultUnits();

        // The spatial domain samples are created from the station coords
        float[][] latLonSamples = new float[2][numObjects.intValue()];
        // Temporal FlatFields are created for each station's weather observations
        ArrayList<FlatField> temporalFields = new ArrayList<>();

        // Create a MathType for the function: Time -> (Weather)
        FunctionType wxFuncOfTime = new FunctionType(RealTupleType.Time1DTuple, FIRE_WEATHER);

        // Process each station
        JSONArray stations = (JSONArray) results.get("STATION");
        int i = 0;
        for (Iterator it = stations.iterator(); it.hasNext();) {
            JSONObject station = (JSONObject) it.next();
            JSONObject obs = (JSONObject) station.get("OBSERVATIONS");
            String name = (String) station.get("NAME");
            String lat = (String) station.get("LATITUDE");
            String lon = (String) station.get("LONGITUDE");
            System.out.println(name + " : " + lat + ", " + lon);

            // Use the station coordinates for the spatial domain sample
            latLonSamples[0][i] = Float.valueOf(lat);
            latLonSamples[1][i] = Float.valueOf(lon);

            // The sensor values come to us in a two element array. The first element is an index 
            // into the date_time array, the second element is the value. Sometimes a stale element 
            // is stuck in the data. We detect this by examining the values in the date_time array.
            // Create an array of date_time "ageHrs" to validate usefullness of sensor values.
            // Alsoe, we'll use the most reset time for the time sample.
            LocalDateTime now = ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime();
            JSONArray times = (JSONArray) obs.get(DATE_TIME);
            long[] ageHrs = new long[times.size()];
            double timeSample = 0;
            for (int j = 0; j < times.size(); j++) {
                String time = (String) times.get(j);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime then = LocalDateTime.parse(time, formatter);
                ageHrs[j] = Duration.between(then, now).toHours();
                timeSample = max(timeSample, then.toEpochSecond(ZoneOffset.UTC));
            }

            // Create the wx range samples, and init with "missing" values
            double[][] wxSamples = new double[FIRE_WEATHER.getDimension()][1];
            for (int dim = 0; dim < FIRE_WEATHER.getDimension(); dim++) {
                Arrays.fill(wxSamples[dim], Double.NaN);
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
                // Skip stale observations that are truely erroneous, i.e., greater than 24 hours.
                if (ageHrs[dateIndex.intValue()] > 24) {
                    logger.log(Level.INFO, "Ignoring {0} sensor variable {1}. Appears to be stale: {2} ({3} hours old).",
                            new Object[]{name, sensor, times.get(dateIndex.intValue()), ageHrs[dateIndex.intValue()]});
                    continue;
                }
                if (value == null) {    // testing for "null" values
                    continue;
                }
                switch (sensor) {
                    case AIR_TEMP:
                        Real airTempF = new Real(WeatherType.AIR_TEMP_F, value.doubleValue());
                        wxSamples[AIR_TEMP_INDEX][0] = airTempF.getValue(defaultUnits[AIR_TEMP_INDEX]);
                        break;
                    case RELATIVE_HUMIDITY:
                        wxSamples[REL_HUMIDITY_INDEX][0] = value.doubleValue();
                        break;
                    case WIND_SPEED:
                        Real wndSpd = new Real(WeatherType.WIND_SPEED_KTS, value.doubleValue());
                        wxSamples[WIND_SPEED_INDEX][0] = wndSpd.getValue(defaultUnits[WIND_SPEED_INDEX]);
                        break;
                    case WIND_DIRECTION:
                        wxSamples[WIND_DIR_INDEX][0] = value.doubleValue();
                        break;
                    default:
                        logger.log(Level.INFO, "{0} sensor [{1}], value [{2}], not processed.",
                                new Object[]{name, sensor, value.toString()});
                }
            }
            // Create the temporal domain Set -- a 1-D sequence with 1 sample.
            Set timeSet = DateTime.makeTimeSet(new double[]{timeSample});
            FlatField temporalField = new FlatField(wxFuncOfTime, timeSet);
            temporalField.setSamples(wxSamples);

            // Add the temporal field to the collection that will be added to the spatial field
            temporalFields.add(temporalField);
            ++i;
        }

        // Create the spatial field with the station lat/lons...
        FieldImpl spatialField = new FieldImpl(
                new FunctionType(LatitudeLongitudeTuple, wxFuncOfTime),
                new Irregular2DSet(LatitudeLongitudeTuple, latLonSamples)); // max number is 3000

        // ... and assign the temporal weather fields to the spatial field's range
        FlatField[] array = new FlatField[temporalFields.size()];
        spatialField.setSamples(temporalFields.toArray(array), false);

        return new WeatherModel(spatialField);
    }

    public static WeatherModel parseTimeSeries(String jsonResult) throws NumberFormatException, ParseException, VisADException, java.text.ParseException, RemoteException {
        JSONObject results;

        // Parse the JSON string. JSONParser is not thread safe.
        synchronized (JSONParser.class) {
            JSONParser parser = new JSONParser();
            results = (JSONObject) parser.parse(jsonResult);
        }

        JSONObject summary = (JSONObject) results.get("SUMMARY");
        Number numObjects = (Number) summary.get("NUMBER_OF_OBJECTS");

        // Create a MathType for the function: Time -> (Weather)
        FunctionType wxFuncOfTime = new FunctionType(RealTupleType.Time1DTuple, FIRE_WEATHER);

        // Parse the units so we can do proper unit conversions.
        JSONObject units = (JSONObject) results.get("UNITS");
        String airTempUom = (String) units.get(AIR_TEMP);
        String windSpdUom = (String) units.get(WIND_SPEED);
        if (!airTempUom.equalsIgnoreCase("fahrenheit")) {
            throw new IllegalStateException("incompatible air temp UOM: " + airTempUom);
        }
        if (!windSpdUom.equalsIgnoreCase("knots")) {
            throw new IllegalStateException("incompatible wind speed UOM: " + airTempUom);
        }
        Unit[] defaultUnits = FIRE_WEATHER.getDefaultUnits();

        // The spatial domain samples are created from the station coords
        float[][] latLonSamples = new float[2][numObjects.intValue()];
        // Temporal FlatFields are created for each station's weather observations
        ArrayList<FlatField> temporalFields = new ArrayList<>();

        // Parse the stations 
        JSONArray stations = (JSONArray) results.get("STATION");
        int i = 0;
        for (Iterator it = stations.iterator(); it.hasNext();) {
            JSONObject station = (JSONObject) it.next();
            JSONObject obs = (JSONObject) station.get("OBSERVATIONS");
            String name = (String) station.get("NAME");
            String lat = (String) station.get("LATITUDE");
            String lon = (String) station.get("LONGITUDE");
            //System.out.println(name + " : " + lat + ", " + lon);

            // Store the spatial domain sample
            latLonSamples[0][i] = Float.valueOf(lat);
            latLonSamples[1][i] = Float.valueOf(lon);

            // The sensor values come to us in a single element array. The element index is an index 
            // into the date_time array. Parse the times to create the time domain samples.
            JSONArray times = (JSONArray) obs.get(DATE_TIME);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            double[][] timeSamples = new double[1][times.size()];
            for (int t = 0; t < times.size(); t++) {
                String time = (String) times.get(t);
                LocalDateTime utc = LocalDateTime.parse(time, formatter);
                timeSamples[0][t] = utc.toEpochSecond(ZoneOffset.UTC);
            }

            // Create the wx range samples, and init with "missing" values
            double[][] wxSamples = new double[FIRE_WEATHER.getDimension()][times.size()];
            for (int dim = 0; dim < FIRE_WEATHER.getDimension(); dim++) {
                Arrays.fill(wxSamples[dim], Double.NaN);
            }
            // Store the wx range samples that exist in this station
            JSONArray sensors = (JSONArray) station.get("SENSOR_VARIABLES");
            for (Object obj : sensors) {
                String sensor = (String) (obj);
                if (sensor.equals(DATE_TIME)) {
                    continue;
                }
                // Get the observation from the current sensor
                JSONArray values = (JSONArray) obs.get(sensor);
                for (int j = 0; j < values.size(); j++) {
                    // Note: values use the same indexing as the datetimes,
                    // so we can use the index for the wxRange samples.
                    Number value = (Number) values.get(j);
                    if (value == null) {    // testing for "null" values
                        continue;
                    }
                    switch (sensor) {
                        case AIR_TEMP:
                            Real airTempF = new Real(WeatherType.AIR_TEMP_F, value.doubleValue());
                            wxSamples[AIR_TEMP_INDEX][j] = airTempF.getValue(defaultUnits[AIR_TEMP_INDEX]);
                            break;
                        case RELATIVE_HUMIDITY:
                            wxSamples[REL_HUMIDITY_INDEX][j] = value.doubleValue();
                            break;
                        case WIND_SPEED:
                            Real wndSpd = new Real(WeatherType.WIND_SPEED_KTS, value.doubleValue());
                            wxSamples[WIND_SPEED_INDEX][j] = wndSpd.getValue(defaultUnits[WIND_SPEED_INDEX]);
                            break;
                        case WIND_DIRECTION:
                            wxSamples[WIND_DIR_INDEX][j] = value.doubleValue();
                            break;
                        default:
                            logger.log(Level.INFO, "{0} sensor [{1}], value [{2}], not processed.",
                                    new Object[]{name, sensor, value.toString()});
                    }
                }
            }
            // Create the temporal domain Set -- a 1-D sequence with no regular interval.
            Set timeSet = new Gridded1DDoubleSet(RealType.Time, timeSamples, timeSamples[0].length);
            // Create the temporal FlatField with the date-time values
            FlatField temporalField = new FlatField(wxFuncOfTime, timeSet);
            // ...and put the weather values into the range
            temporalField.setSamples(wxSamples);

            temporalFields.add(temporalField);
            ++i;
        }

        // Create the spatial field with the station lat/lons...
        FieldImpl spatialField = new FieldImpl(
                new FunctionType(LatitudeLongitudeTuple, wxFuncOfTime),
                new Irregular2DSet(LatitudeLongitudeTuple, latLonSamples)); // max number is 3000

        // ... and assign the temporal weather fields to the spatial field's range
        FlatField[] array = new FlatField[temporalFields.size()];
        spatialField.setSamples(temporalFields.toArray(array), false);

        return new WeatherModel(spatialField);
    }

}
