/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * This is a test of interactive mode of the Logger.
 */
public class InteractiveLogTest {
  
  // finding messages
  String f0 = "Internal Error 1.";
  String f1 = "User Error 1.";
  String f2 = "User Warning 1.";
  String f3 = "Internal Error 2.";
  String f4 = "User Warning 2.";
  
  @Test
  public void testInteractiveLogging() {
    // init interactive logging
    Log.initInteractive();

    // create entries
    Log.errorInternal(f0);
    Log.errorUser(f1);
    Log.warnUser(f2);
    Log.error(f3);
    Log.warn(f4);
    
    // check entries
    assertEquals(Log.getFindings().size(),5);
    assertEquals(Log.getErrorCount(),3);
    
    assertEquals(Log.getFindings().get(0).getType() , Finding.Type.ERROR);
    assertEquals(Log.getFindings().get(0).getMsg() , f0);
    
    assertEquals(Log.getFindings().get(1).getType(), Finding.Type.USER_ERROR);
    assertEquals(Log.getFindings().get(1).getMsg() , f1);
    
    assertEquals(Log.getFindings().get(2).getType(), Finding.Type.WARNING);
    assertEquals(Log.getFindings().get(2).getMsg() , f2);
    
    assertEquals(Log.getFindings().get(3).getType(), Finding.Type.ERROR);
    assertEquals(Log.getFindings().get(3).getMsg() , f3);
    
    assertEquals(Log.getFindings().get(4).getType(), Finding.Type.WARNING);
    assertEquals(Log.getFindings().get(4).getMsg() , f4);
  }
  
  @Test
  public void testInteractiveLoggingInBatchMode() {
    // init interactive default (non-interactive logging
    Log.init();
    Log.enableFailQuick(false);
    
    // create entries
    Log.errorInternal(f0);
    Log.errorUser(f1);
    Log.warnUser(f2);
    Log.error(f3);
    Log.warn(f4);
    
    // check entries
    assertEquals(Log.getFindings().size(),5);
    assertEquals(Log.getErrorCount(),3);
    
    assertEquals(Log.getFindings().get(0).getType() , Finding.Type.ERROR);
    assertEquals(Log.getFindings().get(0).getMsg() , f0);
    
    assertEquals(Log.getFindings().get(1).getType(), Finding.Type.ERROR);
    assertEquals(Log.getFindings().get(1).getMsg() , f1);
    
    assertEquals(Log.getFindings().get(2).getType(), Finding.Type.WARNING);
    assertEquals(Log.getFindings().get(2).getMsg() , f2);
    
    assertEquals(Log.getFindings().get(3).getType(), Finding.Type.ERROR);
    assertEquals(Log.getFindings().get(3).getMsg() , f3);
    
    assertEquals(Log.getFindings().get(4).getType(), Finding.Type.WARNING);
    assertEquals(Log.getFindings().get(4).getMsg() , f4);
  }
}
