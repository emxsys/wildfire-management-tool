/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.visad;

import java.util.logging.Logger;
import visad.Unit;
import visad.data.units.Parser;


/**
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class Units
{
    private Units()
    {
    }


    /**
     * Get a unit instance based upon a specification string.
     *
     * Simply uses visad.data.units.Parser.parse(), but catches the exception so that it can be used
     * in initializers.
     *
     * @param unitSpec specification to be parsed
     * @return a Unit instance based on the specification; throws if the spec cannot be parsed.
     * <title> Unit names for VisAD</title>
     * <center>
     * <h2>
     * Unit names and abbreviations for use in VisAD</h2>
     * <p> 12/21/2001
     * </center>
     * <p>
     * This page details the unit specifications that VisAD can handle (through the
     * visad.data.units.DefaultUnitsDB class). It is recommended that when possible, you adhere to
     * <a href="http://physics.nist.gov/cuu/Units/">the recommendations of NIST</a>, modified as
     * follows:
     * <ol>
     * <li> Use case-sensitive symbols when possible
     *
     * <li> If you must use names, change any spaces to underscores (e.g., "deg F" should be
     * specified as "deg_F")
     * </ol>
     * <p>
     * The unit names contained herein are the ones that can be used or may appear in any of the
     * following:
     * <ul>
     * <li><tt>Unit u = visad.data.units.Parser.parse(String name);</tt>
     * <li>From Python, as in: <tt>u = makeUnit(name)<//t>
     * <li>The <tt>visad.Unit.toString()</tt> method.
     * <li>When creating unit names in NetCDF files
     * <li> (Note: these names are also recognized by the C/Fortran/Perl units-handling package
     * <i>UDUNITS</i>)
     *
     * </ul>
     * <p>
     * If you need to create your own unit as a combination of things, here are some examples of how
     * you would specify "degrees Celsius per kilometer per week":
     * <pre>
     * degC/(km.week)
     * degC.km-1.week-1
     * </pre> but <u>not</u>:
     * <br>
     * <pre>
     * degC/km.week
     * degC/km/week
     * </pre>
     * <p>
     * <i>
     * Note in each group, the word in quotes to the left of the = sign is the acceptable name for
     * the unit (except as noted).
     * </i>
     * </b>
     * <a name="top"></a>
     *
<p>
     * <h3>Fast find - click on these links:</h3>
     * <ul>
     * <li><a href="#prefix">Prefix names</a>
     * <li><a href="#presym">Prefix symbols</a>
     * <li><a href="#base">Base Units</a>
     * <li><a href="#const">Constants</a>
     * <li><a href="#electric">Electric Current</a>
     * <li><a href="#lumin">Luminous Intensity</a>
     * <li><a href="#temp">Thermodynamic Temperature</a>
     *
<li><a href="#mass">Mass</a>
     * <li><a href="#time">Time</a>
     * <li><a href="#angle">Plane angle</a>
     * <li><a href="#special">Special Derived Units</a>
     * <li><a href="#accel">Acceleration</a>
     * <li><a href="#area">Area</a>
     * <li><a href="#electmag">Electricty and Magnetism</a>
     * <li><a href="#energy">Energy</a>
     * <li><a href="#force">Force</a>
     *
<li><a href="#heat">Heat</a>
     * <li><a href="#light">Light</a>
     * <li><a href="#massperlen">Mass Per Unit Length</a>
     * <li><a href="#masspertime">Mass Per Unit Time</a>
     * <li><a href="#power">Power</a>
     * <li><a href="#pressure">Pressure or Stress</a>
     * <li><a href="#rad">Radiation Units</a>
     * <li><a href="#vel">Velocity (includes speed)</a>
     * <li><a href="#viscos">Viscosity</a>
     *
<li><a href="#volu">Volume (includes capacity)</a>
     * <li><a href="#voltime">Volume per unit time</a>
     * <li><a href="#computer">Computers and communication</a>
     * <li><a href="#misc">Misc.</a>
     * </ul>
     * <p>
     *
     *
     *
     *
<a name="prefix">
     * <h3> The unit prefix names in order of lexicographic length:</h3>
     *
</a>
     * <pre>
     * "centi" =   1e-2
     * "femto" =   1e-15
     * "hecto" =   1e2
     * "micro" =   1e-6
     * "milli" =   1e-3
     * "yocto" =   1e-24
     * "yotta" =   1e24
     * "zepto" =   1e-21
     * "zetta" =   1e21
     * "atto"  =   1e-18
     * "deca"  =   1e1    // Spelling according to "ISO 2955:
     * // Information processing --
     * // Representation of SI and other units
     * // in systems with limited character
     * // sets"
     * "deci"  =   1e-1
     * "deka"  =   1e1    // Spelling according to "ASTM
     * // Designation: E 380 - 85: Standard
     * // for METRIC PRACTICE" ="ANSI/IEEE Std
     * // 260-1978 (Reaffirmed 1985): IEEE
     * // Standard Letter Symbols for Units of
     * // Measurement" =and NIST Special
     * // Publication 811, 1995 Edition:
     * // "Guide for the Use of the
     * // International System of Units (SI)".
     * "giga"  =   1e9    // 1st syllable pronounced "jig"
     * // according to "ASTM Designation: E
     * // 380 - 85: Standard for METRIC
     * // PRACTICE".
     * "kilo"  =   1e3
     * "mega"  =   1e6
     * "nano"  =   1e-9
     * "peta"  =   1e15
     * "pico"  =   1e-12
     * "tera"  =   1e12
     * "exa"   =   1e18
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="presym">
     * <h3>The unit prefix symbols in order of lexicographic length:</h3>
     * </a>
     * <pre>
     * "da" =   1e1
     * "E"  =   1e18
     * "G"  =   1e9
     * "M"  =   1e6
     * "P"  =   1e15
     * "T"  =   1e12
     * "Y"  =   1e24
     * "Z"  =   1e21
     * "a"  =   1e-18
     * "c"  =   1e-2
     * "d"  =   1e-1
     * "f"  =   1e-15
     * "h"  =   1e2
     * "k"  =   1e3
     * "m"  =   1e-3
     * "n"  =   1e-9
     * "p"  =   1e-12
     * "u"  =   1e-6
     * "y"  =   1e-24
     * "z"  =   1e-21
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="base">
     *
     <h3>The base units:</h3>
     * </a>
     * <pre>
     * "A" =   SI.ampere (Electric Current)
     * "cd" =  SI.candela (Luminous Intensity)
     * "K" =   SI.kelvin (Temperature)
     * "kg" =  SI.kilogram (Mass)
     * "m"  =  SI.meter (Length)
     * "mol" = SI.mole  (Amount of substance)
     * "s" =   SI.second (Time)
     * "rad" = SI.radian (Angle)
     * "sr" =  SI.steradian (Solid Angle)
     *
     * </pre>
     *
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="const">
     * <h3>Constants:</h3>
     * </a>
     * <pre>
     * "%" =             new ScaledUnit(0.01)
     * "percent" =      "%"
     * "PI" =            new ScaledUnit(Math.PI)
     * "bakersdozen" =   new ScaledUnit(13)
     * "pair" =          new ScaledUnit(2)
     * "ten" =           new ScaledUnit(10)
     * "dozen" =         new ScaledUnit(12)
     * "score" =         new ScaledUnit(20)
     * "hundred" =       new ScaledUnit(100)
     * "thousand" =      new ScaledUnit(1.0e3)
     * "million" =       new ScaledUnit(1.0e6)
     *
     * // NB: "billion" is ambiguous (1e9 in U.S. but 1e12 in U.K.)
     *
     * </pre>
     *
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="electric">
     * <b> All subsequent definitions must be given in terms of earlier definitions. Forward
     * referencing is not permitted.</b>
     * </a>
     *
<p>
     * <h3>UNITS OF ELECTRIC CURRENT</h3>
     * <pre>
     *
     * "amp" =          "ampere"
     * "abampere" =     get("A").scale(10)        // exact
     * "gilbert" =      get("A").scale(7.957747e-1)
     * "statampere" =   get("A").scale(3.335640e-10)
     * "biot" =         "abampere"
     *
     * </pre>
     * <a href="#top">Back to top</a>
     *
<p>
     * <a name="lumin">
     * <h3>UNITS OF LUMINOUS INTENSITY</h3>
     * </a>
     * <pre>
     * "candle" =   "candela"
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="temp">
     * <h3>UNITS OF THERMODYNAMIC TEMPERATURE</h3>
     * </a>
     * <pre>
     * "degree kelvin" =  "K"
     * "degrees kelvin" = "K"
     * "degK" =           "K"
     * "degreeK" =        "K"
     * "degreesK" =       "K"
     * "deg K" =          "K"
     * "degree K" =       "K"
     * "degrees K" =      "K"
     *
     * "Cel" =               new OffsetUnit(273.15, (BaseUnit)get("K"))
     * "celsius" =           "Cel"
     * "degree celsius" =    "Cel"
     * "degrees celsius" =   "Cel"
     * "centigrade" =        "Cel"
     * "degree centigrade" =  "Cel"
     * "degrees centigrade" = "Cel"
     * "degC" =               "Cel"
     * "degreeC" =            "Cel"
     * "degreesC" =           "Cel"
     * "deg C" =              "Cel"
     * "degree C" =           "Cel"
     * "degrees C" =          "Cel"
     * (NOTE:  "C" means `coulomb')
     *
     * "rankine" =            get("K").scale(1/1.8)
     * "degree rankine" =     "rankine"
     * "degrees rankine" =    "rankine"
     * "degR" =               "rankine"
     * "degreeR" =            "rankine"
     * "degreesR" =           "rankine"
     * "deg R" =              "rankine"
     * "degree R" =           "rankine"
     * "degrees R" =          "rankine"
     * (NOTE: "R" means "roentgen")
     *
     * "fahrenheit" =          get("Rankine").shift(459.67)
     * "degree fahrenheit" =   "fahrenheit"
     * "degrees fahrenheit" =  "fahrenheit"
     * "degF" =                "fahrenheit"
     * "degreeF" =             "fahrenheit"
     * "degreesF" =            "fahrenheit"
     * "deg F" =               "fahrenheit"
     * "degree F" =            "fahrenheit"
     * "degrees F" =           "fahrenheit"
     * (NOTE: "F" means "farad")
     *
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="mass">
     * <h3>UNITS OF MASS</h3>
     * </a>
     * <pre>
     * "assay ton" =           get("kg").scale(2.916667e-2)
     * "avoirdupois ounce" =   get("kg").scale(2.834952e-2)
     * "avoirdupois pound" =   get("kg").scale(4.5359237e-1)    // exact
     * "carat" =               get("kg").scale(2e-4)
     * "gr" =                  get("kg").scale(6.479891e-5)    // exact
     * "g" =                   get("kg").scale(1e-3)        // exact
     * "long hundredweight" =  get("kg").scale(5.080235e1)
     * "tne" =                 get("kg").scale(1e3)        // exact
     * "pennyweight" =         get("kg").scale(1.555174e-3)
     * "short hundredweight" = get("kg").scale(4.535924e1)
     * "slug" =                get("kg").scale(14.59390)
     * "troy ounce" =          get("kg").scale(3.110348e-2)
     * "troy pound" =          get("kg").scale(3.732417e-1)
     * "amu" =                 get("kg").scale(1.66054e-27)
     * "scruple" =             get("gr").scale(20)
     * "apdram" =              get("gr").scale(60)
     * "apounce" =             get("gr").scale(480)
     * "appound" =             get("gr").scale(5760)
     *
     * "gram" =                "g"                // was "gravity"
     * "tonne" =               "tne"
     * "metric ton" =          "tne"
     * "apothecary ounce" =    "troy ounce"
     * "apothecary pound" =    "troy pound"
     * "pound" =               "avoirdupois pound"
     * "metricton" =           "tne"
     * "grain" =               "gr"
     * "atomicmassunit" =      "amu"
     * "atomic mass unit" =    "amu"
     *
     * "t" =                   "tne"
     * "lb" =                  "avoirdupois pound"
     * "bag" =                 get("pound").scale(94)
     * "short ton" =           get("pound").scale(2000)
     * "long ton" =            get("pound").scale(2240)
     *
     * "ton" =                 "short ton"
     * "shortton" =            "short ton"
     * "longton" =             "long ton"
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="length">
     * <h3>UNITS OF LENGTH</h3>
     *
</a>
     * <pre>
     * "angstrom" =       get("m").scale(1e-10)
     * "au" =             get("m").scale(1.495979e11)
     * "fermi" =          get("m").scale(1e-15)        // exact
     * "light year" =     get("m").scale(9.46073e15)
     * "micron" =         get("m").scale(1e-6)        // exact
     * "mil" =            get("m").scale(2.54e-5)    // exact
     * "nautical mile" =  get("m").scale(1.852000e3)    // exact
     * "parsec" =         get("m").scale(3.085678e16)
     * "printers point" = get("m").scale(3.514598e-4)
     *
     * "metre" =          "m"
     * "prs" =            "parsec"
     *
     * (NOTE: There's an international foot and a US survey foot and
     * they're not the same!)
     *
     * "US survey foot" =   get("m").scale(1200/3937.)        // exact
     * "US survey yard" =   get("US survey foot").scale(3)    // exact
     * "US survey mile" =   get("US survey foot").scale(5280)    // exact
     * "rod" =              get("US survey foot").scale(16.5)    // exact
     * "furlong" =          get("US survey foot").scale(660)    // exact
     * "fathom" =           get("US survey foot").scale(6)    // exact
     *
     * "US survey feet" =   "US survey foot"
     * "US statute mile" =  "US survey mile"
     * "pole" =             "rod"
     * "perch" =            "rod"
     * "perches" =          "perch"
     *
     * "international inch" =    get("m").scale(.0254)        // exact
     * "international foot" =    get("international inch").scale(12) // exact
     * "international yard" =    get("international foot").scale(3) // exact
     * "international mile" =    get("international foot").scale(5280) // exact
     * "international inches" =  "international inch"        // alias
     * "international feet" =    "international foot"        // alias
     *
     * "inch" =       "international inch"    // alias
     * "foot" =       "international foot"    // alias
     * "yard" =       "international yard"    // alias
     * "mile" =       "international mile"    // alias
     *
     * // The following should hold regardless:
     * "inches" =     "inch"        // alias
     * "in" =         "inches"        // alias
     * "feet" =       "foot"        // alias
     * "ft" =         "feet"        // alias
     * "yd" =         "yard"        // alias
     * "mi" =         "mile"        // alias
     *
     * "chain" =      get("m").scale(2.011684e1)
     *
     * "pica" =               get("printers point").scale(12)    // exact
     * "printers pica" =      "pica"
     * "astronomicalunit" =   "au"
     * "astronomical unit" =  "au"
     * "asu" =                "au"
     * "nmile" =              "nautical mile"
     * "nmi" =                "nautical mile"
     *
     * "big point" =    get("inch").scale(1./72)    // exact
     * "barleycorn" =   get("inch").scale(1./3)
     *
     * "arpentlin" =    get("foot").scale(191.835)
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="time">
     * <h3>UNITS OF TIME</h3>
     * </a>
     * <pre>
     * NOTE:
     * Interval between 2 successive passages of sun through vernal equinox
     * (365.242198781 days -- see
     * http://www.ast.cam.ac.uk/pubinfo/leaflets/,
     * http://aa.usno.navy.mil/AA/
     * and http://adswww.colorado.edu/adswww/astro coord.html):
     *
     * "year" =              get("s").scale(3.15569259747e7)
     * "d" =                 get("s").scale(8.64e4)    // exact
     * "h" =                 get("s").scale(3.6e3)        // exact
     * "min" =               get("s").scale(60)        // exact
     * "shake" =             get("s").scale(1e-8)        // exact
     * "sidereal day" =      get("s").scale(8.616409e4)
     * "sidereal hour" =     get("s").scale(3.590170e3)
     * "sidereal minute" =   get("s").scale(5.983617e1)
     * "sidereal second" =   get("s").scale(0.9972696)
     * "sidereal year" =     get("s").scale(3.155815e7)
     *
     * "day" =           "d"
     * "hour" =          "h"
     * "minute" =        "min"
     * "sec" =           "s"                // avoid
     * "lunar month" =   get("d").scale(29.530589)
     *
     * "common year" =   get("d").scale(365)
     * // exact: 153600e7 seconds
     * "leap year" =       get("d").scale(366)        // exact
     * "Julian year" =     get("d").scale(365.25)    // exact
     * "Gregorian year" =  get("d").scale(365.2425)    // exact
     * "tropical year" =   "year"
     * "sidereal month" =  get("d").scale(27.321661)
     * "tropical month" =  get("d").scale(27.321582)
     * "fortnight" =       get("d").scale(14)
     * "week" =            get("d").scale(7)        // exact
     *
     * "jiffy" =       get("s").scale(1e-2)        // it's true
     * "eon" =         get("y").scale(1e9)        // fuzzy
     * "month" =       get("y").scale(1./12)        // on average
     *
     * "tropical year" =   "year"
     * "yr" =              "year"
     * "a" =               "year"        // "anno"
     * "ann" =             "year"        // "anno"
     * "hr" =              "h"
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <a name="angle">
     * <h3>UNITS OF PLANE ANGLE</h3>
     *
</a>
     * <pre>
     * "circle" =      get("radian").scale(2*Math.PI)
     * "deg" =         get("radian").scale(Math.PI/180.)
     * "'" =           get("deg").scale(1./60)
     * "\"" =          get("deg").scale(1./3600)
     * "grade" =       get("deg").scale(0.9)    // exact
     * "cycle" =       get("circle")
     *
     * "turn" =            "circle"
     * "revolution" =      "cycle"
     * "gon" =             "grade"
     * "angular degree" =  "deg"
     * "angular minute" =  "'"
     * "angular second" =  "\""
     * "arcdeg" =          "deg"
     * "degree" =          "deg"
     * "arcminute" =       "'"
     * "mnt" =             "'"
     * "arcsecond" =       "\""
     * // px("sec" =       "\""    // avoid
     * "arcmin" =          "'"
     * "arcsec" =          "\""
     *
     * "degree true" =    get("deg")
     * "degrees true" =   get("deg")
     * "degrees north" =  get("deg")
     * "degrees east" =   get("deg")
     * "degrees south" =  get("degrees north").scale(-1)
     * "degrees west" =   get("degrees east").scale(-1)
     *
     * "degree north" =  "degrees north"
     * "degreeN" =       "degrees north"
     * "degree N" =      "degrees north"
     * "degreesN" =      "degrees north"
     * "degrees N" =     "degrees north"
     *
     * "degree east" =   "degrees east"
     * "degreeE" =       "degrees east"
     * "degree E" =      "degrees east"
     * "degreesE" =      "degrees east"
     * "degrees E" =     "degrees east"
     *
     * "degree west" =   "degrees west"
     * "degreeW" =       "degrees west"
     * "degree W" =      "degrees west"
     * "degreesW" =      "degrees west"
     * "degrees W" =     "degrees west"
     *
     * "degree true" =   "degrees true"
     * "degreeT" =       "degrees true"
     * "degree T" =      "degrees true"
     * "degreesT" =      "degrees true"
     * "degrees T" =     "degrees true"
     *
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <a name="special">
     * <h3>The following are derived units with special names. They are useful for defining other
     * derived units.</h3>
     * </a>
     * <pre>
     * "Hz" =       get("second").pow(-1)
     * "N" =        get("kg").multiply(get("m").divide(get("s").pow(2)))
     * "C" =        get("A").multiply(get("s"))
     * "lm" =       get("cd").multiply(get("sr"))
     * "Bq" =       get("Hz") // SI unit of activity of a radionuclide
     *
     * "standard free fall" =   get("m").divide( get("s").pow(2)).scale(9.806650)
     * "Pa" =                   get("N").divide(get("m").pow(2))
     * "J" =                    get("N").multiply(get("m"))
     * "lx" =                   get("lm").divide(get("m").pow(2))
     * "sphere" =               get("steradian").scale(4*Math.PI)
     * "W" =                    get("J").divide(get("s"))
     * "Gy" =                   get("J").divide(get("kg")) // absorbed dose. derived unit
     * "Sv" =                   get("J").divide(get("kg")) // dose equivalent. derived unit
     *
     * "V" =        get("W").divide(get("A"))
     * "F" =        get("C").divide(get("V"))
     * "Ohm" =      get("V").divide(get("A"))
     * "S" =        get("A").divide(get("V"))
     * "Wb" =       get("V").multiply(get("s"))
     * "T" =        get("Wb").divide(get("m").pow(2))
     * "H" =        get("Wb").divide(get("A"))
     *
     * "newton" =    "N"
     * "hertz" =     "Hz"
     * "watt" =      "W"
     * "force" =     "standard free fall"
     * "gravity" =   "standard free fall"
     * "free fall" = "standard free fall"
     *
     * "conventional mercury" =   get("gravity").multiply(
     * get("kg").divide(get("m").pow(3))).scale(13595.10)
     * "mercury 0C" =             get("gravity").multiply(
     * get("kg").divide(get("m").pow(3))).scale(13595.1)
     * "mercury 60F" =            get("gravity").multiply(
     * get("kg").divide(get("m").pow(3))).scale(13556.8)
     * "conventional water" =     get("gravity").multiply(
     * get("kg").divide(get("m").pow(3))).scale(1000)    // exact
     * "water 4C" =                get("gravity").multiply(
     * get("kg").divide(get("m").pow(3))).scale(999.972)
     * "water 60F" =               get("gravity").multiply(
     * get("kg").divide(get("m").pow(3))).scale(999.001)
     *
     * NOTE: "g" =   get("gravity"))    // approx.  should be `local'.avoid.
     *
     * "mercury 32F" =   "mercury 0C"
     * "water 39F" =     "water 4C"    // actually 39.2 degF
     * "mercury" =       "conventional mercury"
     * "water" =         "conventional water"
     *
     * "farad" =    "F"
     * "Hg" =       "mercury"
     * "H2O" =      "water"
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="accel">
     * <h3>ACCELERATION</h3>
     *
</a>
     * <pre>
     * "Gal" =       get("m").divide(get("s").pow(2)).scale(1e-2)
     * "gals" =      "Gal"        // avoid "gal" (gallon)
     * </pre>
     *
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="area">
     * <h3>AREA</h3>
     * </a>
     * <pre>
     * "are" =            get("m").pow(2).scale(1e2)    // exact
     * "barn" =           get("m").pow(2).scale(1e-28)    // exact
     * "circular mil" =   get("m").pow(2).scale(5.067075e-10)
     * "darcy" =          get("m").pow(2).scale(9.869233e-13)  // permeability of porous solids
     * "hectare" =        get("hectoare")        // exact
     * "har" =            "hectare"            // exact
     * "acre" =           get("rod").pow(2).scale(160)    // exact
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     *
<a name="electmag">
     * <h3>ELECTRICITY AND MAGNETISM</h3>
     * </a>
     * <pre>
     * "abfarad" =            get("F").scale(1e9)    // exact
     * "abhenry" =            get("H").scale(1e-9)    // exact
     * "abmho" =              get("S").scale(1e9)    // exact
     * "abohm" =              get("Ohm").scale(1e-9)    // exact
     * "megohm" =             get("Ohm").scale(1e6)    // exact
     * "kilohm" =             get("Ohm").scale(1e3)    // exact
     * "abvolt" =             get("V").scale(1e-8)    // exact
     * "e" =                  get("C").scale(1.60217733-19)
     * "chemical faraday" =   get("C").scale(9.64957e4)
     * "physical faraday" =   get("C").scale(9.65219e4)
     * "C12 faraday" =        get("C").scale(9.648531e4)
     * "gamma" =              get("nT")            // exact
     * "gauss" =              get("T").scale(1e-4)        // exact
     * "maxwell" =            get("Wb").scale(1e-8)        // exact
     * "Oe" =                 get("A").divide(get("m")).scale(7.957747e1)
     * "statcoulomb" =        get("C").scale(3.335640e-10)
     * "statfarad" =          get("F").scale(1.112650e-12)
     * "stathenry" =          get("H").scale(8.987554e11)
     * "statmho" =            get("S").scale(1.112650e-12)
     * "statohm" =            get("Ohm").scale(8.987554e11)
     * "statvolt" =           get("V").scale(2.997925e2)
     * "unit pole" =          get("Wb").scale(1.256637e-7)
     *
     * "henry" =     "H"
     * "siemens" =   "S"
     * "ohm" =       "Ohm"
     * "tesla" =     "T"
     * "volt" =      "V"
     * "weber" =     "Wb"
     * "mho" =       "siemens"
     * "oersted" =   "Oe"
     * "faraday" =   "C12 faraday"    // charge of 1 mole of electrons
     * "coulomb" =   "C"
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="energy">
     * <h3>ENERGY (INCLUDES WORK)</h3>
     * </a>
     * <pre>
     * "eV" =                     get("J").scale(1.602177e-19)
     * "bev" =                    get("eV").scale(1e9)
     * "erg" =                    get("J").scale(1e-7)    // exact
     * "IT Btu" =                 get("J").scale(1.05505585262e3)    // exact
     * "EC therm" =               get("J").scale(1.05506e8)    // exact
     * "thermochemical calorie" = get("J").scale(4.184000)    // exact
     * "IT calorie" =             get("J").scale(4.1868)    // exact
     * "ton TNT" =                get("J").scale(4.184e9)
     * "US therm" =               get("J").scale(1.054804e8)    // exact
     * "Wh" =                     get("W").multiply(get("h"))
     *
     * "joule" =          "J"
     * "therm" =          "US therm"
     * "watthour" =       "Wh"
     * "Btu" =            "IT Btu"
     * "calorie" =        "IT calorie"
     * "electronvolt" =   "eV"
     * "electron volt" =  "eV"
     * "thm" =            "therm"
     * "cal" =            "calorie"
     *
     * </pre>
     *
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="force">
     * <h3>FORCE</h3>
     * </a>
     * <pre>
     * "dyne" =            get("N").scale(1e-5)        // exact
     * "pond" =            get("N").scale(9.806650e-3)    // exact
     * "force kilogram" =  get("N").scale(9.806650)    // exact
     * "force gram" =      get("N").scale(9.806650e-3)    // exact
     * "force ounce" =     get("N").scale(2.780139e-1)
     * "force pound" =     get("N").scale(4.4482216152605)    // exact
     * "poundal" =         get("N").scale(1.382550e-1)
     * "force ton" =       get("force pound").scale(2000)    // exact
     *
     * "gf" =              "force gram"
     * "lbf" =             "force pound"
     * "ounce force" =     "force ounce"
     * "kilogram force" =  "force kilogram"
     * "pound force" =     "force pound"
     * "ozf" =             "force ounce"
     * "kgf" =             "force kilogram"
     * "ton force" =       "force ton"
     * "gram force" =      "force gram"
     *
     * "kip" =             get("lbf").scale(1e3)
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="heat">
     * <h3>HEAT</h3>
     *
</a>
     * <pre>
     * "clo" =        get("K").multiply( get("m").pow(2).divide(get("W"))).scale(1.55e-1)
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="light">
     * <h3>LIGHT</h3>
     * </a>
     * <pre>
     * "lumen" =        "lm"
     * "lux" =          "lx"
     * "footcandle" =   get("lux").scale(1.076391e-1)
     * "footlambert" =  get("cd").divide( get("m").pow(2)).scale(3.426259)
     * "lambert" =      get("cd").divide( get("m").pow(2)).scale(1e4/Math.PI)    // exact
     * "stilb" =    get("cd").divide( get("m").pow(2)).scale(1e4)
     * "phot" =     get("lm").divide( get("m").pow(2)).scale(1e4)        // exact
     * "nit" =      get("cd").multiply( get("m").pow(2))                // exact
     * "langley" =  get("J").divide( get("m").pow(2)).scale(4.184000e4)    // exact
     * "blondel" =  get("cd").divide( get("m").pow(2)).scale(1./Math.PI)
     *
     * "apostilb" = "blondel"
     * "nt" =       "nit"
     * "ph" =       "phot"
     * "sb" =       "stilb"
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="massperlen">
     *
     <h3>MASS PER UNIT LENGTH</h3>
     * </a>
     * <pre>
     * "denier" =  get("kg").divide(get("m")).scale(1.111111e-7)
     * "tex" =     get("kg").divide(get("m")).scale(1e-6)
     *
     * </pre>
     *
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="masspertime">
     * <h3>MASS PER UNIT TIME (INCLUDES FLOW)</h3>
     * </a>
     * <pre>
     *
     * "perm 0C" =       get("kg").divide(
     * get("Pa").multiply(get("s")).multiply(
     * get("m").pow(2))).scale(5.72135e-11)
     *
     * "perm 23C" =       get("kg").divide(
     * get("Pa").multiply(get("s")).multiply(
     * get("m").pow(2))).scale(5.74525e-11)
     *
     * </pre>
     *
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="power">
     * <h3>POWER</h3>
     * </a>
     * <pre>
     * "VA" =                  get("V").multiply(get("A"))
     * "voltampere" =          "VA"
     * "boiler horsepower" =   get("W").scale(9.80950e3)
     * "shaft horsepower" =    get("W").scale(7.456999e2)
     * "metric horsepower" =   get("W").scale(7.35499)
     * "electric horsepower" = get("W").scale(7.460000e2)    // exact
     * "water horsepower" =    get("W").scale(7.46043e2)
     * "UK horsepower" =       get("W").scale(7.4570e2)
     * "refrigeration ton" =   get("Btu").divide(get("h")).scale(12000)
     *
     * "horsepower" =             "shaft horsepower"
     * "ton of refrigeration" =   "refrigeration ton"
     * "hp" =                     "horsepower"
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="pressure">
     * <h3>PRESSURE OR STRESS</h3>
     *
</a>
     * <pre>
     *
     * "bar" =       get("Pa").scale(1e5)    // exact
     * "standard atmosphere" =   get("Pa").scale(1.01325e5)    // exact
     * "technical atmosphere" =   get("kg").multiply(get("gravity").divide(
     * get("m").scale(.01).pow(2)))
     *
     * "inch H2O 39F" =  get("inch").multiply(get("water 39F"))
     * "inch H2O 60F" =  get("inch").multiply(get("water 60F"))
     * "inch Hg 32F" =   get("inch").multiply(get("mercury 32F"))
     * "inch Hg 60F" =   get("inch").multiply(get("mercury 60F"))
     * "mm Hg 0C" =      get("m").scale(1e-3).multiply( get("mercury 0C"))
     * "cmHg" =          get("m").scale(1e-2).multiply(get("Hg"))
     * "cmH2O" =         get("m").scale(1e-2).multiply(get("water"))
     * "inch Hg" =       get("inch").multiply(get("Hg"))
     * "torr" =          get("m").scale(1e-3).multiply(get("Hg"))
     * "foot H2O" =      get("foot").multiply(get("water"))
     * "psi" =    get("pound").multiply( get("gravity").divide(get("inch").pow(2)))
     * "ksi" =    get("kip").divide(get("inch").pow(2))
     * "barie" =  get("N").divide(get("m").pow(2)).scale(0.1)
     *
     * "footH2O" =         "foot H2O"
     * "ftH2O" =           "foot H2O"
     * "millimeter Hg" =   "torr"
     * "mm Hg" =           "torr"
     * "mm Hg" =           "torr"
     * "pascal" =          "Pa"
     * "pal" =             "Pa"
     * "inHg" =            "inch Hg"
     * "in Hg" =           "inch Hg"
     * "at" =              "technical atmosphere"
     * "atmosphere" =      "standard atmosphere"
     * "atm" =             "standard atmosphere"
     * "barye" =           "barie"
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="rad">
     * <h3>RADIATION UNITS</h3>
     * </a>
     * <pre>
     * "Ci" =     get("Bq").scale(3.7e10)    // exact
     * "rem" =    get("Sv").scale(1e-2)    // exact dose equivalent
     * "rd" =     get("Gy").scale(1e-2)    // absorbed dose. exact.
     * // use instead of "rad"
     * "R" =      get("C").divide(get("kg")).scale(2.58e-4)
     *
     * "gray" =         "Gy"
     * "sie" =          "Sv"
     * "becquerel" =    "Bq"
     * "rads" =         "rd"        // avoid "rad" (radian)
     * "roentgen" =     "R"
     * "curie" =        "Ci"
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="vel">
     *
     <h3>VELOCITY (INCLUDES SPEED)</h3>
     * </a>
     * <pre>
     * "c" =    get("m").divide( get("s")).scale(2.997925e+8)
     * "kt" =   get("nautical mile").divide(get("h"))
     *
     * "knot international" =   "kt"
     * "international knot" =   "kt"
     * "knot" =                 "kt"
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="viscos">
     * <h3>VISCOSITY</h3>
     * </a>
     * <pre>
     * "P" =        get("Pa").multiply(get("s")).scale(1e-1) // exact
     * "St" =       get("m").pow(2).divide(get("s")).scale(1e-4) // exact
     * "rhe" =      get("Pa").multiply(get("s")).pow(-1).scale(10)
     *
     * "poise" =    "P"
     * "stokes" =   "St"
     *
     * </pre>
     * <a href="#top">Back to top</a>
     *
<p>
     * <a name="volu">
     * <h3>VOLUME (INCLUDES CAPACITY)</h3>
     * </a>
     * <pre>
     * "acre feet" =          get("m").pow(3).scale(1.233489e3)
     * // but `acre foot' is 1233.4867714897 m^3.  Odd.
     *
     * "board feet" =         get("m").pow(3).scale(2.359737e-3)
     * "bushel" =             get("m").pow(3).scale(3.523907e-2)
     * "UK liquid gallon" =   get("m").pow(3).scale(4.546090e-3)    // exact
     * "Canadian liquid gallon"= get("m").pow(3).scale(4.546090e-3)    // exact
     * "US dry gallon" =      get("m").pow(3).scale(4.404884e-3)
     * "US liquid gallon" =   get("m").pow(3).scale(3.785412e-3)
     * "cc" =                 get("m").scale(.01).pow(3)
     * "liter" =              get("m").pow(3).scale(1e-3)
     * // exact. However, from 1901 to 1964, 1 liter = 1.000028 dm3
     * "stere" =              get("m").pow(3)    // exact
     * "register ton" =       get("m").pow(3).scale(2.831685)
     * "US dry quart" =       get("US dry gallon").scale(1./4)
     * "US dry pint" =        get("US dry gallon").scale(1./8)
     * "US liquid quart" =    get("US liquid gallon").scale(1./4)
     * "US liquid pint" =     get("US liquid gallon").scale(1./8)
     * "US liquid cup" =      get("US liquid gallon").scale(1./16)
     * "US liquid gill" =     get("US liquid gallon").scale(1./32)
     * "US liquid ounce" =    get("US liquid gallon").scale(1./128)
     * "UK liquid quart" =    get("UK liquid gallon").scale(1./4)
     * "UK liquid pint" =     get("UK liquid gallon").scale(1./8)
     * "UK liquid cup" =      get("UK liquid gallon").scale(1./16)
     * "UK liquid gill" =     get("UK liquid gallon").scale(1./32)
     * "UK liquid ounce" =    get("UK liquid gallon").scale(1./160)
     *
     * "US fluid ounce" =    "US liquid ounce"
     * "UK fluid ounce" =    "UK liquid ounce"
     * "liquid gallon" =     "US liquid gallon"
     * "fluid ounce" =       "US fluid ounce"
     * "dry quart" =         "US dry quart"
     * "dry pint" =          "US dry pint"
     *
     * "liquid quart" =  get("liquid gallon").scale(1./4)
     * "liquid pint" =   get("liquid gallon").scale(1./8)
     * "bbl" =           get("US liquid gallon").scale(42) // petroleum industry definition
     * "pt" =            get("liquid pint")
     *
     * "gallon" =        "liquid gallon"
     * "quart" =         "liquid quart"
     *
     * "cup" =        get("liquid gallon").scale(1./16)
     * "gill" =       get("liquid gallon").scale(1./32)
     * "tablespoon" = get("US fluid ounce").scale(0.5)
     * "teaspoon" =   get("tablespoon").scale(1./3)
     * "peck" =       get("bushel").scale(1./4)
     *
     * "acre foot" =    "acre feet"
     * "board foot" =   "board feet"
     * "barrel" =       "bbl"
     *
     * "gal" =       get("gallon")    // "gal" is also acceleration unit
     *
     * "oz" =       "fluid ounce"
     * "floz" =     "fluid ounce"
     * "Tbl" =      "tablespoon"
     * "Tbsp" =     "tablespoon"
     * "tbsp" =     "tablespoon"
     * "Tblsp" =    "tablespoon"
     * "tblsp" =    "tablespoon"
     * "litre" =    "liter"
     * "L" =        "liter"
     * "l" =        "liter"
     * "tsp" =      "teaspoon"
     * "pk" =       "peck"
     * "bu" =       "bushel"
     *
     * "fldr" =     get("floz").scale(1./8)
     * "dr" =       get("floz").scale(1./16)
     *
     * "firkin" =   get("bbl").scale(1./4)
     * // exact but "barrel" is vague
     * "pint" =     "pt"
     * "dram" =     "dr"
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="voltime">
     * <h3>VOLUME PER UNIT TIME</h3>
     * </a>
     * <pre>
     *
     * "sverdrup" =  get("m").pow(3).scale(1e6).divide(get("s"))    // oceanographic flow
     *
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="computer">
     * <h3>COMPUTERS AND COMMUNICATION</h3>
     * </a>
     * <pre>
     * "bit" =    new ScaledUnit(1)    // unit of information
     * "Bd" =     get("Hz")
     * "bps" =    get("Hz")
     * "cps" =    get("cycle").divide(get("s"))
     * "baud" =   "Bd"
     *
     * </pre>
     * <a href="#top">Back to top</a>
     * <p>
     * <a name="misc">
     * <h3>MISC</h3>
     *
</a>
     * <pre>
     * "kayser" =         get("m").pow(-1).scale(1e2)    // exact
     * "rps" =            get("revolution").divide(get("s"))
     * "rpm" =            get("revolution").divide(get("min"))
     * "geopotential" =   get("gravity")
     * "work year" =      get("hours").scale(2056)
     * "work month" =     get("work year").scale(1./12)
     *
     * "count" =          ""
     * "gp" =             "geopotential"
     * "dynamic" =        "geopotential"
     * "gpm" =             get("geopotential").multiply(get("meter"))
     * </pre>
     * <br>
     * <a href="#top">Back to top</a>
     *
     */
    static public Unit getUnit(String unitSpec)
    {
        try
        {
            return Parser.parse(unitSpec);
        }
        catch (Exception ex)
        {
            String msg = "The unit spec: \"" + unitSpec + "\" could not be parsed! \n" + ex.getMessage();
            Logger.getLogger(Reals.class.getName()).severe(msg);
            throw new RuntimeException(ex);
        }
    }
}
