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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import visad.Real;
import visad.RealType;

/**
 * The RealXmlBinding class maps a Real to JAXB XmlElements for use by the RealXmlAdaptor, which
 * marshals/unmarshals a VisAD Real object to/from XML.
 *
 * @see RealXmlAdaptor
 * @author Bruce Schubert
 * @version $Id$
 */
@XmlType(propOrder = {"type", "value", "unit"})
public class RealXmlBinding {

    private String type;
    private double value;
    private String unit;

    public RealXmlBinding() {
        this(new Real(RealType.Generic));
    }

    public RealXmlBinding(Real real) {
        if (real == null) {
            real = new Real(RealType.Generic);
        }
        type = real.getType().toString();
        value = real.getValue();
        unit = real.getUnit().getIdentifier();
    }

    @XmlElement
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlElement
    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @XmlElement
    public String getUnit() {
        return this.unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
}
