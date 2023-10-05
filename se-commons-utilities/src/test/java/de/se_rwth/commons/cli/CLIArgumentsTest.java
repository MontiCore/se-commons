/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.cli;

import com.google.common.collect.Iterables;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Couple of basic unit tests for {@link CLIArguments}.
 *
 */
public class CLIArgumentsTest {
  
  static final String grammar = "src/test/resources/Statechart.mc4";
  
  static final String modelPath = "src/test/resources";
  
  static final String template = "monticore.Main";
  
  static final String outputDirectory = "target";
  
  static final String[] args = { "-grammar", grammar,
      "-modelPath", modelPath,
      "-template", template,
      "-outputDirectory", outputDirectory,
      "-listArgument", "arg0", "arg1", "arg2",
      "-anotherList", "foo", "bar",
      "-switch" };
  
  @Test
  public void testCLIArguments() {
    Map<String, Iterable<String>> properties = CLIArguments.forArguments(args).asMap();
    
    assertNotNull(properties);
    assertNull(properties.get("foo"));
    
    assertTrue(properties.containsKey("switch"));
    assertNotNull(properties.get("switch"));
    
    assertTrue(Iterables.contains(properties.get("grammar"), grammar));
    assertTrue(Iterables.contains(properties.get("modelPath"), modelPath));
    assertTrue(Iterables.contains(properties.get("template"), template));
    assertTrue(Iterables.contains(properties.get("outputDirectory"), outputDirectory));
    
    assertEquals(3, Iterables.size(properties.get("listArgument")));
    assertEquals(2, Iterables.size(properties.get("anotherList")));
  }
  
}
