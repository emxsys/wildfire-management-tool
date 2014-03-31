/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package com.emxsys.wmt.globe.render;

/**
 * This class serves as a Model for the decorations of a wind barb. It maintains one wind speed in
 * knots and calculates how that should be represented in terms of number and type of graphic
 * elements. The rules used by this class are as follows:
 * <ul>
 * <li>A &quot;pennant&quot; represents 50 knots</li>
 * <li>A &quot;full flag&quot; represents 10 knots</li>
 * <li>A &quot;half flag&quot; represents 5 knots</li>
 * </ul>
 *
 * <p>
 * Special cases to the symbology are outlined here. This class does not take care of the actual
 * drawing of the symbols, but it does provide convenience methods to alert the View to special
 * circumstances:
 * <ol>
 * <li>&quot;calm&quot;: the wind speed is equal to zero. No staff is drawn, and two concentric
 * circles indicate the condition.</li>
 * <li>A staff with no decorations is present for wind speeds of one or two knots. </li>
 * <li>A single half-flag is a special case because it is not drawn exactly at the head of the barb.
 * It is drawn slightly away from the end in order to avoid visual confusion.</li>
 * </ol>
 *
 * <p>
 * Please note that this class does not mess around with negative wind speeds. If you pass a
 * negative wind speed in, it will make it positive, then attempt to symbolize it.
 *
 * @author Bryce Nordgren / USDA Forest Service
 * @since 2.2
 * @version $Id$
 */
public class WindBarbModel implements Cloneable {

    private int speed;
    private int pennants;
    private int fullFlags;
    private boolean halfFlag;
    private boolean loneHalfFlag;
    private boolean calm;

    /**
     * Initializes this WindBarbModel to the given speed.
     * @param speed wind speed in knots.
     */
    public WindBarbModel(int speed) {
        setSpeed(speed);
    }

    /**
     * Initializes this WindBarbModel to the given speed. This speed is rounded to the nearest
     * integer before being stored.
     * @param speed wind speed in knots.
     */
    public WindBarbModel(double speed) {
        setSpeed(speed);
    }

    /**
     * Creates a WindBarbModel with a zero windspeed.
     */
    public WindBarbModel() {
        setSpeed(0);
    }

    /**
     * Indicates the wind speed represented by this model (in knots).
     * @return wind speed in knots.
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * Allows the user to reset the wind speed. The decorations (e.g., number of pennants, flags and
     * half flags are recomputed automatically.
     * @param speed wind speed in knots.
     */
    public void setSpeed(int speed) {
        if (speed < 0) {
            speed = -speed;
        }
        this.speed = speed;
        calcDecorations();
    }

    /**
     * Allows the user to reset the wind speed. The decorations (e.g., number of pennants, flags and
     * half flags are recomputed automatically. The wind speed is rounded to the nearest integer.
     * @param speed wind speed in knots.
     */
    public void setSpeed(double speed) {
        setSpeed((int) (Math.rint(speed)));
    }

    /**
     * Indicates the number of pennants (representing 50 kt) required to represent the wind speed.
     * @return number of pennants to draw.
     */
    public int getPennants() {
        return pennants;
    }

    /**
     * Indicates the number of full flags (representing 10 kt) required to represent the wind speed.
     * @return number of full flags to draw.
     */
    public int getFullFlags() {
        return fullFlags;
    }

    /**
     * Indicates whether a half flag is required. (representing 5 kt.)
     * @return true if a half flag is required.
     */
    public boolean isHalfFlag() {
        return halfFlag;
    }

    /**
     * Indicates the special case where there is no decoration other than a single half flag on the
     * staff.
     * @return true if symbology consists of a single half flag.
     */
    public boolean isLoneHalfFlag() {
        return loneHalfFlag;
    }

    /**
     * Indicates the special case where there is no wind.
     * @return true if no wind.
     */
    public boolean isCalm() {
        return calm;
    }

    /**
     * Indicates that the staff is drawn. This is any case other than calm.
     * @return true if there is some wind.
     */
    public boolean isStaffPresent() {
        return !calm;
    }

    /**
     * actually calculate the symbology required by the barb representing the associated wind speed.
     */
    private void calcDecorations() {

        calm = (speed == 0);

        // +2 because the marks are _centered_ on 5 kt intervals.
        int localSpeed = speed + 2;
        pennants = localSpeed / 50;

        localSpeed = localSpeed % 50;
        fullFlags = localSpeed / 10;

        localSpeed = localSpeed % 10;
        halfFlag = (localSpeed >= 5);

        loneHalfFlag = (pennants == 0) && (fullFlags == 0) && halfFlag;
    }

    /**
     * Two WindBarbModels are equal if all of their symbology is the same. If the user wants to
     * compare wind speeds, it's easier to just to the comparison. This has the effect of lumping
     * all wind speeds into 5 knot &quot;bins&quot;.
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof WindBarbModel)) {
            return false;
        }
        WindBarbModel otherModel = (WindBarbModel) other;
        return (calm == otherModel.calm)
                && (pennants == otherModel.pennants)
                && (fullFlags == otherModel.fullFlags)
                && (halfFlag == otherModel.halfFlag)
                && (loneHalfFlag == otherModel.loneHalfFlag);
    }

    @Override
    public int hashCode() {
        int tmp = 0;
        if (halfFlag) {
            tmp = 1;
        }
        return (pennants << 16)
                | ((fullFlags << 1) & 0x0E)
                | (tmp & 0x01);
    }

    /**
     * This performs the default shallow copy of a wind barb object.
     */
    @Override
    public Object clone() {
        Object newGuy = null;
        try {
            newGuy = super.clone();
        } catch (CloneNotSupportedException cnse) {
            ;// do nothing.  We implement cloneable.
        }
        return newGuy;
    }

}
