/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    assertEquals(5, Log.getFindings().size());
    assertEquals(3, Log.getErrorCount());
    
    assertEquals(Finding.Type.ERROR, Log.getFindings().get(0).getType());
    assertEquals(f0, Log.getFindings().get(0).getMsg());
    
    assertEquals(Finding.Type.USER_ERROR, Log.getFindings().get(1).getType());
    assertEquals(f1, Log.getFindings().get(1).getMsg());
    
    assertEquals(Finding.Type.WARNING, Log.getFindings().get(2).getType());
    assertEquals(f2, Log.getFindings().get(2).getMsg());
    
    assertEquals(Finding.Type.ERROR, Log.getFindings().get(3).getType());
    assertEquals(f3, Log.getFindings().get(3).getMsg());
    
    assertEquals(Finding.Type.WARNING, Log.getFindings().get(4).getType());
    assertEquals(f4, Log.getFindings().get(4).getMsg());
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
    assertEquals(5, Log.getFindings().size());
    assertEquals(3, Log.getErrorCount());
    
    assertEquals(Finding.Type.ERROR, Log.getFindings().get(0).getType());
    assertEquals(f0, Log.getFindings().get(0).getMsg());
    
    assertEquals(Finding.Type.ERROR, Log.getFindings().get(1).getType());
    assertEquals(f1, Log.getFindings().get(1).getMsg());
    
    assertEquals(Finding.Type.WARNING, Log.getFindings().get(2).getType());
    assertEquals(f2, Log.getFindings().get(2).getMsg());
    
    assertEquals(Finding.Type.ERROR, Log.getFindings().get(3).getType());
    assertEquals(f3, Log.getFindings().get(3).getMsg());
    
    assertEquals(Finding.Type.WARNING, Log.getFindings().get(4).getType());
    assertEquals(f4, Log.getFindings().get(4).getMsg());
  }
}
