/*
 * Copyright (c) 2009-2012, Bruce Schubert. <bruce@emxsys.com>
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
 * - Neither the name of Bruce Schubert, Emxsys nor the names of its 
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
package com.emxsys.util;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 * @version $Id: ScreenCaptureUtil.java 209 2012-09-05 23:09:19Z bdschubert $
 */
public class ScreenCaptureUtil {

    public final static String BASENAME = "screenshot";
    public final static String FORMAT = "png";
    public final static String SUFFIX = "." + FORMAT;

    private ScreenCaptureUtil() {
    }

    public static void captureToFile(Rectangle bounds) {

        try {
            Robot robot = new Robot();
            BufferedImage image = robot.createScreenCapture(bounds);

            File saveFile = chooseFile(WindowManager.getDefault().getMainWindow());
            if (saveFile != null) {
                try {
                    ImageIO.write(image, FORMAT, saveFile);
                }
                catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        }
        catch (AWTException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    private static File chooseFile(Component parentFrame) {
        File outFile = null;
        JFileChooser fileChooser = new JFileChooser();
        try {
            while (true) {
                fileChooser.setDialogTitle("Save Screenshot");
                fileChooser.setSelectedFile(new File(composeSuggestedName(fileChooser.getCurrentDirectory())));

                int status = fileChooser.showSaveDialog(parentFrame);
                if (status != JFileChooser.APPROVE_OPTION) {
                    return null;
                }

                outFile = fileChooser.getSelectedFile();
                if (outFile == null) // Shouldn't happen, but include a reaction just in case
                {
                    JOptionPane.showMessageDialog(parentFrame, "Please select a location for the image file.",
                            "No Location Selected", JOptionPane.ERROR_MESSAGE);
                    continue;
                }

                if (!outFile.getPath().endsWith(SUFFIX)) {
                    outFile = new File(outFile.getPath() + SUFFIX);
                }

                if (outFile.exists()) {
                    status = JOptionPane.showConfirmDialog(parentFrame,
                            "Replace existing file\n" + outFile.getName() + "?",
                            "Overwrite Existing File?", JOptionPane.YES_NO_CANCEL_OPTION);
                    if (status == JOptionPane.NO_OPTION) {
                        continue;
                    }
                    if (status != JOptionPane.YES_OPTION) {
                        return null;
                    }
                }
                break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return outFile;
    }

    private static String composeSuggestedName(File directory) {

        File candidate = new File(directory.getPath() + File.separatorChar + BASENAME + SUFFIX);
        for (int i = 1; candidate.exists(); i++) {
            String sequence = String.format("%03d", i);
            candidate = new File(directory.getPath() + File.separatorChar + BASENAME + sequence + SUFFIX);
        }
        return candidate.getPath();
    }
}
