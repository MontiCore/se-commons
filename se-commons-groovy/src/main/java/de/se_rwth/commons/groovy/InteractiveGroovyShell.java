/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.groovy;

import jline.console.ConsoleReader;
import org.codehaus.groovy.GroovyException;

import java.io.IOException;
import java.io.PrintWriter;

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
