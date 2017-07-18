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
package de.se_rwth.commons.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Iterables;

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
