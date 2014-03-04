/*
 * Copyright (c) 2010-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.gis;

import com.emxsys.wmt.gis.api.Coord2D;
import com.emxsys.wmt.gis.api.Coord3D;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import visad.*;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
/**
 * RealTuple implementation of Coord2D for defining geographic coordinate points. I.e., latitude and
 * longitude (in degrees).
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
@SuppressWarnings("StaticNonFinalUsedInInitialization")
public class GeoCoord3D extends RealTuple implements Coord3D {

    public static CoordinateSystem DEFAULT_COORD_SYS;
    public static RealTupleType DEFAULT_TUPLE_TYPE;
    public static GeoCoord3D INVALID_POSITION;
    public static GeoCoord3D ZERO_POSITION;
    public static final String XML_ATTR_ANGLE = "Angle";
    public static final String XML_ATTR_LENGTH = "Length";
    public static final int LAT_TUPLE_INDEX = 0;
    public static final int LON_TUPLE_INDEX = 1;
    public static final int ALT_TUPLE_INDEX = 2;
    private Real lat;
    private Real lon;
    private Real alt;
    private RealTuple coord;
    private Data[] components;
    private static final double ZERO = 0.0;
    private static final Logger logger = Logger.getLogger(GeoCoord3D.class.getName());

    static {
        try {
//           DEFAULT_COORD_SYS = new TrivialNavigation(RealTupleType.LatitudeLongitudeAltitude);
            DEFAULT_COORD_SYS = new Simple3DCoordinateSystem();
            DEFAULT_TUPLE_TYPE = new RealTupleType(
                    RealType.Latitude, RealType.Longitude, RealType.Altitude,
                    DEFAULT_COORD_SYS, null);
            INVALID_POSITION = new GeoCoord3D();
            ZERO_POSITION = fromDegreesAndMeters(ZERO, ZERO, ZERO);
        }
        catch (VisADException ex) {
            logger.severe(ex.toString());
            DEFAULT_COORD_SYS = null;
        }

    }
    /* Default units (degree, degree, meter) */
    public static final Unit[] DEFAULT_UNITS
            = new Unit[]{
                CommonUnit.degree, CommonUnit.degree, CommonUnit.meter
            };

    /**
     * Factory method to create a GeoCoord3D from a lat, lon with a zero altitude.
     *
     * @param lat latitude in degrees.
     * @param lon longitude in degrees.
     * @return a new GeoCoord3D
     */
    public static GeoCoord3D fromDegrees(double lat, double lon) {
        return fromDegreesAndMeters(lat, lon, ZERO);
    }

    /**
     * Factory method to create a GeoCoord3D from a lat, lon with a zero altitude.
     *
     * @param lat latitude in radians.
     * @param lon longitude in radians.
     * @return a new GeoCoord3D
     */
    public static GeoCoord3D fromRadians(double lat, double lon) {
        return fromRadiansAndMeters(lat, lon, ZERO);
    }

    /**
     * Factory method to create a GeoCoord3D from a lat,lon and altitude point.
     *
     * @param lat latitude in degrees.
     * @param lon longitude in degrees.
     * @param meters altitude in degrees.
     * @return a new GeoCoord3D
     */
    public static GeoCoord3D fromDegreesAndMeters(double lat, double lon, double meters) {
        try {
            return new GeoCoord3D(lat, lon, meters);
        }
        catch (VisADException | RemoteException ex) {
            Exceptions.printStackTrace(ex);
        }
        return INVALID_POSITION;
    }

    /**
     * Factory method to create a GeoCoord3D from a lat,lon and altitude point.
     *
     * @param lat latitude in radians.
     * @param lon longitude in radians.
     * @param meters altitude in meters.
     * @return a new GeoCoord3D
     */
    public static GeoCoord3D fromRadiansAndMeters(double lat, double lon, double meters) {
        return fromDegreesAndMeters(Math.toDegrees(lat), Math.toDegrees(lon), meters);
    }

    /**
     * Factory method to create a GeoCoord3D from an XML element.
     *
     * @param element an Element formatted by the toXmlElement method.
     * @return a new position object, or an INVALID_POSITION if incorrectly formatted.
     */
    public static GeoCoord3D fromXmlElement(Element element) {
        try {
            if (element == null) {
                throw new IllegalArgumentException("Element parameter is null.");
            }
            String latValue = element.getAttribute(RealType.Latitude.getName());
            String lonValue = element.getAttribute(RealType.Longitude.getName());
            String altValue = element.getAttribute(RealType.Altitude.getName());
            String angularUnits = element.getAttribute(XML_ATTR_ANGLE);
            String linearUnits = element.getAttribute(XML_ATTR_LENGTH);

            if (latValue == null || lonValue == null || altValue == null) {
                throw new IllegalArgumentException("Attribute(s) missing in the XML element.");
            }
            // These throw NumberFormatExceptions if emtpy strings
            double latitude = Double.parseDouble(latValue);
            double longitude = Double.parseDouble(lonValue);
            double altitude = Double.parseDouble(altValue);

            // Assume degrees if missing
            if (angularUnits != null && !angularUnits.isEmpty() && !angularUnits.equals(CommonUnit.degree.toString())) {
                // TODO: could convert radians to degrees
                throw new IllegalArgumentException("Angular units must be 'degrees': Found " + angularUnits);
            }

            // Assume meters if missing
            if (linearUnits != null && !linearUnits.isEmpty() && !linearUnits.equals(CommonUnit.meter.toString())) {
                // TODO: could convert feet to meters
                throw new IllegalArgumentException("Linear units must be 'meters': Found " + linearUnits);
            }

            return new GeoCoord3D(latitude, longitude, altitude);
        }
        catch (IllegalArgumentException | VisADException | RemoteException ex) {
            logger.log(Level.WARNING, "fromXmlElement() failed: {0}", ex.toString());
        }
        return INVALID_POSITION;
    }

    public static Element toXmlElement(Document doc, String tagName, Coord3D position) {
        GeoCoord3D tuple;
        if (position instanceof GeoCoord3D) {
            tuple = (GeoCoord3D) position;
        }
        else {
            try {
                tuple = new GeoCoord3D(position);
            }
            catch (VisADException | RemoteException ex) {
                Exceptions.printStackTrace(ex);
                tuple = INVALID_POSITION;
            }
        }
        return tuple.toXmlElement(doc, tagName);
    }

    /**
     * Construct a GeoCoord3D with missing values
     *
     */
    public GeoCoord3D() {
        super(DEFAULT_TUPLE_TYPE);
        this.lat = new Real(RealType.Latitude);
        this.lon = new Real(RealType.Longitude);
        this.alt = new Real(RealType.Altitude);
    }

    /**
     * Construct a GeoCoord3D from a Coord2D
     *
     */
    public GeoCoord3D(Coord2D coord) throws VisADException, RemoteException {
        this(coord.getLatitude(), coord.getLongitude(), Altitude.fromMeters(ZERO));
    }

    /**
     * Construct a GeoCoord3D from another Coord3D
     *
     */
    public GeoCoord3D(Coord3D coord) throws VisADException, RemoteException {
        this(coord.getLatitude(), coord.getLongitude(), coord.getAltitude());
    }

    /**
     * Construct a GeoCoord3D from double values of latitude and longitude; a zero altitude is
     * assumed.
     *
     * @param lat latitude (degrees North positive)
     * @param lon longitude (degrees East positive)
     */
    public GeoCoord3D(double lat, double lon) throws VisADException, RemoteException {
        this(Latitude.fromDegrees(lat), Longitude.fromDegrees(lon), Altitude.fromMeters(ZERO));
    }

    /**
     * Construct a GeoCoord3D from double values of latitude and longitude and altitude.
     *
     * @param lat latitude (degrees North positive)
     * @param lon longitude (degrees East positive)
     * @param alt altitude in meters
     */
    public GeoCoord3D(double lat, double lon, double alt) throws VisADException,
            RemoteException {
        this(Latitude.fromDegrees(lat), Longitude.fromDegrees(lon), Altitude.fromMeters(alt));
    }

    /**
     * Construct a GeoCoord3D from Reals representing the latitude and longitude and altitude.
     *
     * @param lat a Real representing latitude
     * @param lon a Real representing longitude
     * @param alt a Real representing altitude or elevation
     */
    public GeoCoord3D(Real lat, Real lon, Real alt) throws VisADException, RemoteException {
        this(new Real[]{
            Latitude.fromReal(lat), Longitude.fromReal(lon), Altitude.fromReal(alt)
        });
    }

    /**
     * Construct a GeoCoord3D from Reals representing the latitude and longitude and a zero
     * altitude.
     *
     * @param lat a Real representing latitude
     * @param lon a Real representing longitude
     */
    public GeoCoord3D(Real lat, Real lon) throws VisADException, RemoteException {
        this(new Real[]{
            Latitude.fromReal(lat), Longitude.fromReal(lon), Altitude.ZERO
        });
    }

    /**
     * Construct a LatLonTuple from Reals representing the latitude and longitude.
     *
     */
    public GeoCoord3D(Real[] realArray) throws VisADException, RemoteException {
        super(DEFAULT_TUPLE_TYPE, realArray, DEFAULT_COORD_SYS);
        this.lat = realArray[LAT_TUPLE_INDEX];
        this.lon = realArray[LON_TUPLE_INDEX];
        this.alt = realArray[ALT_TUPLE_INDEX];
    }

    /**
     * Get the latitude of this point
     *
     * @return Real representing the latitude
     */
    @Override
    public Real getLatitude() {
        return lat;
    }

    /**
     * Get the latitude of this point.
     *
     * @return double representing the latitude in degrees.
     */
    @Override
    public double getLatitudeDegrees() {
        return lat.getValue();
    }

    /**
     * Get the longitude of this point.
     *
     * @return Real representing the longitude
     */
    @Override
    public Real getLongitude() {
        return lon;
    }

    /**
     * Get the longitude of this point.
     *
     * @return double representing the longitude in degrees.
     */
    @Override
    public double getLongitudeDegrees() {
        return lon.getValue();
    }

    /**
     * Get the altitude (elevation) of this coordinate.
     *
     * @return Real representing the altitude.
     */
    @Override
    public Real getAltitude() {
        return alt;
    }

    /**
     * Get the altitude (elevation) of this coordinate.
     *
     * @return double representing the altitude in meters.
     */
    @Override
    public double getAltitudeMeters() {
        return alt.getValue();
    }

    /**
     * Is missing any data elements?
     *
     * @return is missing
     */
    @Override
    public boolean isMissing() {
        return lat.isMissing() || lon.isMissing() || alt.isMissing();
    }

    /**
     * Get the i'th component.
     *
     * @param i Which one
     * @return The component
     *
     * @throws RemoteException On badness
     * @throws VisADException On badness
     */
    @Override
    public Data getComponent(int i) throws VisADException, RemoteException {
        switch (i) {
            case LAT_TUPLE_INDEX:
                return lat;
            case LON_TUPLE_INDEX:
                return lon;
            case ALT_TUPLE_INDEX:
                return alt;
            default:
                throw new IllegalArgumentException("Wrong component number:" + i);
        }
    }

    /**
     * Create, if needed, and return the component array.
     *
     * @return components
     */
    @Override
    public Data[] getComponents(boolean copy) {
        //Create the array and populate it if needed
        if (components == null) {
            Data[] tmp = new Data[getDimension()];
            tmp[LAT_TUPLE_INDEX] = lat;
            tmp[LON_TUPLE_INDEX] = lon;
            tmp[ALT_TUPLE_INDEX] = alt;
            components = tmp;
        }
        return components;
    }

    /**
     * Indicates if this Tuple is identical to an object.
     *
     * @param obj The object.
     * @return            <code>true</code> if and only if the object is a Tuple and both Tuple-s have
     * identical component sequences.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GeoCoord3D)) {
            return false;
        }
        GeoCoord3D that = (GeoCoord3D) obj;
        return this.lat.equals(that.lat)
                && this.lon.equals(that.lon)
                && this.alt.equals(that.alt);
    }

    /**
     * Returns the hash code of this object.
     *
     * @return The hash code of this object.
     */
    @Override
    public int hashCode() {
        return lat.hashCode() | lon.hashCode() | alt.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("Lat: ");
        try {
            buf.append(visad.browser.Convert.shortString(lat.getValue(CommonUnit.degree)));
        }
        catch (VisADException ve) {
            buf.append(visad.browser.Convert.shortString(lat.getValue()));
        }
        buf.append(" Lon: ");
        try {
            buf.append(visad.browser.Convert.shortString(lon.getValue(CommonUnit.degree)));
        }
        catch (VisADException ve) {
            buf.append(visad.browser.Convert.shortString(lon.getValue()));
        }
        buf.append(" Alt: ");
        try {
            buf.append(visad.browser.Convert.shortString(alt.getValue(CommonUnit.meter)));
        }
        catch (VisADException ve) {
            buf.append(visad.browser.Convert.shortString(alt.getValue()));
        }
        return buf.toString();
    }

    public Element toXmlElement(Document doc, String tagName) {
        Element element = doc.createElement(tagName);
        element.setAttribute(RealType.Latitude.getName(), Double.toString(getLatitudeDegrees()));
        element.setAttribute(RealType.Longitude.getName(), Double.toString(getLongitudeDegrees()));
        element.setAttribute(RealType.Altitude.getName(), Double.toString(getAltitudeMeters()));

//        element.setAttribute(XML_ATTR_ANGLE, CommonUnit.degree.getIdentifier());
//        element.setAttribute(XML_ATTR_LENGTH, CommonUnit.meter.getIdentifier());
        return element;
    }

    @Override
    public Coord2D getCoordinate2D() {
        if (coord == null) {
            try {
                coord = new GeoCoord2D(lat, lon);
            }
            catch (VisADException | RemoteException e) {
                // shouldn't every happen
                coord = this;
                throw new RuntimeException(e);
            }
        }
        return (Coord2D) coord;
    }
}
