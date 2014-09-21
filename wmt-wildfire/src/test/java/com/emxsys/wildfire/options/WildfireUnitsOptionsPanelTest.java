/*
 * Copyright (c) 2014, Bruce Schubert <bruce@emxsys.com>
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
 *     - Neither the name of Bruce Schubert,  nor the names of its 
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

package com.emxsys.wildfire.options;

import javax.swing.JOptionPane;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Bruce Schubert
 */
public class WildfireUnitsOptionsPanelTest {
    
    public WildfireUnitsOptionsPanelTest() {
    }

    /**
     * Interactive tests to evaluate layout and load/store behavior.
     * Comment out @Ignore to interactively evaluate the behavior of the panel.
     */
    @Ignore
    @Test
    public void testLoad() {
        System.out.println("Load and Store");
        WildfireUnitsOptionsPanel instance = new WildfireUnitsOptionsPanel(new WildfireUnitsOptionsPanelController());
        
        System.out.println("Default: Chains");
        instance.load();
        assertTrue(JOptionPane.showConfirmDialog(
                null,
                instance,
                "Validate Chains. Select MPH",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null) == JOptionPane.YES_OPTION);
        
        System.out.println("MPH");
        instance.store();
        instance.load();
        assertTrue(JOptionPane.showConfirmDialog(
                null,
                instance,
                "Validate MPH. Select KPH",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null) == JOptionPane.YES_OPTION);
        
        System.out.println("KPH");
        instance.store();
        instance.load();
        assertTrue(JOptionPane.showConfirmDialog(
                null,
                instance,
                "Validate KPH. Select MPS",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null) == JOptionPane.YES_OPTION);
        
        System.out.println("MPS");
        instance.store();
        instance.load();
        assertTrue(JOptionPane.showConfirmDialog(
                null,
                instance,
                "Validate MPS. Select Chains",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null) == JOptionPane.YES_OPTION);
        
        System.out.println("Chains");
        instance.store();
        instance.load();
        assertTrue(JOptionPane.showConfirmDialog(
                null,
                instance,
                "Validate Chains",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null) == JOptionPane.YES_OPTION);
    }
    
    
}
