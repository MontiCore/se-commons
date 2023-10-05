/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.groovy;

import org.codehaus.groovy.GroovyException;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
