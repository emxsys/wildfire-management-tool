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
package com.emxsys.wmt.util;

import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import javax.swing.ButtonModel;

/**
 * A utility class used to handle Event based tasks.
 *
 * @author Bruce Schubert <bruce@emxsys.com>
 */
public class EventUtil {

    private static final Logger logger = Logger.getLogger(EventUtil.class.getName());

    private EventUtil() {
    }

    /**
     * Retrieves the ButtonModel from the event. Gets the ActionModel derivative from Flamingo
     * components.
     *
     * @param event typically, an event passed into Action.actionPerformed method.
     * @return the ButtonModel from the event source, which may be null
     */
    @SuppressWarnings("unchecked")
    static public ButtonModel getButtonModel(ActionEvent event) {
        if (event.getSource() instanceof ButtonModel) {
            // Native buttons
            return (ButtonModel) event.getSource();
        }
        else {
            try {
                // Use reflection to look for a Flamingo ActionModel, a ButtonModel derivative, 
                // like that embedded in a JCommandToggleButton
                Class<?> clazz = event.getSource().getClass();
                Class<?>[] args
                        = {};
                Method method = clazz.getMethod("getActionModel", args);

                Object[] params
                        = {};
                return (ButtonModel) method.invoke(event.getSource(), params);
            }
            catch (Exception exception) {
                // handle NoSuchMethod and Security excetions
                logger.severe(exception.getMessage());
            }
            return null;
        }
    }
}
