/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import de.monticore.gradle.internal.ProgressLoggerService;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

/**
 * work parameters consisting of CLI arguments
 */
public interface ToolArgActionParameter extends WorkParameters {
    /**
     * Arguments passed to a Tool
     */
    ListProperty<String> getArgs();

    /**
     * Name of this work action, passed to the log
     */
    Property<String> getProgressName();

    /**
     * Optional extra classpath elements
     */
    ConfigurableFileCollection getExtraClasspathElements();

    /**
     * See {@link ProgressLoggerService}
     */
    Property<ProgressLoggerService> getProgressLogger();
}
