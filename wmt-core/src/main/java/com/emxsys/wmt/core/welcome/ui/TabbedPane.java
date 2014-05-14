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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
import com.emxsys.wmt.core.welcome.content.Constants;
import com.emxsys.wmt.core.welcome.content.Logo;
import com.emxsys.wmt.core.welcome.content.Utils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import org.openide.util.ImageUtilities;


/**
 *
 * @author S. Aubrecht
 */
class TabbedPane extends JPanel implements Constants
{// , Scrollable {
    private final JComponent[] tabs;
    private final TabButton[] buttons;
    private final JComponent tabHeader;
    private final JPanel tabContent;
    private boolean[] tabAdded;
    private int selTabIndex = -1;


    public TabbedPane(JComponent... tabs)
    {
        super(new BorderLayout());

        setOpaque(false);

        this.tabs = tabs;
        tabAdded = new boolean[tabs.length];
        Arrays.fill(tabAdded, false);

        // vlv: print
        for (JComponent c : tabs)
        {
            c.putClientProperty("print.printable", Boolean.TRUE); // NOI18N
            c.putClientProperty("print.name", c.getName()); // NOI18N
        }

        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TabButton btn = (TabButton) e.getSource();
                switchTab(btn.getTabIndex());
                WelcomeOptions.getDefault().setLastActiveTab(btn.getTabIndex());
            }
        };

        buttons = new TabButton[tabs.length];
        for (int i = 0; i < buttons.length; i++)
        {
            buttons[i] = new TabButton(tabs[i].getName(), i);
            buttons[i].addActionListener(al);
        }


        tabHeader = new TabHeader(buttons);
        add(tabHeader, BorderLayout.NORTH);

        tabContent = new TabContentPane();//JPanel( new GridBagLayout() );
//        tabContent.setOpaque(false);
//        tabContent.setBorder(new ContentBorder());

        add(tabContent, BorderLayout.CENTER);
        int activeTabIndex = WelcomeOptions.getDefault().getLastActiveTab();
        if (WelcomeOptions.getDefault().isSecondStart() && activeTabIndex < 0)
        {
            activeTabIndex = 1;
            WelcomeOptions.getDefault().setLastActiveTab(1);
        }
        activeTabIndex = Math.max(0, activeTabIndex);
        activeTabIndex = Math.min(activeTabIndex, tabs.length - 1);
//        buttons[activeTabIndex].setSelected(true);
        switchTab(activeTabIndex);
    }


    private void switchTab(int tabIndex)
    {
        if (!tabAdded[tabIndex])
        {
            tabContent.add(tabs[tabIndex], new GridBagConstraints(tabIndex, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0)); //NOI18N
            tabAdded[tabIndex] = true;
        }
        if (selTabIndex >= 0)
        {
            buttons[selTabIndex].setSelected(false);
        }
        JComponent compToShow = tabs[tabIndex];
        JComponent compToHide = selTabIndex >= 0 ? tabs[selTabIndex] : null;
        selTabIndex = tabIndex;
        buttons[selTabIndex].setSelected(true);

        if (null != compToHide)
        {
            compToHide.setVisible(false);
        }

        compToShow.setVisible(true);
        compToShow.requestFocusInWindow();
    }


    @Override
    public Dimension getPreferredSize()
    {
        Dimension d = super.getPreferredSize();
        if (null != getParent() && null != getParent().getParent())
        {
            Component scroll = getParent().getParent();
            if (scroll.getWidth() > 0)
            {
                if (d.width > scroll.getWidth())
                {
                    d.width = Math.max(scroll.getWidth(), START_PAGE_MIN_WIDTH + (int) (((FONT_SIZE - 11) / 11.0) * START_PAGE_MIN_WIDTH));
                }
                else if (d.width < scroll.getWidth())
                {
                    d.width = scroll.getWidth();
                }
            }
        }
        d.width = Math.min(d.width, 1000);
        return d;
    }
    private final static Color colBackground = Utils.getColor(COLOR_TAB_BACKGROUND);
    private static final Image imgSelected = ImageUtilities.loadImage(IMAGE_TAB_SELECTED, true);
    private static final Image imgRollover = ImageUtilities.loadImage(IMAGE_TAB_ROLLOVER, true);


    private static class TabButton extends JPanel
    {
        private boolean isSelected = false;
        private ActionListener actionListener;
        private final int tabIndex;
        private final JLabel lblTitle = new JLabel();
        private boolean isMouseOver = false;


        public TabButton(String title, int tabIndex)
        {
            super(new BorderLayout());
            lblTitle.setText(title);
            add(lblTitle, BorderLayout.CENTER);
            this.tabIndex = tabIndex;
            setOpaque(true);
            lblTitle.setFont(TAB_FONT);
            lblTitle.setForeground(Color.white);
            lblTitle.setHorizontalAlignment(JLabel.CENTER);
            setFocusable(true);
            setBackground(colBackground);

            addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyPressed(KeyEvent e)
                {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER)
                    {
//                        setSelected( !isSelected );
                        if (null != actionListener)
                        {
                            actionListener.actionPerformed(new ActionEvent(TabButton.this, 0, "clicked"));
                        }
                    }
                }
            });

            addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
//                    setSelected( !isSelected );
                    if (null != actionListener)
                    {
                        actionListener.actionPerformed(new ActionEvent(TabButton.this, 0, "clicked"));
                    }
                }


                @Override
                public void mouseEntered(MouseEvent e)
                {
                    if (!isSelected)
                    {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }
                    else
                    {
                        setCursor(Cursor.getDefaultCursor());
                    }
                    isMouseOver = true;
                    repaint();
                }


                @Override
                public void mouseExited(MouseEvent e)
                {
                    setCursor(Cursor.getDefaultCursor());
                    isMouseOver = false;
                    repaint();
                }
            });

            addFocusListener(new FocusListener()
            {
                @Override
                public void focusGained(FocusEvent e)
                {
                    isMouseOver = true;
                    repaint();
                }


                @Override
                public void focusLost(FocusEvent e)
                {
                    isMouseOver = false;
                    repaint();
                }
            });
        }


        @Override
        protected void paintComponent(Graphics g)
        {
            Graphics2D g2d = (Graphics2D) g;
            if (isSelected)
            {
                g2d.drawImage(imgSelected, 0, 0, getWidth(), getHeight(), this);
            }
            else if (isMouseOver || isFocusOwner() || lblTitle.isFocusOwner())
            {
                g2d.drawImage(imgRollover, 0, 0, getWidth(), getHeight(), this);
            }
            else
            {
                super.paintComponent(g);
            }
        }


        public void addActionListener(ActionListener l)
        {
            assert null == actionListener;
            this.actionListener = l;
        }


        public void setSelected(boolean sel)
        {
            this.isSelected = sel;

            setFocusable(!sel);
            repaint();
        }


        public int getTabIndex()
        {
            return tabIndex;
        }
    }


    private class TabHeader extends JPanel
    {
        private final ShowNextTime showNextTime = new ShowNextTime();


        public TabHeader(TabButton... buttons)
        {
            super(new GridBagLayout());
            setOpaque(false);
            JPanel panelButtons = new JPanel(new GridLayout(1, 0));
            panelButtons.setOpaque(false);
            for (int i = 0; i < buttons.length; i++)
            {
                TabButton btn = buttons[i];
                btn.setBorder(new TabBorder(i == buttons.length - 1));
                panelButtons.add(btn);
            }

            add(Logo.createNetBeansLogo(), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 12, 5, 12), 0, 0));
            add(new JLabel(), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            add(panelButtons, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
            add(new JLabel(), new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
            add(showNextTime, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(15, 12, 15, 12), 0, 0));
        }


        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setPaint(colBackground);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        }


        @Override
        public void addNotify()
        {
            super.addNotify();
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    showNextTime.requestFocusInWindow();
                }
            });
        }
    }
    private static final Color COL__BORDER1 = Utils.getColor(COLOR_TAB_BORDER1);
    private static final Color COL__BORDER2 = Utils.getColor(COLOR_TAB_BORDER2);


    private static final class TabBorder implements Border
    {
        private final boolean isLastButton;


        public TabBorder(boolean isLastButton)
        {
            this.isLastButton = isLastButton;
        }


        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
        {
            g.setColor(COL__BORDER2);
            g.drawRect(x, y, width - 1, height - 1);
            g.setColor(COL__BORDER1);
            g.drawLine(x, y, x, height);
            if (isLastButton)
            {
                g.drawLine(width - 1, y, width - 1, height);
            }
        }


        @Override
        public Insets getBorderInsets(Component c)
        {
            return new Insets(16, 16, 12, 16);
        }


        @Override
        public boolean isBorderOpaque()
        {
            return false;
        }
    }
}
