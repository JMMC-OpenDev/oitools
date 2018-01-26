/* 
 * Copyright (C) 2018 CNRS - JMMC project ( http://www.jmmc.fr )
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Object created to store file information (path / file name)
 * @author kempsc
 */
public final class FileRef {

    /** global memory counter */
    private final static AtomicInteger GLOBAL_COUNTER = new AtomicInteger(1);

    /* members */
    /** file name */
    private String fileName;
    /** absolute file path */
    private String absoluteFilePath;
    /** memory counter (-1 by default) */
    private int memCount = -1;

    /**
     * Constructor
     * @param absoluteFilePath
     */
    public FileRef(final String absoluteFilePath) {
        setAbsoluteFilePath(absoluteFilePath);
    }

    /**
     * Returns hash code for this FileRef instance = System#identityHashCode
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Indicates whether some other object is "equal to" this one = Identity.
     * @return true if this object is the same; false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /*
     * Getter - Setter -----------------------------------------------------------
     */
    /**
     * Get the name of this FitsImageFile file.
     *  @return a string containing the name of the FitsImageFile file.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Return the absolute file path
     * @return absolute file path or null if the file does not exist
     */
    public String getAbsoluteFilePath() {
        return absoluteFilePath;
    }

    /**
     * Define the absolute file path
     * @param absoluteFilePath absolute file path
     */
    public void setAbsoluteFilePath(final String absoluteFilePath) {
        this.absoluteFilePath = absoluteFilePath;
        this.fileName = absoluteFilePath;
        if (absoluteFilePath != null && !absoluteFilePath.isEmpty()) {
            final int pos = absoluteFilePath.lastIndexOf(File.separatorChar);
            if (pos != -1) {
                this.fileName = absoluteFilePath.substring(pos + 1);
            }
        }
    }

    public int getMemoryIndex() {
        if (memCount == -1) {
            memCount = GLOBAL_COUNTER.getAndIncrement();
        }
        return memCount;
    }

    /** 
     * Return a short description of FitsImageFile content.
     * @return short description of FitsImageFile content
     */
    @Override
    public String toString() {
        return "FileRef[" + getStringId() + "]";
    }

    public String getStringId() {
        if (getFileName() != null) {
            return getFileName();
        }
        return "memory:" + getMemoryIndex();
    }

    /**
     * Returns a XML representation of this class
     * @param sb
     * @return a XML representation of this class
     */
    public StringBuilder toXML(final StringBuilder sb) {

        if (getAbsoluteFilePath() != null) {
            sb.append("    <file>").append(getAbsoluteFilePath()).append("</file>\n");
        } else {
            sb.append("    <file>").append("memory:").append(getMemoryIndex()).append("</file>\n");
        }

        return sb;
    }

}
