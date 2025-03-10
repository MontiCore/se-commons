/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.logging;

/**
 * A logger that supports rich console output, including colors and hyperlinks to files.
 */
public class RichConsoleLogHook extends ConsoleLogHook {

  public static final String RESET = "\033[0m";
  public static final String BLUE = "\033[0;34m";
  public static final String YELLOW = "\033[0;33m";
  public static final String RED = "\033[0;31m";
  public static final String RED_BOLD = "\033[1;31m";

  @Override
  public String formatInfo(String msg, String logName) {
    return String.format("%s[INFO]%s  %s %s", BLUE, RESET, logName, msg);
  }

  @Override
  public String formatWarn(Finding warn) {
    return String.format("%s[WARN]%s  %s", YELLOW, RESET, warn.buildMsgGNU());
  }

  @Override
  public String formatError(Finding error) {
    return String.format("%s[ERROR]%s  %s", RED_BOLD, RESET, error.buildMsgGNU());
  }

  @Override
  public String formatErrorUser(Finding error) {
    return String.format("%s[USER-ERROR]%s  %s", RED, RESET, error.buildMsgGNU());
  }
}
