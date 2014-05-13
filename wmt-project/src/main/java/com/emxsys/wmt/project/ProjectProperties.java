/*
 * Copyright (c) 2012, Bruce Schubert. <bruce@emxsys.com>
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
package com.emxsys.wmt.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.netbeans.spi.project.ProjectState;

/**
 * This class sets the project's ProjectState to "dirty" whenever a property is modified.
 */
public class ProjectProperties extends Properties {

    private final ProjectState state;
    private boolean isLoading = false;

    /**
     * This constructor associates a supplied ProjectState object with the instance.
     *
     * @param state will be set to "modified" when a property is updated.
     * @see ProjectState
     */
    ProjectProperties(ProjectState state) {
        this.state = state;
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        this.isLoading = true;
        super.load(inStream);
        this.isLoading = false;
    }

    /**
     * Updates the key with a new value and sets the state to modified if the value changed.
     *
     * @param key the hashtable key
     * @param value the new value
     * @return the previous value
     */
    @Override
    public Object put(Object key, Object value) {
        Object result = super.put(key, value);
        if (isPutSuccessful(result, value)) {
            if (!isLoading) {
                this.state.markModified();
            }
        }
        return result;
    }

    private boolean isPutSuccessful(Object result, Object val) {
        return ((result == null) != (val == null)) || 
                (result != null && val != null && !val.equals(result));
    }
}
