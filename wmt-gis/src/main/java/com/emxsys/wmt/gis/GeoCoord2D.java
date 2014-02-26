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
import java.rmi.RemoteException;
import java.util.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import visad.*;


/**
 * RealTuple implementation of Coord2D for defining geographic coordinate points. I.e., latitude and
 * longitude (in degrees).
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class GeoCoord2D extends RealTuple implements Coord2D
{
    public static final int LATITUDE_INDEX = 0;
    public static final int LONGITUDE_INDEX = 1;
    public static final GeoCoord2D INVALID_POINT = new GeoCoord2D();
    public static CoordinateSystem DEFAULT_COORD_SYS;
    public static RealTupleType DEFAULT_TUPLE_TYPE;
    public static final String XML_ATTR_ANGLE = "Angle";
    private Real lat;
    private Real lon;
    private Data[] components;
    private static final Logger LOGGER = Logger.getLogger(GeoCoord2D.class.getName());


    static
    {
        try
        {
//            DEFAULT_COORD_SYS = new TrivialNavigation(RealTupleType.LatitudeLongitudeTuple);
            DEFAULT_COORD_SYS = new SimpleCoordinateSystem();
            DEFAULT_TUPLE_TYPE = new RealTupleType(
                RealType.Latitude, RealType.Longitude, DEFAULT_COORD_SYS, null);
        }
        catch (VisADException ex)
        {
            LOGGER.severe(ex.toString());
            DEFAULT_COORD_SYS = null;
        }

    }

    /* Default units (degree, degree) */
    public static final Unit[] DEFAULT_UNITS = new Unit[]
    {
        CommonUnit.degree, CommonUnit.degree
    };


    /**
     * Factory method to create a GeoCoord2D from a lat lon point.
     *
     * @param lat latitude in degrees.
     * @param lon longitude in degrees
     * @return a new GeoCoord2D
     */
    public static GeoCoord2D fromDegrees(double lat, double lon)
    {
        try
        {
            return new GeoCoord2D(lat, lon);
        }
        catch (VisADException | RemoteException ex)
        {
            LOGGER.severe(ex.toString());
        }
        return INVALID_POINT;
    }


    /**
     * Factory method to create a GeoCoord2D from a lat lon point.
     *
     * @param lat latitude in radians.
     * @param lon longitude in radians
     * @return a new GeoCoord2D
     */
    public static GeoCoord2D fromRadians(double lat, double lon)
    {
        return fromDegrees(Math.toDegrees(lat), Math.toDegrees(lon));
    }

    /**
     * Factory method to create a GeoCoord2D from a lat lon point.
     *
     * @param lat latitude in RealType.Latitude.
     * @param lon longitude in RealType.Longitude
     * @return a new GeoCoord2D
     */
    public static GeoCoord2D fromReals(Real lat, Real lon)
    {
        try
        {
            return new GeoCoord2D(lat, lon);
        }
        catch (VisADException | RemoteException ex)
        {
            LOGGER.severe(ex.toString());
        }
        return INVALID_POINT;
    }

    /**
     * Factory method to create a GeoCoord2D from an XML element.
     *
     * @param element An Element formatted by the toXmlElement method.
     * @return A new point object, or an INVALID_POSITION if incorrectly formatted.
     */
    public static GeoCoord2D fromXmlElement(Element element)
    {
        try
        {
            if (element == null)
            {
                throw new IllegalArgumentException("Element parameter is null.");
            }
            String latValue = element.getAttribute(RealType.Latitude.getName());
            String lonValue = element.getAttribute(RealType.Longitude.getName());
            String angularUnits = element.getAttribute(XML_ATTR_ANGLE);

            if (latValue == null || lonValue == null)
            {
                throw new IllegalArgumentException("Attribute(s) missing in the XML element.");
            }

            double latitude = Double.parseDouble(latValue);
            double longitude = Double.parseDouble(lonValue);

            // Assume degrees if missing
            if (angularUnits != null && !angularUnits.isEmpty() && !angularUnits.equals(CommonUnit.degree.toString()))
            {
                // TODO: could convert radians to degrees
                throw new IllegalArgumentException("Angular units must be 'degrees': Found " + angularUnits);
            }

            return new GeoCoord2D(latitude, longitude);
        }
        catch (IllegalArgumentException | VisADException | RemoteException ex)
        {
            // Catching NumberFormatExceptions and IllegalArgumentExceptions,
            // plus VisADExceptions and RemoteExceptions
            LOGGER.severe(ex.toString());
        }
        return INVALID_POINT;
    }


    /**
     * Construct a GeoCoord2D with missing values
     *
     */
    public GeoCoord2D()
    {
        super(RealTupleType.LatitudeLongitudeTuple);
        this.lat = new Real(RealType.Latitude);
        this.lon = new Real(RealType.Longitude);
    }


    /**
     * Construct a GeoCoord2D from double values of latitude and longitude.
     *
     * @param lat latitude (degrees North positive)
     * @param lon longitude (degrees East positive)
     */
    public GeoCoord2D(double lat, double lon) throws VisADException, RemoteException
    {
        this(Latitude.fromDegrees(lat), Longitude.fromDegrees(lon));
    }


    /**
     * Construct a LatLonTuple from Reals representing the latitude and longitude.
     *
     * @param lat Real representing latitude
     * @param lon Real representing longitude
     */
    public GeoCoord2D(Real lat, Real lon) throws VisADException, RemoteException
    {
        this(new Real[]
        {
            Latitude.fromReal(lat), Longitude.fromReal(lon)
        });
    }


    /**
     * Construct a LatLonTuple from Reals representing the latitude and longitude.
     *
     */
    public GeoCoord2D(Real[] latLonArray) throws VisADException, RemoteException
    {
        super(DEFAULT_TUPLE_TYPE, latLonArray, DEFAULT_COORD_SYS);
        this.lat = latLonArray[0];
        this.lon = latLonArray[1];
    }


    /**
     * Get the latitude of this point.
     *
     * @return Real representing the latitude.
     */
    @Override
    public Real getLatitude()
    {
        return lat;
    }


    /**
     * Get the longitude of this point.
     *
     * @return Real representing the longitude
     */
    @Override
    public Real getLongitude()
    {
        return lon;
    }


    /**
     * Get the latitude of this point.
     *
     * @return double representing the latitude in degrees.
     */
    @Override
    public double getLatitudeDegrees()
    {
        return lat.getValue();
    }


    /**
     * Get the longitude of this point.
     *
     * @return double representing the longitude in degrees.
     */
    @Override
    public double getLongitudeDegrees()
    {
        return lon.getValue();
    }


    /**
     * Is missing any data elements?
     *
     * @return is missing
     */
    @Override
    public boolean isMissing()
    {
        return lat.isMissing() || lon.isMissing();
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
    public Data getComponent(int i) throws VisADException, RemoteException
    {
        switch (i)
        {
            case LATITUDE_INDEX:
                return lat;
            case LONGITUDE_INDEX:
                return lon;
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
    public Data[] getComponents(boolean copy)
    {
        //Create the array and populate it if needed
        if (components == null)
        {
            Data[] tmp = new Data[getDimension()];
            tmp[LATITUDE_INDEX] = lat;
            tmp[LONGITUDE_INDEX] = lon;
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
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!(obj instanceof GeoCoord2D))
        {
            return false;
        }
        GeoCoord2D that = (GeoCoord2D) obj;
        return this.lat.equals(that.lat)
            && this.lon.equals(that.lon);
    }


    /**
     * Returns the hash code of this object.
     *
     * @return The hash code of this object.
     */
    @Override
    public int hashCode()
    {
        return lat.hashCode() | lon.hashCode();
    }


    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("Lat: ");
        try
        {
            buf.append(
                visad.browser.Convert.shortString(lat.getValue(CommonUnit.degree)));
        }
        catch (VisADException ve)
        {
            buf.append(
                visad.browser.Convert.shortString(lat.getValue()));
        }
        buf.append(" Lon: ");
        try
        {
            buf.append(
                visad.browser.Convert.shortString(lon.getValue(CommonUnit.degree)));
        }
        catch (VisADException ve)
        {
            buf.append(
                visad.browser.Convert.shortString(lon.getValue()));
        }
        return buf.toString();
    }


    public Element toXmlElement(Document doc, String tagName)
    {
        Element element = doc.createElement(tagName);
        element.setAttribute(RealType.Latitude.getName(), Double.toString(getLatitudeDegrees()));
        element.setAttribute(RealType.Longitude.getName(), Double.toString(getLongitudeDegrees()));

        //element.setAttribute(XML_ATTR_ANGLE, CommonUnit.degree.getIdentifier());
        return element;
    }
}
