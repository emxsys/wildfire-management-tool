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
package com.emxsys.visad;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import visad.Real;
import visad.RealType;

/**
 * The RealXmlAdapter marshals a VisAD Real to XML via the JAXB XmlAdapter and the XmlElements
 defined in RealElement. The RealElement class maps the Real type, value and unit properties
 * to XmlElements.
 *
 * To use, JavaBean properties that return a Real should be annotated with
 * <code>@XmlJavaTypeAdapter(RealXmlAdapter.class)</code> Example:
 * <pre> @code{
 * @XmlElement
 * @XmlJavaTypeAdapter(RealAdaptor.class)
 * public Real getDead1HrFuelLoad() {
 *       return this.dead1HrFuelLoad;
 * }
 * </pre>
 *
 * @see RealElement
 *
 * @author Bruce Schubert
 */
public class RealXmlAdapter extends XmlAdapter<RealElement, Real> {

    @Override
    public Real unmarshal(RealElement real) throws Exception {
        return new Real(RealType.getRealType(real.getType()), real.getValue());
    }

    @Override
    public RealElement marshal(Real real) throws Exception {
        return new RealElement(real);
    }

}
