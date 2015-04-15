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
package com.emxsys.solar.api;

import com.emxsys.visad.RealXmlBindings;
import java.time.OffsetTime;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import visad.Real;

/**
 * The OffsetTimeXmlAdaptor marshals a Java OffsetTime Real to XML via the JAXB XmlAdapter and the
 * XmlElements defined in OffsetTimeXmlBinding. The RealXmlBindings class maps the Real type, value
 * and unit properties to XmlElements.
 *
 * To use, JavaBean properties that return a Real should be annotated with
 * <code>@XmlJavaTypeAdapter(OffsetTimeXmlAdaptor.class)</code> Example:
 * <pre>
 * @code{
 * @XmlElement
 * @XmlJavaTypeAdapter(RealXmlAdaptor.class)
 * public Real getDead1HrFuelLoad() {
 *       return this.dead1HrFuelLoad;
 * }
 * </pre>
 *
 * @see RealXmlBindings
 *
 * @author Bruce Schubert
 */
public class OffsetTimeXmlAdaptor extends XmlAdapter<OffsetTimeXmlBinding, OffsetTime> {

    @Override
    public OffsetTime unmarshal(OffsetTimeXmlBinding binding) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public OffsetTimeXmlBinding marshal(OffsetTime time) throws Exception {
        return new OffsetTimeXmlBinding(time);
    }

}