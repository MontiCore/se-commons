/* (c) https://github.com/MontiCore/monticore */
package de.monticore.gradle.common;

import de.monticore.gradle.internal.ProgressLoggerService;
import de.monticore.gradle.queue.CachedIsolatedWorkQueue;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * work parameters consisting of CLI arguments
 */
public interface ToolArgActionParameter extends CachedIsolatedWorkQueue.WorkQueueParameters {
    /**
     * Arguments passed to a Tool
     */
    ListProperty<String> getArgs();

    /**
     * Name of this work action, passed to the log
     */
    @Deprecated
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
