/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.montitoolbox;

/**
 * A Taglist for the viable annotations in Javadoc comments.
 * (@param, @return, @input, @output)
 * (more can be added)
 */
public enum Taglist {
  PARAMETER("Parameter"),
  RETURN("Returns"),
  INPUT("Input"),
  OUTPUT("Output");

  private String tag;

  /**
   * Constructor.
   *
   * @param tag name
   */
  Taglist(String tag) {
    this.tag = tag;
  }

  /**
   * Getter for the String of the Tag.
   *
   * @return String version of the Tag
   */
  public String getTag() {
    return this.tag;
  }
}
