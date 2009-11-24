/*
 * TarOutputArchive.java
 *
 * Created on 28. Februar 2006, 20:17
 */
/*
 * Copyright (C) 2006-2007 Schlichtherle IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.schlichtherle.io.archive.tar;

import de.schlichtherle.io.*;
import de.schlichtherle.io.archive.spi.*;
import de.schlichtherle.io.util.*;

import java.io.*;
import java.util.*;

import org.apache.tools.tar.*;

/**
 * An implementation of {@link OutputArchive} to write TAR archives.
 * <p>
 * Because the TAR file format needs to know each entry's length in advance,
 * entries from an unknown source (such as entries created with
 * {@link FileOutputStream}) are actually written to temp files and copied
 * to the underlying <code>TarOutputStream</code> upon a call to their
 * {@link OutputStream#close} method.
 * Note that this implies that the <code>close()</code> method may fail with
 * an {@link IOException}.
 * <p>
 * Entries which's size is known in advance (such as entries copied with one
 * of the <code>(archiveC|c)opy(All)?(From|To)</code> methods in the
 * {@link File} class) are directly written to the underlying
 * <code>TarOutputStream</code> instead.
 * <p>
 * This output archive can only write one entry at a time.
 * Archive drivers may wrap this class in a {@link MultiplexedOutputArchive}
 * to overcome this limitation.
 * 
 * @author Christian Schlichtherle
 * @version @version@
 * @since TrueZIP 6.0
 */
public class TarOutputArchive
        extends TarOutputStream
        implements OutputArchive {

    /** Prefix for temporary files created by the multiplexer. */
    private static final String TEMP_FILE_PREFIX = TarDriver.TEMP_FILE_PREFIX;

    /** Maps entry names to tar entries [String -> TarEntry]. */
    private final Map entries = new LinkedHashMap();

    private OutputArchiveMetaData metaData;
    private boolean busy;

    public TarOutputArchive(OutputStream out) {
        super(out);
        super.setLongFileMode(LONGFILE_GNU);
    }

    public int getNumArchiveEntries() {
        return entries.size();
    }

    public Enumeration getArchiveEntries() {
        return Collections.enumeration(entries.values());
    }

    public ArchiveEntry getArchiveEntry(String entryName) {
        return (TarEntry) entries.get(entryName);
    }

    public OutputStream getOutputStream(
            final ArchiveEntry entry,
            final ArchiveEntry srcEntry)
    throws IOException {
        if (isBusy())
            throw new OutputArchiveBusyException(entry);

        final TarEntry tarEntry = (TarEntry) entry;

        if (tarEntry.isDirectory()) {
            tarEntry.setSize(0);
            return new EntryOutputStream(tarEntry);
        }

        if (srcEntry != null) {
            tarEntry.setSize(srcEntry.getSize());
            return new EntryOutputStream(tarEntry);
        }

        // The source entry does not exist or cannot support DDC
        // to the destination entry.
        // So we need to buffer the output in a temporary file and write
        // it upon close().
        final java.io.File temp = Temps.createTempFile(TEMP_FILE_PREFIX);
        return new TempEntryOutputStream(tarEntry, temp);
    }

    /**
     * Returns whether this output archive is busy writing an archive entry
     * or not.
     */
    private final boolean isBusy() {
        return busy;
    }

    /**
     * This entry output stream writes directly to our subclass.
     * It can only be used if this output stream is not currently busy
     * writing another entry and the entry holds enough information to
     * write the entry header.
     * These preconditions are checked by {@link #getOutputStream}.
     */
    private class EntryOutputStream extends FilterOutputStream {
        private boolean closed;

        private EntryOutputStream(final TarEntry entry)
        throws IOException {
            super(TarOutputArchive.this);
            putNextEntry(entry);
            entries.put(entry.getName(), entry);
            busy = true;
        }

        public void write(byte[] b) throws IOException {
            out.write(b, 0, b.length);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
        }

        public void close() throws IOException {
            if (closed)
                return;

            // Order is important here!
            closed = true;
            busy = false;
            closeEntry();
        }
    } // class EntryOutputStream

    /**
     * This entry output stream writes the entry to a temporary file.
     * When the stream is closed, the temporary file is then copied to this
     * output stream and finally deleted.
     */
    private class TempEntryOutputStream extends java.io.FileOutputStream {
        private final TarEntry entry;
        private final java.io.File temp;
        private boolean closed;

        public TempEntryOutputStream(
                final TarEntry entry,
                final java.io.File temp)
        throws IOException {
            super(temp);
            this.entry = entry;
            this.temp = temp;
            entries.put(entry.getName(), entry);
            busy = true;
        }

        public void close() throws IOException {
            if (closed)
                return;

            // Order is important here!
            closed = true;
            busy = false;
            try {
                super.close();
            } finally {
                entry.setSize(temp.length());
                storeTempEntry(entry, temp);
            }
        }
    } // class TempEntryOutputStream

    private void storeTempEntry(
            final TarEntry entry,
            final java.io.File temp)
    throws IOException {
        try {
            final InputStream in = new java.io.FileInputStream(temp);
            try {
                putNextEntry(entry);
                try {
                    de.schlichtherle.io.File.cat(in, this);
                } finally {
                    closeEntry();
                }
            } finally {
                in.close();
            }
        } finally {
            if (!temp.delete()) // may fail on Windoze if in.close() failed!
                temp.deleteOnExit(); // we're bullish never to leavy any temps!
        }
    }

    /**
     * @deprecated This method will be removed in the next major version number
     *             release and should be implemented as
     *             <code>getOutputStream(entry, null).close()</code>.
     */
    public final void storeDirectory(ArchiveEntry entry)
    throws IOException {
        assert false : "Since TrueZIP 6.5, this is not used anymore!";
        if (!entry.isDirectory())
            throw new IllegalArgumentException();
        getOutputStream(entry, null).close();
    }

    //
    // Metadata stuff.
    //

    public OutputArchiveMetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(final OutputArchiveMetaData metaData) {
        this.metaData = metaData;
    }
}
