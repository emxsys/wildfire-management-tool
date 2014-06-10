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
package com.emxsys.wildfire.api;

import com.emxsys.visad.FireUnit;
import com.emxsys.visad.GeneralUnit;
import static com.emxsys.visad.Reals.*;
import visad.CommonUnit;
import visad.RealTupleType;
import visad.RealType;

/**
 * Wildfire VisAD types.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class WildfireType {

    // Fuel model types
    /** Fuel loading: kg/m2 */
    public static final RealType FUEL_LOAD_SI;
    /** Fuel model fuel loading: tons/acre */
    public static final RealType FUEL_LOAD_US;
    /** Fuel loading: lb/ft2 */
    public static final RealType FUEL_LOAD;
    /** Surface area to volume ratio: m2/m3 */
    public static final RealType SAV_RATIO_SI;
    /** Surface area to volume ratio: ft2/ft3 */
    public static final RealType SAV_RATIO_US;
    /** Heat content: kJ/kg */
    public static final RealType HEAT_CONTENT_SI;
    /** Heat content: Btu/lb */
    public static final RealType HEAT_CONTENT_US;
    /** Fuel bed depth: m */
    public static final RealType FUEL_DEPTH_SI;
    /** Fuel bed depth: ft */
    public static final RealType FUEL_DEPTH_US;
    /** Moisture of extinction: % */
    public static final RealType MOISTURE_OF_EXTINCTION;

    /** Fuel condition tuple */
    public final static RealTupleType FUEL_CONDITION;

    // Fuel components
    /** Dead 1 hour fine fuel load: [tons/acre] */
    public static final RealType LOAD_DEAD_1H;
    /** Dead 10 hour fuel load: [tons/acre] */
    public static final RealType LOAD_DEAD_10H;
    /** Dead 100 hour fuel load: [tons/acre] */
    public static final RealType LOAD_DEAD_100H;
    /** Live herb fuel load: [tons/acre] */
    public static final RealType LOAD_LIVE_HERB;
    /** Live woody fuel load: [tons/acre] */
    public static final RealType LOAD_LIVE_WOODY;

    /** Dead 1 hour fine fuelbed load: [lb/ft2] */
    public static final RealType FUELBED_LOAD_DEAD_1H;
    /** Dead 10 hour fuelbed load: [lb/ft2] */
    public static final RealType FUELBED_LOAD_DEAD_10H;
    /** Dead 100 hour fuelbed load: [lb/ft2] */
    public static final RealType FUELBED_LOAD_DEAD_100H;
    /** Dead herbaceous fuelbed load: [lb/ft2] */
    public static final RealType FUELBED_LOAD_DEAD_HERB;
    /** Live herb fuel fuelbed load: [lb/ft2] */
    public static final RealType FUELBED_LOAD_LIVE_HERB;
    /** Live woody fuel fuelbed load: [lb/ft2] */
    public static final RealType FUELBED_LOAD_LIVE_WOODY;

    /** Dead 1 hour fine fuel surface to volume ratio: [ft2/ft3] */
    public static final RealType FUELBED_SAV_DEAD_1H;
    /** Dead 10 hour fuel surface to volume ratio: [ft2/ft3] */
    public static final RealType FUELBED_SAV_DEAD_10H;
    /** Dead 100 hour fuel surface to volume ratio: [ft2/ft3] */
    public static final RealType FUELBED_SAV_DEAD_100H;
    /** Dead herbaceous fuel surface to volume ratio: [ft2/ft3] */
    public static final RealType FUELBED_SAV_DEAD_HERB;
    /** Live herb fuel surface to volume ratio: [ft2/ft3] */
    public static final RealType FUELBED_SAV_LIVE_HERB;
    /** Live woody fuel surface to volume ratio: [ft2/ft3] */
    public static final RealType FUELBED_SAV_LIVE_WOODY;

    /** Dead 1 hour fine fuel surface to volume ratio: [ft2/ft3] */
    public static final RealType SAV_DEAD_1H;
    /** Dead 10 hour fuel surface to volume ratio: [ft2/ft3] */
    public static final RealType SAV_DEAD_10H;
    /** Dead 100 hour fuel surface to volume ratio: [ft2/ft3] */
    public static final RealType SAV_DEAD_100H;
    /** Live herb fuel surface to volume ratio: [ft2/ft3] */
    public static final RealType SAV_LIVE_HERB;
    /** Live woody fuel surface to volume ratio: [ft2/ft3] */
    public static final RealType SAV_LIVE_WOODY;
    /** 1 hour fine fuel moisture content: [%] */
    public static final RealType FUEL_MOISTURE_1H;
    /** 10 hour fuel moisture content: [%] */
    public static final RealType FUEL_MOISTURE_10H;
    /** 100 hour fuel moisture content: [%] */
    public static final RealType FUEL_MOISTURE_100H;
    /** Live herb fuel moisture content: [%] */
    public static final RealType FUEL_MOISTURE_HERB;
    /** Live woody fuel moisture content: [%] */
    public static final RealType FUEL_MOISTURE_WOODY;
    /** Fuel bed depth: [ft] */
    public static final RealType FUEL_BED_DEPTH;
    /** Dead fuel moisture of extinction: mx [%] */
    public static final RealType MX_DEAD;
    
    /** Fuel model tuple */
    public final static RealTupleType FUEL_MODEL;
    /** Fuel moisture tuple */
    public final static RealTupleType FUEL_MOISTURE;
    /** Fuel characteristics tuple */
    public final static RealTupleType FUEL_BED;

    // Fuel combusible properties
    /** Characteristic surface area-to-volume ratio - sigma: [ft2/ft3] */
    public static final RealType SIGMA;
    /** Live Moisture of extinction - mx_live: [%] */
    public static final RealType MX_LIVE;
    /** Mean bulk density: fuel-bed weight per unit volume - rho_b: [lbs/ft3] */
    public static final RealType RHO_B;
    /** Fuel-particle weight per unit volume- rho_p: [lbs/ft3] */
    public static final RealType RHO_P;
    /** Mean Packing ratio - beta: [-] */
    public static final RealType BETA;
    /** Optimal Packing ratio - beta: [-] */
    public static final RealType BETA_OPT;
    /** Relative Packing ratio - beta: [-] */
    public static final RealType BETA_RATIO;
    /** Optimum Reaction Velocity: [1/min] */
    public static final RealType GAMMA;
    /** Mineral Damping Coefficient: [-] */
    public static final RealType ETA_S;
    /** Moisture Damping Coefficient: [-] */
    public static final RealType ETA_M;
    /** Heat Source [BTU/ft2/min] */
    public static final RealType HEAT_SOURCE;
    /** Heat Source [BTU/ft3] */
    public static final RealType HEAT_SINK;
    /** Reaction Intensity: [BTU/ft2/min] */
    public static final RealType I_R;
    /** Fuel combustible tuple */
    public final static RealTupleType COMBUSTIBLE;

    // Fire behavior types
    public static final RealType FIRE_LINE_INTENSITY_SI;
    public static final RealType FIRE_LINE_INTENSITY_US;
    public static final RealType FLAME_LENGTH_SI;
    public static final RealType FLAME_LENGTH_US;
    public static final RealType HEAT_RELEASE_SI;
    public static final RealType HEAT_RELEASE_US;
    public static final RealType RATE_OF_SPREAD_SI;
    public static final RealType RATE_OF_SPREAD_US;
    public static final RealType DIR_OF_SPREAD;
    public final static RealTupleType FIRE_BEHAVIOR;
    // Terrain
    public static final RealType ASPECT;
    public static final RealType SLOPE;
    public static final RealType ELEVATION;
    public final static RealTupleType TERRAIN;
    // Environment
    public static final RealType FUEL_TEMP_C;
    public static final RealType FUEL_TEMP_F;

    // Initializer
    static {
        FUEL_TEMP_C = RealType.getRealType("fuel_temp:C", GeneralUnit.degC, null);
        FUEL_TEMP_F = RealType.getRealType("fuel_temp:F", GeneralUnit.degF, null);
        FUEL_LOAD_SI = RealType.getRealType("fuel_load:kg/m2", FireUnit.kg_m2, null);
        FUEL_LOAD_US = RealType.getRealType("fuel_load:tons/acre", FireUnit.tons_acre, null);
        FUEL_LOAD = RealType.getRealType("fuel_load:lb/ft2", FireUnit.lb_ft2, null);

        LOAD_DEAD_1H = RealType.getRealType("dead_1h:tons/acre", FireUnit.tons_acre, null);
        LOAD_DEAD_10H = RealType.getRealType("dead_10h:tons/acre", FireUnit.tons_acre, null);
        LOAD_DEAD_100H = RealType.getRealType("dead_100h:tons/acre", FireUnit.tons_acre, null);
        LOAD_LIVE_HERB = RealType.getRealType("live_herb:tons/acre", FireUnit.tons_acre, null);
        LOAD_LIVE_WOODY = RealType.getRealType("live_woody:tons/acre", FireUnit.tons_acre, null);
        
        FUELBED_LOAD_DEAD_1H = RealType.getRealType("dead_1h:lb/ft2", FireUnit.lb_ft2, null);
        FUELBED_LOAD_DEAD_10H = RealType.getRealType("dead_10h:lb/ft2", FireUnit.lb_ft2, null);
        FUELBED_LOAD_DEAD_100H = RealType.getRealType("dead_100h:lb/ft2", FireUnit.lb_ft2, null);
        FUELBED_LOAD_DEAD_HERB = RealType.getRealType("dead_herb:lb/ft2", FireUnit.lb_ft2, null);
        FUELBED_LOAD_LIVE_HERB = RealType.getRealType("live_herb:lb/ft2", FireUnit.lb_ft2, null);
        FUELBED_LOAD_LIVE_WOODY = RealType.getRealType("live_woody:lb/ft2", FireUnit.lb_ft2, null);
        
        FUELBED_SAV_DEAD_1H = RealType.getRealType("dead_1h:ft2/ft3", FireUnit.ft2_ft3, null);
        FUELBED_SAV_DEAD_10H = RealType.getRealType("dead_10h:ft2/ft3", FireUnit.ft2_ft3, null);
        FUELBED_SAV_DEAD_100H = RealType.getRealType("dead_100h:ft2/ft3", FireUnit.ft2_ft3, null);
        FUELBED_SAV_DEAD_HERB = RealType.getRealType("dead_herb:ft2/ft3", FireUnit.ft2_ft3, null);
        FUELBED_SAV_LIVE_HERB = RealType.getRealType("live_herb:ft2/ft3", FireUnit.ft2_ft3, null);
        FUELBED_SAV_LIVE_WOODY = RealType.getRealType("live_woody:ft2/ft3", FireUnit.ft2_ft3, null);
        
        SAV_DEAD_1H = RealType.getRealType("dead_1h:ft2/ft3", FireUnit.ft2_ft3, null);
        SAV_DEAD_10H = RealType.getRealType("dead_10h:ft2/ft3", FireUnit.ft2_ft3, null);
        SAV_DEAD_100H = RealType.getRealType("dead_100h:ft2/ft3", FireUnit.ft2_ft3, null);
        SAV_LIVE_HERB = RealType.getRealType("live_herb:ft2/ft3", FireUnit.ft2_ft3, null);
        SAV_LIVE_WOODY = RealType.getRealType("live_woody:ft2/ft3", FireUnit.ft2_ft3, null);
        FUEL_MOISTURE_1H = RealType.getRealType("fuel_moisture_1h:%", GeneralUnit.percent, null);
        FUEL_MOISTURE_10H = RealType.getRealType("fuel_moisture_10h:%", GeneralUnit.percent, null);
        FUEL_MOISTURE_100H = RealType.getRealType("fuel_moisture_100h:%", GeneralUnit.percent, null);
        FUEL_MOISTURE_HERB = RealType.getRealType("fuel_moisture_herb:%", GeneralUnit.percent, null);
        FUEL_MOISTURE_WOODY = RealType.getRealType("fuel_moisture_woody:%", GeneralUnit.percent, null);
        MX_DEAD = RealType.getRealType("dead_moisture_of_ext:%", GeneralUnit.percent, null);
        FUEL_BED_DEPTH = RealType.getRealType("fuel_depth:ft", GeneralUnit.foot, null);
        FUEL_MODEL = newRealTupleType(
                new RealType[]{
                    LOAD_DEAD_1H,
                    LOAD_DEAD_10H,
                    LOAD_DEAD_100H,
                    LOAD_LIVE_HERB,
                    LOAD_LIVE_WOODY,
                    SAV_DEAD_1H,
                    SAV_DEAD_10H,
                    SAV_DEAD_100H,
                    SAV_LIVE_HERB,
                    SAV_LIVE_WOODY,
                    FUEL_BED_DEPTH,
                    MX_DEAD
                });
        FUEL_MOISTURE = newRealTupleType(
                new RealType[]{
                    FUEL_MOISTURE_1H,
                    FUEL_MOISTURE_10H,
                    FUEL_MOISTURE_100H,
                    FUEL_MOISTURE_HERB,
                    FUEL_MOISTURE_WOODY,
                });
        FUEL_BED = newRealTupleType(
                new RealType[]{
                    FUELBED_LOAD_DEAD_HERB,
                    FUELBED_LOAD_DEAD_1H,
                    FUELBED_LOAD_DEAD_10H,
                    FUELBED_LOAD_DEAD_100H,
                    FUELBED_LOAD_LIVE_HERB,
                    FUELBED_LOAD_LIVE_WOODY,
                    FUELBED_SAV_DEAD_HERB,
                    FUELBED_SAV_DEAD_1H,
                    FUELBED_SAV_DEAD_10H,
                    FUELBED_SAV_DEAD_100H,
                    FUELBED_SAV_LIVE_HERB,
                    FUELBED_SAV_LIVE_WOODY,
                    FUEL_BED_DEPTH,
                    MX_DEAD,
                });

        SIGMA = RealType.getRealType("fuel_complex:ft2/ft3", FireUnit.ft2_ft3, null);
        MX_LIVE = RealType.getRealType("live_moisture_of_ext:%", GeneralUnit.percent, null);
        RHO_B = RealType.getRealType("bulk_density:lb/ft3", FireUnit.lb_ft3, null);
        RHO_P = RealType.getRealType("particle_density:lb/ft3", FireUnit.lb_ft3, null);
        BETA = RealType.getRealType("mean_packing_ratio", CommonUnit.dimensionless, null);
        BETA_OPT = RealType.getRealType("optimal_packing_ratio", CommonUnit.dimensionless, null);
        BETA_RATIO = RealType.getRealType("relative_packing_ratio", CommonUnit.dimensionless, null);
        GAMMA =  RealType.getRealType("reaction_velocity:1/min", CommonUnit.dimensionless, null);
        HEAT_SOURCE =  RealType.getRealType("heat_source:BTU/ft2/min", FireUnit.Btu_ft2_min, null);
        HEAT_SINK =  RealType.getRealType("heat_sink:BTU/f3", FireUnit.Btu_f3, null);
        ETA_S =  RealType.getRealType("mineral_damping_coefficient",  CommonUnit.dimensionless, null);
        ETA_M =  RealType.getRealType("moisture_damping_coefficient",  CommonUnit.dimensionless, null);
        I_R =  RealType.getRealType("reaction_intensity:BTU/ft2/min", FireUnit.Btu_ft2_min, null);
        
        
        COMBUSTIBLE = newRealTupleType(
                new RealType[]{
                    SIGMA,
                    MX_LIVE,
                    RHO_B,
                    BETA,
                    BETA_OPT
                });

        SAV_RATIO_SI = RealType.getRealType("surface_to_volume:m2/m3", FireUnit.m2_m3, null);
        SAV_RATIO_US = RealType.getRealType("surface_to_volume:ft2/ft3", FireUnit.ft2_ft3, null);
        HEAT_CONTENT_SI = RealType.getRealType("heat_content:kJ/kg", FireUnit.kJ_kg, null);
        HEAT_CONTENT_US = RealType.getRealType("heat_content:Btu/lb", FireUnit.Btu_lb, null);
        FUEL_DEPTH_SI = RealType.getRealType("fuel_depth:m", CommonUnit.meter, null);
        FUEL_DEPTH_US = RealType.getRealType("fuel_depth:ft", GeneralUnit.foot, null);
        MOISTURE_OF_EXTINCTION = RealType.getRealType("moisture_of_extinction:%", GeneralUnit.percent, null);

        ASPECT = RealType.getRealType("aspect:deg", CommonUnit.degree, null);
        SLOPE = RealType.getRealType("slope:deg", CommonUnit.degree, null);
        ELEVATION = RealType.getRealType("elevation:m", CommonUnit.meter, null);
        TERRAIN = newRealTupleType(
                new RealType[]{
                    ASPECT,
                    SLOPE,
                    ELEVATION
                });
        FUEL_CONDITION = newRealTupleType(
                new RealType[]{
                    FUEL_MOISTURE_1H,
                    FUEL_MOISTURE_10H,
                    FUEL_MOISTURE_100H,
                    FUEL_MOISTURE_HERB,
                    FUEL_MOISTURE_WOODY,
                    FUEL_TEMP_C,});
        FIRE_LINE_INTENSITY_SI = RealType.getRealType("fire_line_intensity:kW/m", FireUnit.kW_m, null);
        FIRE_LINE_INTENSITY_US = RealType.getRealType("fire_line_intensity:Btu/ft/s", FireUnit.Btu_ft_s, null);
        FLAME_LENGTH_SI = RealType.getRealType("flame_length:m", CommonUnit.meter, null);
        FLAME_LENGTH_US = RealType.getRealType("flame_length:ft", GeneralUnit.foot, null);
        HEAT_RELEASE_SI = RealType.getRealType("heat_release:kJ/m2", FireUnit.kJ_m2, null);
        HEAT_RELEASE_US = RealType.getRealType("heat_release:Btu/ft2", FireUnit.Btu_ft2, null);
        RATE_OF_SPREAD_SI = RealType.getRealType("rate_of_spread:m/s", CommonUnit.meterPerSecond, null);
        RATE_OF_SPREAD_US = RealType.getRealType("rate_of_spread:chain/hr", FireUnit.chain_hour, null);
        DIR_OF_SPREAD = RealType.getRealType("dir_of_spread:deg", CommonUnit.degree, null);

        FIRE_BEHAVIOR = newRealTupleType(
                new RealType[]{
                    FIRE_LINE_INTENSITY_SI,
                    FLAME_LENGTH_SI,
                    RATE_OF_SPREAD_SI,
                    DIR_OF_SPREAD,
                    HEAT_RELEASE_SI
                });

    }
}
