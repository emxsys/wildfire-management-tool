/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package com.emxsys.wmt.core.welcome.ui;

import com.emxsys.wmt.core.welcome.WelcomeOptions;
import com.emxsys.wmt.core.welcome.content.BundleSupport;
import com.emxsys.wmt.core.welcome.content.Constants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 *
 * @author S. Aubrecht
 */
class ShowNextTime extends JPanel
        implements ActionListener, Constants, PropertyChangeListener {

    private JCheckBox button;

    /** Creates a new instance of RecentProjects */
    public ShowNextTime() {
        super( new BorderLayout() );
        setOpaque(false);

        button = new JCheckBox( BundleSupport.getLabel( "ShowOnStartup" ) ); // NOI18N
        button.setSelected( WelcomeOptions.getDefault().isShowOnStartup() );
        button.setOpaque( false );
        button.setForeground( Color.white );
        button.setHorizontalTextPosition( SwingConstants.LEFT );
        BundleSupport.setAccessibilityProperties( button, "ShowOnStartup" ); //NOI18N
        add( button, BorderLayout.CENTER );
        button.addActionListener( this );
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        WelcomeOptions.getDefault().setShowOnStartup( button.isSelected() );
    }

    @Override
    public void addNotify() {
        super.addNotify();
        WelcomeOptions.getDefault().addPropertyChangeListener( this );
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        WelcomeOptions.getDefault().removePropertyChangeListener( this );
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        button.setSelected( WelcomeOptions.getDefault().isShowOnStartup() );
    }
    
    
}
