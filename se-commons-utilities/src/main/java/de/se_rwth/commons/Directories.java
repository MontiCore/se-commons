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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

/**
 * Utility class with static methods to work with directories.
 *
 *
 */
public final class Directories {
  
  /**
   * Copys one file or directory into another. In case of directories, contents
   * are merged.
   *
   * @param source
   * @param destination
   * @throws IOException
   */
  public static final void copy(File source, File destination) throws IOException {
    
    checkNotNull(source);
    checkNotNull(destination);
    checkArgument(source.exists());

    if (source.isDirectory()) {
      copyDirectory(source, destination);
    }
    else {
      Files.copy(source, destination);
    }
  }
  
  /**
   * Deletes a file or a directory with all its contents.
   */
  public static void delete(File file) {
    
    checkNotNull(file);

    if (file.isDirectory()) {
      for (File subFile : file.listFiles()) {
        delete(subFile);
      }
    }
    
    file.delete();
    
  }
  
  /**
   * Lists all files recursivly located beneath the given directory that have
   * the given file extension.
   */
  public static Collection<File> listFilesRecursivly(File directory, String fileExtension) {
    
    checkArgument(directory.exists());

    ImmutableList.Builder<File> files = ImmutableList.builder();
    
    if (directory.isDirectory()) {
      for (File file : directory.listFiles()) {
        files.addAll(listFilesRecursivly(file, fileExtension));
      }
    }
    else {
      fileExtension = StringMatchers.DOT.trimLeadingFrom(fileExtension);
      String fileName = directory.getName();
      String actualFileExtension = Files.getFileExtension(fileName);
      if (actualFileExtension.equalsIgnoreCase(fileExtension)) {
        files.add(directory);
      }
    }
    
    return files.build();
  }
  
  /**
   * Copies a directory from source to destination.
   */
  private static final void copyDirectory(File source, File destination) throws IOException {
    
    assert source != null;
    assert destination != null;

    if (!source.isDirectory()) {
      throw new IllegalArgumentException(
          "Source (" + source.getPath() + ") must be a directory.");
    }
    
    if (!source.exists()) {
      throw new IllegalArgumentException(
          "Source directory (" + source.getPath() + ") doesn't exist.");
    }
    
    destination.mkdirs();
    File[] files = source.listFiles();
    
    for (File file : files) {
      if (file.isDirectory()) {
        copyDirectory(file, new File(destination, file.getName()));
      }
      else {
        Files.copy(file, new File(destination, file.getName()));
      }
    }
    
  }
  
  /**
   * Private constructor permitting manual instantiation.
   */
  private Directories() {
  }
  
}
