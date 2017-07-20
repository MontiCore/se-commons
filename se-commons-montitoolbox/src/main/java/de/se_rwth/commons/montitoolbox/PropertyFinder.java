/*
 * ******************************************************************************
 * MontiCore Language Workbench, www.monticore.de
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

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class PropertyFinder {

  public static List<String> propertyToString(File pomfile) {
    Model model;
    FileReader reader;
    MavenXpp3Reader mavenreader = new MavenXpp3Reader();

    try {
      reader = new FileReader(pomfile); // <-- pomfile is your pom.xml
      model = mavenreader.read(reader);
      model.setPomFile(pomfile);
    } catch(Exception ex){
      throw new MavenPOMNotFoundException("Maven pom.xml not found. This is necessary to use this doclet.");
    }

    MavenProject project = new MavenProject(model);
    List<String> result = new ArrayList<>();
    result.add(0, project.getGroupId());
    result.add(1, project.getArtifactId());
    result.add(2, project.getVersion());
    return result;
  }
}
