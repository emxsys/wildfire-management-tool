/*
 * Copyright (c) 2014, Bruce Schubert. <bruce@emxsys.com>
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
 * - Neither the name of the Emxsys company nor the names of its 
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
package com.emxsys.wmt.core.ribbon;

import com.terramenta.ribbon.api.RibbonPreferences;
import com.terramenta.ribbon.spi.Office2013CompactRibbonPreferences;
import com.terramenta.ribbon.spi.Office2013RibbonPreferences;
import com.terramenta.ribbon.spi.RibbonPreferencesProvider;
import java.net.URL;
import org.openide.util.lookup.ServiceProvider;

/**
 * Service Provider for Ribbon preferences.
 * <p>
 * @author Bruce Schubert
 * @depeciated No longer needed. Terramenta has proper support for ribbons.
 */
//@ServiceProvider(service = com.terramenta.ribbon.spi.RibbonPreferencesProvider.class)
public class EmxsysRibbonPreferencesProvider extends RibbonPreferencesProvider {

    @Override
    public String getName() {
        return "Emxsys Custom UI";
    }

    @Override
    public String getDescription() {
        return "Customizations for the Office 2013 Compact Ribbon look and feel.";
    }

    @Override
    public URL getPreview() {
        return Office2013RibbonPreferences.class.getResource("/com/terramenta/ribbon/images/preview-office2013-compact.png");
    }

    private class EmxsysRibbonPreferences extends Office2013CompactRibbonPreferences {

    };

    private RibbonPreferences preferences;

    @Override
    public RibbonPreferences getPreferences() {
        if (preferences == null) {
            preferences = new EmxsysRibbonPreferences();
        }
        return preferences;
    }

}
