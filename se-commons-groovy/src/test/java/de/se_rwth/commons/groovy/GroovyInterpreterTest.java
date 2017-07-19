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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.groovy.GroovyException;
import org.junit.Test;

/**
 * Test for {@link GroovyInterpreter}.
 *
 */
public class GroovyInterpreterTest {
  
  @Test
  public void test__singleEvaluationWithVariableInteraction() {
    
    AtomicInteger i = new AtomicInteger();
    GroovyInterpreter groovy = GroovyInterpreter.newInterpreter()
        .addVariable("i", i)
        .build();
    groovy.evaluate("i.incrementAndGet()");
    assertThat(i.get(), is(1));

  }
  

  @Test
  public void test__multipleEvaluationsWithVariableInteraction() {
    
    AtomicInteger i = new AtomicInteger();
    GroovyInterpreter groovy = GroovyInterpreter.newInterpreter()
        .addVariable("i", i)
        .build();
    groovy.evaluate("i.incrementAndGet()");
    groovy.evaluate("i.incrementAndGet()");
    assertThat(i.get(), is(2));

  }
  
  @Test
  public void test__multipleVariablesWithSameName() {
    
    AtomicInteger i = new AtomicInteger();
    GroovyInterpreter groovy = GroovyInterpreter.newInterpreter()
        .addVariable("i", new Object())
        .addVariable("i", i)
        .build();
    groovy.evaluate("i.incrementAndGet()");
    assertThat(i.get(), is(1));
    
  }
  
  @Test(expected = RuntimeException.class)
  public void test__evaluate_scriptWithSyntaxError() {
    
    GroovyInterpreter groovy = GroovyInterpreter.newInterpreter().build();
    groovy.evaluate("if(}{)");
    
  }
  
  @Test(expected = GroovyException.class)
  public void test__tryEvaluate_scriptWithSyntaxError() throws GroovyException {
    
    GroovyInterpreter groovy = GroovyInterpreter.newInterpreter().build();
    groovy.tryEvaluate("if(}{)");
    
  }
  
  @Test(expected = GroovyException.class)
  public void test__tryEvaluate_nonExistentVariable() throws GroovyException {
    
    GroovyInterpreter groovy = GroovyInterpreter.newInterpreter().build();
    groovy.tryEvaluate("o.toString()");
    
  }

  @Test(expected = GroovyException.class)
  public void test__tryEvaluate_nonExistentMethod() throws GroovyException {
    
    GroovyInterpreter groovy = GroovyInterpreter.newInterpreter().build();
    groovy.tryEvaluate("foo()");
    
  }

}
