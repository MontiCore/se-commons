/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
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

import java.io.IOException;
import java.io.PrintWriter;

import jline.console.ConsoleReader;

import org.codehaus.groovy.GroovyException;

/**
 * An interactive shell that delegates commands to a {@link GroovyInterpreter}.
 *
 */
public class InteractiveGroovyShell {
  
  public static void main(String[] arguments) throws IOException {
    InteractiveGroovyShell.forInterpreter(GroovyInterpreter.newInterpreter().build()).tryRun();
  }
  
  /**
   * Constructor for de.se_rwth.commons.groovy.InteractiveGroovyShell
   */
  public InteractiveGroovyShell(GroovyInterpreter groovyInterpreter) {
    this.groovy = groovyInterpreter;
  }
  
  /**
   * Factory method for {@link InteractiveGroovyShell}.
   */
  public static final InteractiveGroovyShell forInterpreter(GroovyInterpreter groovyInterpreter) {
    return new InteractiveGroovyShell(groovyInterpreter);
  }
  
  private static final String PROMPT = "mc: ";
  
  private final GroovyInterpreter groovy;
  
  public void tryRun() throws IOException {
    
    ConsoleReader reader = new ConsoleReader();
    PrintWriter writer = new PrintWriter(reader.getOutput());
    
    reader.setPrompt(PROMPT);
    
    String line;
    while ((line = reader.readLine()) != null) {
      try {
        groovy.tryEvaluate(line);
      }
      catch (GroovyException e) {
        writer.append("[ERROR] Could not parse command.");
        writer.flush();
      }
    }
    
  }
  
}
