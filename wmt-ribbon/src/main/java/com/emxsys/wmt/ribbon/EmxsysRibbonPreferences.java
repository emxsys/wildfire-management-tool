/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.emxsys.wmt.ribbon;

import com.terramenta.ribbon.spi.RibbonPreferencesProvider;
import java.awt.Dimension;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Bruce Schubert
 * @version $Id$
 */
@ServiceProvider(service = com.terramenta.ribbon.spi.RibbonPreferencesProvider.class)
public class EmxsysRibbonPreferences extends RibbonPreferencesProvider
{

    @Override
    public Object[] getLafClassDefaults()
    {
        return new Object[]
        {
            "RibbonApplicationMenuButtonUI", "com.terramenta.ribbon.FileRibbonApplicationMenuButtonUI",
            "RibbonUI", "com.terramenta.ribbon.FileRibbonUI",
        };
    }

    @Override
    public Dimension getPreferredBandSize()
    {
        return new Dimension(40, 90);
    }

    @Override
    public boolean getUsePopupMenus()
    {
        return false;
    }

    @Override
    public boolean getUseTabNameForTasksBand()
    {
        return false;
    }

    @Override
    public boolean getAlwaysDisplayButtonText()
    {
        return true;
    }
}
