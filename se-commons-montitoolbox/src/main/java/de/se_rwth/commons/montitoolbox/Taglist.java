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
