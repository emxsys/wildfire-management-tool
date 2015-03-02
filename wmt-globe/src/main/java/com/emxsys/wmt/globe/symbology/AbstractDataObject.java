/*
 * Copyright (c) 2012, Bruce Schubert <bruce@emxsys.com>
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
 *     - Neither the name of Bruce Schubert, Emxsys nor the names of its 
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
package com.emxsys.wmt.globe.symbology;

import java.util.concurrent.atomic.AtomicReference;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiFileLoader;
import org.openide.loaders.XMLDataObject;
import org.openide.nodes.Node;

/**
 * This base class provides common State based functionality for symbology DataObjects.
 *
 * @author Bruce Schubert
 */
public abstract class AbstractDataObject extends XMLDataObject {

    enum State {

        NEW, INITIALIZING, INITIALIZED, DELETING, DELETED, INVALID
    };
    protected final transient AtomicReference<State> init = new AtomicReference<>(State.NEW);
    protected final transient SaveCookie saveCookie = new SaveSupport();

    public AbstractDataObject(FileObject fo, MultiFileLoader loader) throws
            DataObjectExistsException {
        super(fo, loader);
        initialize();
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    /**
     * Delegates to DataObject.getLookup().lookup().
     */
    @Override
    public <T extends Node.Cookie> T getCookie(Class<T> cls) {
        return getLookup().lookup(cls);
    }

    /**
     * Called the Catalog's property change listener; adds/removes the Save cookie.
     *
     * @param modified the new state.
     */
    @Override
    public void setModified(boolean modified) {
        if (init.get() == State.INITIALIZED) {
            if (modified) {
                addSaveCookie();
            } else {
                removeSaveCookie();
            }
            super.setModified(modified);
        }
    }

    /**
     * Adds the MarkerSaveCapability cookie.
     */
    protected void addSaveCookie() {
        if (getLookup().lookup(SaveCookie.class) == null) {
            getCookieSet().add(this.saveCookie);
        }
    }

    /**
     * Removes the MarkerSaveCapability cookie.
     */
    protected void removeSaveCookie() {
        SaveCookie save = getLookup().lookup(SaveCookie.class);
        if (save != null) {
            getCookieSet().remove(save);
        }
    }

    /**
     *
     * @return true if the DataObject is in a state that supports deletion
     */
    protected boolean canDelete() {
        State state = init.get();
        return (state == State.INITIALIZED || state == State.INVALID);
    }

    protected final void initialize() {
        if (!init.compareAndSet(State.NEW, State.INITIALIZING)) {
            throw new IllegalStateException("Already initialized.");
        }
        readFile();
        init.compareAndSet(State.INITIALIZING, State.INITIALIZED);
    }

    /**
     * Reads the file and initializes the graphic from the XML contents.
     *
     */
    protected abstract void readFile();

    /**
     * Write the graphic to persistent storage.
     *
     */
    protected abstract void writeFile();

    /**
     * Save capability class.
     *
     * @author Bruce Schubert
     */
    class SaveSupport implements SaveCookie {

        @Override
        public void save() {
            writeFile();
        }
    }

}
