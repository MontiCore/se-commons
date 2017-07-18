/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2015, MontiCore, All rights reserved.
 *
 * This project is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * ******************************************************************************
 */
package de.se_rwth.commons;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import com.google.common.base.Charsets;
import java.util.Optional;
import com.google.common.base.Throwables;
import com.google.common.io.Files;

/**
 * Provides build information about the running software containing this class..
 *
 * @author (last commit) $Author$
 * @version $Revision$, $Date$
 */
public final class BuildInfo {

  /**
   * This classes builder
   */
  public static class Writer {

    private File file;
    
    private Date date;
    
    private String version;

    public Writer toFile(File file) {
      this.file = checkNotNull(file);
      return this;
    }

    public void tryWrite() {
      try {
        write();
      }
      catch (FileNotFoundException e) {
        Throwables.propagate(e);
      }
      catch (IOException e) {
        Throwables.propagate(e);
      }
    }
    
    public Writer withVersion(String version) {
      this.version = checkNotNull(version);
      return this;
    }

    public void write() throws FileNotFoundException, IOException {
      this.file = firstNonNull(this.file, new File("buildinfo.properties"));
      Properties buildInfo = new Properties();
      buildInfo.put("date", firstNonNull(this.date, new Date()));
      buildInfo.put("version", firstNonNull(this.version, ""));
      buildInfo.store(Files.newWriter(this.file, Charsets.UTF_8), "BuildInfo");
    }
    
  }
  
  /**
   * Factory method for {@link BuildInfo}.
   */
  public static final Optional<BuildInfo> fromFile(File file) {

    Optional<BuildInfo> buildInfo;
    
    try {
      buildInfo = fromReader(Files.newReader(file, Charsets.UTF_8));
    }
    catch (FileNotFoundException e) {
      buildInfo = Optional.empty();
    }
    
    return buildInfo;
  }
  
  /**
   * Factory method for {@link BuildInfo}.
   */
  public static final Optional<BuildInfo> fromReader(Reader reader) {
    
    Optional<BuildInfo> buildInfo;
    
    try {

      Properties buildInfoFile = new Properties();
      buildInfoFile.load(reader);

      buildInfo = Optional.of(new BuildInfo(
          DateFormat.getInstance().parse(buildInfoFile.getProperty("date")),
          buildInfoFile.getProperty("version", "")));
      
    }
    catch (FileNotFoundException e) {
      buildInfo = Optional.empty();
    }
    catch (IOException e) {
      buildInfo = Optional.empty();
    }
    catch (ParseException e) {
      buildInfo = Optional.empty();
    }

    return buildInfo;
  }

  /**
   * Factory method for {@link BuildInfo}.
   */
  public static final Writer toFile(File file) {
    return new Writer().toFile(file);
  }
  
  private final Date date;
  
  private final String version;
  
  /**
   * Private constructor permitting manual instantiation.
   */
  private BuildInfo(Date date) {
    this(date, BuildInfo.class.getPackage().getImplementationVersion());
  }
  
  /**
   * Private constructor permitting manual instantiation.
   */
  private BuildInfo(Date date, String version) {
    this.date = checkNotNull(date);
    this.version = checkNotNull(version);
  }

  /**
   * @return the date of this build.
   */
  public Date getDate() {
    return this.date;
  }

  /**
   * @return the version of this build.
   */
  public String getVersion() {
    return this.version;
  }
  
}
