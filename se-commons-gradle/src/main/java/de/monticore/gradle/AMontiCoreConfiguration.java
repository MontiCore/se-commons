/* (c) https://github.com/MontiCore/monticore */

package de.monticore.gradle;

/**
 * Provides access to the MontiCore Tool Options
 */
public abstract class AMontiCoreConfiguration {


  /**
   * Constants for the allowed CLI options in their long and short froms.
   * Stored in constants as they are used multiple times in MontiCore.
   */
  public static final String GRAMMAR = "g";
  public static final String OUT = "o";
  public static final String TOOL_JAR_NAME = "tn";
  public static final String MODELPATH = "mp";
  public static final String HANDCODEDPATH = "hcp";
  public static final String HANDCODEDMODELPATH = "hcg";
  public static final String SCRIPT = "sc";
  public static final String TEMPLATEPATH = "fp";
  public static final String CONFIGTEMPLATE = "ct";
  public static final String DEV = "d";
  public static final String CUSTOMLOG = "cl";
  public static final String REPORT = "r";

  public static final String REPORT_BASE = "rb";

  public static final String GRAMMAR_LONG = "grammar";
  public static final String OUT_LONG = "out";
  public static final String TOOL_JAR_NAME_LONG = "toolName";
  public static final String MODELPATH_LONG = "modelPath";
  public static final String HANDCODEDPATH_LONG = "handcodedPath";
  public static final String HANDCODEDMODELPATH_LONG = "modelPathHC";
  public static final String SCRIPT_LONG = "script";
  public static final String TEMPLATEPATH_LONG = "templatePath";
  public static final String CONFIGTEMPLATE_LONG = "configTemplate";
  public static final String DEV_LONG = "dev";
  public static final String CUSTOMLOG_LONG = "customLog";
  public static final String REPORT_LONG = "report";
  public static final String REPORT_BASE_LONG = "report_base";
  public static final String GENDST_LONG = "genDST";
  public static final String GENTAG_LONG = "genTag";

}
