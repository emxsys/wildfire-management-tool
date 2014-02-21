/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emxsys.wmt.ribbon;

import com.terramenta.ribbon.api.RibbonPreferences;
import com.terramenta.ribbon.spi.Office2013RibbonPreferences;
import com.terramenta.ribbon.spi.RibbonPreferencesProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * Service Provider for Ribbon preferences.
 * <p>
 * @author Bruce Schubert
 * @version $Id$
 */
@ServiceProvider(service = com.terramenta.ribbon.spi.RibbonPreferencesProvider.class)
public class EmxsysRibbonPreferences extends RibbonPreferencesProvider
{

    private RibbonPreferences preferences;

    @Override
    public RibbonPreferences getPreferences()
    {
        if (preferences==null)
        {
            preferences = new Office2013RibbonPreferences();
        }
        return preferences;
    }

}
