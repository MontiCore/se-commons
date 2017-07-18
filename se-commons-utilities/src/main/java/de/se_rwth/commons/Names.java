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

import static com.google.common.base.CharMatcher.WHITESPACE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.transform;
import static de.se_rwth.commons.StringMatchers.DOT;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

/**
 * Functions dealing with the composition and decomposition of names in models.
 * 
 * @author (last commit) $Author: nazari $
 * @version $Revision: 381 $, $Date: 2012-11-01 14:26:18 +0100 (Do, 01 Nov 2012)
 * $
 */
public final class Names {
  
  /**
   * @return The combination of the given name with the given list of parameters
   * in Java notation, e.g. a.b.c.Name<Parameter1,Parameter2>
   */
  public static final String getParameterizedTypeName(final String name,
      final Iterable<String> parameters) {
    checkNotNull(name);
    checkNotNull(parameters);
    checkArgument(name.length() > 0);
    
    return name + getParameterList(parameters);
  }
  
  /**
   * @return The combination of the given name with the given list of parameters
   * in Java notation, e.g. a.b.c.Name<Parameter1,Parameter2>
   */
  public static final String getParameterList(final Iterable<String> parameters) {
    
    checkNotNull(parameters);
    
    String parametersAsString = "";
    if (!Iterables.isEmpty(parameters)) {
      parametersAsString = '<' + Joiners.COMMA.join(parameters) + '>';
    }
    
    return parametersAsString;
  }
  
  /**
   * @return a string composed of the given parts (after trimming dots) and the
   * given simple name separated by dots.
   */
  public static final String getQualifiedName(Iterable<String> qualifierParts, String simpleName) {
    return getQualifiedName(getQualifiedName(qualifierParts), simpleName);
  }
  
  /**
   * @return a string composed of the given parts (after trimming dots)
   * separated by dots.
   */
  public static final String getQualifiedName(Iterable<String> parts) {
    return Joiners.DOT.join(
        transform(transform(parts,
            StringTransformations.TRIM_WHITESPACE),
            StringTransformations.TRIM_DOT));
  }
  
  /**
   * @return a string representing a Java type identifier composed of the given
   * type qualifier, type name and the list of type parameters.
   */
  public static final String getQualifiedParameterizedTypeName(Iterable<String> qualifierParts,
      String simpleName, List<String> typeParameters) {
    return getParameterizedTypeName(getQualifiedName(qualifierParts, simpleName), typeParameters);
  }
  
  /**
   * @return a string representing a Java type identifier composed of the given
   * type qualifier, type name and the list of type parameters.
   */
  public static final String getQualifiedParameterizedTypeName(String qualifier, String simpleName,
      List<String> typeParameters) {
    return getParameterizedTypeName(getQualifiedName(qualifier, simpleName), typeParameters);
  }
  
  /**
   * @return a string representing a Java type identifier composed of the given
   * type name parts and the list of type parameters.
   */
  public static final String getQualifiedParameterizedTypeName(Iterable<String> nameParts,
      List<String> typeParameters) {
    return getParameterizedTypeName(getQualifiedName(nameParts), typeParameters);
  }
  
  /**
   * Constructs a qualified name based on the given qualifier and simple name.
   * 
   * @param qualifier a {@link String} of the form "a.b.c". Leading or trailing
   * dots are removed.
   * @param simpleName a {@link String}. Leading or trailing dots are removed.
   */
  public static final String getQualifiedName(final String qualifier, final String simpleName) {
    
    checkNotNull(qualifier);
    checkNotNull(simpleName);
    checkArgument(!simpleName.isEmpty(), "The simple name must not be empty.");
    
    String trimedQualifier = DOT.trimFrom(WHITESPACE.trimFrom(qualifier));
    String trimedSimpleName = DOT.trimFrom(WHITESPACE.trimFrom(simpleName));
    
    return trimedQualifier.isEmpty()
        ? trimedSimpleName
        : trimedQualifier + "." + trimedSimpleName;
  }
  
  /**
   * @return The qualifier of the given qualified name (e.g. "a.b.c" from
   * "a.b.c.Name"). Returns an empty string if the name has no qualifier.
   * Leading or trailing dots are ignored and multiple dots are interpreted as a
   * single dot.
   */
  public static final String getQualifier(final String qualifiedName) {
    
    checkNotNull(qualifiedName);
    
    FluentIterable<String> parts = FluentIterable.from(
        Splitters.DOT.split(qualifiedName));
    if (parts.last().isPresent()) {
      // remove name
      if (!parts.last().get().equals(".")) {
        parts = parts.limit(parts.size() - 1);
      }
    }
    
    // ignore empty path-names, e.g. "....." is interpreted as "."
    // TODO actually this should be an IllegalArgumentException?!
    parts = parts.filter(new Predicate<String>() {
      @Override
      public boolean apply(String input) {
        return !input.trim().isEmpty();
      }
    });
    
    return Joiners.DOT.join(parts);
  }
  
  /**
   * @return The simple name of the given qualified name (e.g. "Name" from
   * "a.b.c.Name"). Leading or trailing dots are ignored.
   */
  public static final String getSimpleName(final String qualifiedName) {
    
    checkNotNull(qualifiedName);
    
    FluentIterable<String> parts = FluentIterable.from(
        Splitters.DOT.split(qualifiedName));
    
    return parts.last().get();
  }
  
  /**
   * @return The simple name of the given qualified name (e.g. the last list
   * element from the qualified name in the list form).
   */
  public static String getSimpleName(final List<String> qualifiedName) {
    
    checkNotNull(qualifiedName);
    
    String name = "";
    if (qualifiedName != null && !qualifiedName.isEmpty()) {
      name = qualifiedName.get(qualifiedName.size() - 1);
    }
    return name;
  }
  
  /**
   * @return a qualified name where the given number of parts are trimmed off
   * the end.
   */
  public static final String trimQualifiedName(String qualifiedName, int length) {
    return trimQualifiedName(Splitters.DOT.split(qualifiedName), length);
  }
  
  /**
   * @return a qualified name where the given number of parts are trimmed off
   * the end.
   */
  public static final String trimQualifiedName(Iterable<String> qualifiedName, int length) {
    return getQualifiedName(Iterables.limit(qualifiedName,
        Iterables.size(qualifiedName) - length + 1));
  }
  
  /**
   * Private constructor permitting instantiation is not useful (Instantiation
   * in templates is necessary.
   */
  public Names() {
  }
  
  /**
   * @return a file name build from the given file name and the given file
   * extension. Leading or trailing dots in the file extension will be ignored.
   */
  public static String getFileName(String fileName, String fileExtension) {
    checkArgument(!isNullOrEmpty(fileName));
    checkArgument(!isNullOrEmpty(fileExtension));
    return fileName + "." + DOT.trimFrom(WHITESPACE.trimFrom(fileExtension));
  }
  
  /**
   * Returns the path of a package for a full qualified name e.g. a.b.C -> a/b<br>
   * Note: This function is based on <b>File.separator</b> and leading and
   * trailing dots are ignored.
   * 
   * @param in String to transform
   * @return
   */
  public static String getPathFromQualifiedName(String in) {
    return getPathFromPackage(getQualifier(in));
  }
  
  /**
   * Returns the path of a package name, e.g.<br>
   * a.b.c -> a/b/c<br>
   * .a.b.c -> /a/b/c<br>
   * a.b.c. -> a/b/c/<br>
   * Note: This function is based on <b>File.separator</b>.
   * 
   * @param in String to transform
   * @return
   */
  public static String getPathFromPackage(String in) {
    return in.replaceAll("[.]", Matcher.quoteReplacement(File.separator)).intern();
  }
  
  /**
   * Returns the path of the given filename, e.g. my/path/to/filename.ext ->
   * my/path/to<br>
   * Note: This function is based on <b>File.separator</b>.
   * 
   * @param in filename
   * @return
   * @see #getPathFromFilename(String, String)
   */
  public static String getPathFromFilename(String in) {
    return getPathFromFilename(in, File.separator);
  }
  
  /**
   * Returns the path of the given filename, e.g. my/path/to/filename.ext ->
   * my/path/to<br>
   * 
   * @param in filename
   * @param separator used separator, e.g. "/" or File.separator
   * @see #getPathFromFilename(String)
   * @return
   */
  public static String getPathFromFilename(String in, String separator) {
    if (in == null) {
      return null;
    }
    int pos = in.lastIndexOf(separator);
    if (pos > 0) {
      return in.substring(0, pos).intern();
    }
    return "";
  }
  
  /**
   * Returns the package of the given path, e.g.<br>
   * my/path/ -> my.path.<br>
   * my/path -> my.path<br>
   * Note: This function is based on <b>File.separator</b>.
   * 
   * @param path
   * @return
   * @see #getPackageFromPath(String, String)
   */
  public static String getPackageFromPath(String path) {
    return getPackageFromPath(path, File.separator);
  }
  
  /**
   * Returns the package of the given path, e.g.<br>
   * my/path/ -> my.path.<br>
   * my/path -> my.path<br>
   * /my/path -> .my.path<br>
   * 
   * @param path
   * @param separator used separator, e.g. "/" or File.separator
   * @see #getPackageFromPath(String)
   * @return
   */
  public static String getPackageFromPath(String path, String separator) {
    return path.replaceAll("[" + Matcher.quoteReplacement(separator) + "]", ".").intern();
  }
  
}
