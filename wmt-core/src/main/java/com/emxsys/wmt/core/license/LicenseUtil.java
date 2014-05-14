/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package com.emxsys.wmt.core.license;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/**
 * Provides utility methods
 *
 * @author Marek Slama
 */
public class LicenseUtil {

    /**
     * Creates a new instance of Utilities
     */
    private LicenseUtil() {
    }

    /**
     * Tries to set default L&F according to platform. Uses: Metal L&F on Linux and Solaris Windows
     * L&F on Windows Aqua L&F on Mac OS X System L&F on other OS
     */
    public static void setDefaultLookAndFeel() {
        String uiClassName;
        if (Utilities.isWindows()) {
            uiClassName = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"; //NOI18N
        }
        else if (Utilities.isMac()) {
            uiClassName = "apple.laf.AquaLookAndFeel"; //NOI18N
        }
        else if (Utilities.isUnix()) {
            uiClassName = "javax.swing.plaf.metal.MetalLookAndFeel"; //NOI18N
        }
        else {
            uiClassName = UIManager.getSystemLookAndFeelClassName();
        }
        if (uiClassName.equals(UIManager.getLookAndFeel().getClass().getName())) {
            //Desired L&F is already set
            return;
        }
        try {
            UIManager.setLookAndFeel(uiClassName);
        }
        catch (Exception ex) {
            System.err.println("Cannot set L&F " + uiClassName); //NOI18N
            System.err.println("Exception:" + ex.getMessage()); //NOI18N
        }
    }

    /**
     * On JDK 1.6 it creates dialog without owner and modality type APPLICATION_MODAL. On JDK 1.5
     * creates standard modal dialog. When JDK5 is obsolete, use just new JDialog(null, title,
     * Dialog.ModalityType.APPLICATION_MODAL).
     */
    public static JDialog createModalDialog(String title) {
        try {
//            Class clazz = Class.forName("java.awt.Dialog$ModalityType");  //NOI18N
//            Method methodValues = clazz.getMethod("valueOf", new Class[]{String.class});  //NOI18N
//            Object modalityType = methodValues.invoke(null, new Object[]{"APPLICATION_MODAL"});  //NOI18N
//            Constructor c = JDialog.class.getConstructor(new Class[]{Window.class, String.class, modalityType.getClass()});
//            return (JDialog) c.newInstance(new Object[]{(Window) null, title, modalityType});
            return new JDialog(null, title, JDialog.ModalityType.APPLICATION_MODAL);
        }
        catch (Exception e) {
            // fallback on JDK5
            return new JDialog((Frame) null, title, true);
        }
    }

    /**
     * #154031 - set NetBeans icons for license dialog. It works only with JDK1.6. When JDK5 is
     * obsolete, use just dialog.setIconImages(images).
     */
    public static void initIcons(JDialog dialog) {
        Method m = null;
        try {
            m = dialog.getClass().getMethod("setIconImages", new Class<?>[]{
                List.class
            });  //NOI18N
        }
        catch (NoSuchMethodException ex) {
            //Method not available so we are on JDK 5 => give up
            return;
        }
        if (m != null) {
            List<Image> images = new ArrayList<Image>();
            images.add(ImageUtilities.loadImage("org/netbeans/core/startup/frame.gif", true));  //NOI18N
            images.add(ImageUtilities.loadImage("org/netbeans/core/startup/frame32.gif", true));  //NOI18N
            images.add(ImageUtilities.loadImage("org/netbeans/core/startup/frame48.gif", true));  //NOI18N
            try {
                m.invoke(dialog, new Object[]{
                    images
                });
            }
            catch (IllegalAccessException ex) {
                // ignore
            }
            catch (InvocationTargetException ex) {
                // ignore
            }
        }
    }

    /**
     * #154030 - Creates JDialog around JOptionPane. The body is copied from
     * JOptionPane.createDialog because we need APPLICATION_MODAL type of dialog on JDK6.
     */
    public static JDialog createJOptionDialog(final JOptionPane pane, String title) {
        final JDialog dialog = LicenseUtil.createModalDialog(title);
        LicenseUtil.initIcons(dialog);
        Container contentPane = dialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(pane, BorderLayout.CENTER);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        WindowAdapter adapter = new WindowAdapter() {
            private boolean gotFocus = false;

            @Override
            public void windowClosing(WindowEvent we) {
                pane.setValue(null);
            }

            @Override
            public void windowGainedFocus(WindowEvent we) {
                // Once window gets focus, set initial focus
                if (!gotFocus) {
                    pane.selectInitialValue();
                    gotFocus = true;
                }
            }
        };
        dialog.addWindowListener(adapter);
        dialog.addWindowFocusListener(adapter);
        dialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent ce) {
                // reset value to ensure closing works properly
                pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
            }
        });
        pane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                // Let the defaultCloseOperation handle the closing
                // if the user closed the window without selecting a button
                // (newValue = null in that case).  Otherwise, close the dialog.
                if (dialog.isVisible() && event.getSource() == pane
                        && (event.getPropertyName().equals(JOptionPane.VALUE_PROPERTY))
                        && event.getNewValue() != null
                        && event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                    dialog.setVisible(false);
                }
            }
        });
        return dialog;
    }
}
