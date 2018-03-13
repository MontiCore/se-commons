/* (c) https://github.com/MontiCore/monticore */
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
