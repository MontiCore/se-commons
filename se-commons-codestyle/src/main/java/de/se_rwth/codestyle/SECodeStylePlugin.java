/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.codestyle;

import com.diffplug.gradle.spotless.SpotlessExtension;
import com.diffplug.gradle.spotless.SpotlessPlugin;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * This plugin applies & configures the spotless code formatter,
 * as well as provides an .editorconfig file.
 */
@SuppressWarnings("unused")
public class SECodeStylePlugin implements Plugin<Project> {
  
  @Override
  public void apply(@Nonnull Project project) {
    Provider<RegularFile> codeStyleFile =
        project.getLayout().getBuildDirectory().file("se-codestyle-eclipse.xml");
    // The task creation of spotless requires the xml config file to exist
    project.afterEvaluate(p -> {
      // Thus, we create the file if it does not exist
      if (!codeStyleFile.get().getAsFile().exists()) {
        try {
          var path = codeStyleFile.get().getAsFile().toPath();
          Files.createDirectories(path.getParent());
          Files.createFile(path);
        }
        catch (IOException ex) {
          p.getLogger().error("Failed to prepare se code style file", ex);
        }
      }
    });
    // Next, ensure the spotless plugin is applied
    project.getPlugins().apply(SpotlessPlugin.class);
    
    // The extractSECodeStyle *actually* extracts the code style config file
    var xmlTask = project.getTasks().register("extractSECodeStyle", ExtractCodeStyleFileTask.class);
    xmlTask.configure(it -> {
      it.getDestination().set(codeStyleFile);
      it.getInput().set("se-codestyle/se-codestyle-eclipse.xml");
    });
    
    // Ensure the XML is populated before spotless runs
    project.getTasks().getByName("spotlessInternalRegisterDependencies").dependsOn(xmlTask);
    project.getTasks().getByName("spotlessDiagnose").dependsOn(xmlTask);
    
    // Configure spotless
    SpotlessExtension spotless = project.getExtensions().getByType(SpotlessExtension.class);
    spotless.java(extension -> {
      extension.targetExclude("build/**", "target/**");
      
      // trim trailing whitespace before doing the Eclipse formatting
      extension.trimTrailingWhitespace();
      
      // Eclipse formatting
      extension.eclipse().configFile(codeStyleFile);
      
      extension.licenseHeader("/* (c) https://github.com/MontiCore/monticore */");
      extension.endWithNewline();
      extension.toggleOffOn("@formatter:off", "@formatter:on");
      extension.removeUnusedImports();
      extension.indentWithSpaces(2);
    });
    
    spotless.format("markdown", extension -> {
      extension.target("**/*.md");
      
      extension.endWithNewline();
    });
    
    spotless.format("montiCore", extension -> {
      extension.target("**/*.mc4");
      
      extension.licenseHeader("/* (c) https://github.com/MontiCore/monticore */", "(package)");
      extension.indentWithSpaces(2);
      extension.endWithNewline();
    });
    
    // Provide the .editorconfig file
    // We do not provide it via a task to always create it
    File editorConfig = project.file(".editorconfig");
    if (!editorConfig.exists()) {
      try {
        try (
            InputStream url = getClass().getClassLoader()
                .getResourceAsStream("se-codestyle/.editorconfig")) {
          Files.copy(Objects.requireNonNull(url), editorConfig.toPath(),
              StandardCopyOption.REPLACE_EXISTING);
        }
      }
      catch (IOException e) {
        project.getLogger().warn("Failed to provide editorconfig", e);
      }
    }
  }
}
