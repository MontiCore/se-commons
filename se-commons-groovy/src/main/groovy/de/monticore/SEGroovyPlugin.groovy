/* (c) https://github.com/MontiCore/monticore */
package de.monticore

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This class realizes the plugin itself.
 * The plugin is only used to provide task type
 * SEGroovyTask but no predefined task instances
 */
class SEGroovyPlugin implements Plugin<Project> {
  
  public void apply(Project project) {
    project.ext.SEGroovyTask = SEGroovyTask
  }
}


