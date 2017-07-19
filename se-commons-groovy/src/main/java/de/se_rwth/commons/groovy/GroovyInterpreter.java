/*
 * ******************************************************************************
 * MontiCore Language Workbench
 * Copyright (c) 2017, MontiCore, All rights reserved.
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
package de.se_rwth.commons.groovy;

import static com.google.common.base.Preconditions.checkNotNull;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.codehaus.groovy.GroovyException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.CharSource;
import com.google.common.io.Files;

/**
 * An interpreter for Groovy scripts.
 *
 */
public class GroovyInterpreter {
  
  public static final class Builder {
    
    private ClassLoader classLoader = GroovyInterpreter.class.getClassLoader();
    
    private final Binding binding = new Binding();
    
    private Class<? extends Script> scriptBaseClass = null;
    
    private ImportCustomizer importCustomizer = null;
    
    protected Builder() {
    }
    
    public Builder addVariable(String key, Object value) {
      this.binding.setVariable(checkNotNull(key), checkNotNull(value));
      return this;
    }
    
    public Builder addVariables(Map<String, Object> variables) {
      for (Map.Entry<String, Object> variable : checkNotNull(variables).entrySet()) {
        addVariable(variable.getKey(), variable.getValue());
      }
      return this;
    }
    
    public GroovyInterpreter build() {
      CompilerConfiguration configuration = new CompilerConfiguration();
      if (this.scriptBaseClass != null) {
        configuration.setScriptBaseClass(this.scriptBaseClass.getCanonicalName());
      }
      if (this.importCustomizer != null) {
        configuration.addCompilationCustomizers(importCustomizer);
      }
      return new GroovyInterpreter(this.classLoader, this.binding, configuration);
    }
    
    public Builder withClassLoader(ClassLoader classLoader) {
      this.classLoader = classLoader;
      return this;
    }
    
    public Builder withScriptBaseClass(Class<? extends Script> scriptBaseClass) {
      this.scriptBaseClass = scriptBaseClass;
      return this;
    }
    
    public Builder withImportCustomizer(ImportCustomizer importCustomizer) {
      this.importCustomizer = checkNotNull(importCustomizer);
      return this;
    }
    
  }
  
  /**
   * Factory method for {@link GroovyInterpreter}.
   */
  public static final Builder newInterpreter() {
    return new Builder();
  }
  
  private final GroovyShell shell;
  
  /**
   * Private constructor permitting manual instantiation.
   */
  private GroovyInterpreter(
      ClassLoader classLoader,
      Binding binding,
      CompilerConfiguration configuration) {
    this.shell = new GroovyShell(classLoader, binding, configuration);
  }
  
  /**
   * Runs the given script silently.
   */
  public void evaluate(CharSource scriptSource) {
    try {
      evaluate(scriptSource.read());
    }
    catch (IOException e) {
      Throwables.propagate(e);
    }
  }
  
  /**
   * Runs the given script silently.
   */
  public void evaluate(File scriptSource) {
    evaluate(Files.asCharSource(scriptSource, Charsets.UTF_8));
  }
  
  /**
   * Runs the given script silently.
   */
  public void evaluate(String scriptSource) {
    this.shell.evaluate(scriptSource);
  }
  
  /**
   * Runs the given script.
   *
   * @throws GroovyException
   */
  public void tryEvaluate(CharSource scriptSource) throws GroovyException {
    try {
      tryEvaluate(scriptSource.read());
    }
    catch (IOException e) {
      Throwables.propagate(e);
    }
  }
  
  /**
   * Runs the given script.
   *
   * @throws GroovyException
   */
  public void tryEvaluate(File scriptSource) throws GroovyException {
    tryEvaluate(Files.asCharSource(scriptSource, Charsets.UTF_8));
  }
  
  /**
   * Runs the given script.
   *
   * @throws GroovyException
   */
  public void tryEvaluate(String scriptSource) throws GroovyException {
    try {
      this.shell.evaluate(scriptSource);
    }
    catch (Exception e) {
      throw new GroovyException("Failed to evaluate groovy script.", e);
    }
  }
  
}
