package de.se_rwth.commons.groovy;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import de.se_rwth.commons.Splitters;
import de.se_rwth.commons.Values;
import de.se_rwth.commons.configuration.Configuration;

import java.util.*;

import static com.google.common.collect.Iterables.transform;

public class GroovyRunnerMapConfiguration implements Configuration {

    /**
     * Factory method for {@link GroovyRunnerMapConfiguration}.
     */
    public static final GroovyRunnerMapConfiguration fromMap(Map<String, String> properties) {
        return fromMap(properties, Splitters.SEMICOLON);
    }

    /**
     * Factory method for {@link GroovyRunnerMapConfiguration}.
     */
    public static final GroovyRunnerMapConfiguration fromMap(
            Map<String, String> properties,
            Splitter splitter) {
        return new GroovyRunnerMapConfiguration(properties, splitter);
    }

    /**
     * Factory method for {@link GroovyRunnerMapConfiguration}.
     */
    public static final GroovyRunnerMapConfiguration fromSplitMap(
            Multimap<String, String> properties) {
        return new GroovyRunnerMapConfiguration(properties);
    }

    /**
     * Factory method for {@link GroovyRunnerMapConfiguration}.
     */
    public static final GroovyRunnerMapConfiguration fromSplitMap(
            Map<String, Iterable<String>> properties) {
        return new GroovyRunnerMapConfiguration(properties);
    }

    private final Map<String, Iterable<String>> properties;

    /**
     * Constructor for
     * de.se_rwth.commons.groovy.GroovyRunnerMapConfiguration
     */
    GroovyRunnerMapConfiguration(Map<String, String> properties, Splitter listSplitter) {
        Splitter splitter = listSplitter.trimResults().omitEmptyStrings();
        Map<String, Iterable<String>> splitProperties = new HashMap<>();
        for (Map.Entry<String, String> property : properties.entrySet()) {
            splitProperties.put(property.getKey(), splitter.split(property.getValue()));
        }
        this.properties = splitProperties;
    }

    /**
     * Constructor for
     * de.se_rwth.commons.groovy.GroovyRunnerMapConfiguration
     */
    public GroovyRunnerMapConfiguration(Map<String, Iterable<String>> properties) {
        this.properties = Collections.unmodifiableMap(properties);
    }

    /**
     * Constructor for
     * de.se_rwth.commons.groovy.GroovyRunnerMapConfiguration
     */
    GroovyRunnerMapConfiguration(Multimap<String, String> properties) {
        Map<String, Iterable<String>> builtProperties = new HashMap<>();
        for (Map.Entry<String, String> property : properties.entries()) {
            builtProperties.put(property.getKey(), properties.get(property.getKey()));
        }
        this.properties = builtProperties;
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#getAllValues()
     */
    @Override
    public Map<String, Object> getAllValues() {
        return ImmutableMap.<String, Object>builder()
                .putAll(this.properties)
                .build();
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#getAllValuesAsStrings()
     */
    @Override
    public Map<String, String> getAllValuesAsStrings() {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder();
        this.properties.forEach((key, value) -> builder.put(key, value.toString()));
        return builder.build();
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#getAsBoolean(java.lang.String)
     */
    @Override
    public Optional<Boolean> getAsBoolean(String key) {
        String property = null;
        if (this.properties.containsKey(key)) {
            property = Iterables.getFirst(this.properties.get(key), null);
            if (property != null)
                return Optional.of(Boolean.valueOf(property));
        }
        return Optional.empty();
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#getAsBooleans(java.lang.String)
     */
    @Override
    public Optional<List<Boolean>> getAsBooleans(String key) {
        return this.properties.containsKey(key)
                ? Optional.<List<Boolean>>of(ImmutableList.<Boolean>copyOf(
                transform(this.properties.get(key),
                        new Function<String, Boolean>() {

                            @Override
                            public Boolean apply(String string) {
                                return Boolean.valueOf(string);
                            }
                        })))
                : Optional.empty();
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#getAsDouble(java.lang.String)
     */
    @Override
    public Optional<Double> getAsDouble(String key) {
        String property = null;
        if (this.properties.containsKey(key)) {
            property = Iterables.getFirst(this.properties.get(key), null);
        }
        return property != null
                ? Optional.of(Doubles.tryParse(property))
                : Optional.empty();
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#getAsDoubles(java.lang.String)
     */
    @Override
    public Optional<List<Double>> getAsDoubles(String key) {
        try {
            return this.properties.containsKey(key)
                    ? Optional.<List<Double>>of(ImmutableList.<Double>copyOf(
                    transform(this.properties.get(key),
                            new Function<String, Double>() {

                                @Override
                                public Double apply(String string) {
                                    return Doubles.tryParse(string);
                                }
                            })))
                    : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#getAsInteger(java.lang.String)
     */
    @Override
    public Optional<Integer> getAsInteger(String key) {
        String property = null;
        if (this.properties.containsKey(key)) {
            property = Iterables.getFirst(this.properties.get(key), null);
        }
        return property != null
                ? Optional.of(Ints.tryParse(property))
                : Optional.empty();
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#getAsIntegers(java.lang.String)
     */
    @Override
    public Optional<List<Integer>> getAsIntegers(String key) {
        try {
            return this.properties.containsKey(key)
                    ? Optional.<List<Integer>>of(ImmutableList.<Integer>copyOf(
                    transform(this.properties.get(key),
                            new Function<String, Integer>() {

                                @Override
                                public Integer apply(String string) {
                                    return Ints.tryParse(string);
                                }
                            })))
                    : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#getAsString(java.lang.String)
     */
    @Override
    public Optional<String> getAsString(String key) {
        String property = null;
        if (this.properties.containsKey(key)) {
            property = Iterables.getFirst(this.properties.get(key), null);
        }
        return property != null
                ? Optional.of(property)
                : Optional.empty();
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#getAsStrings(java.lang.String)
     */
    @Override
    public Optional<List<String>> getAsStrings(String key) {
        return this.properties.containsKey(key)
                ? Optional.<List<String>>of(ImmutableList.<String>copyOf(this.properties.get(key)))
                : Optional.empty();
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#getValue(java.lang.String)
     */
    @Override
    public Optional<Object> getValue(String key) {
        String property = null;
        if (this.properties.containsKey(key)) {
            property = Iterables.getFirst(this.properties.get(key), null);
        }
        return property != null
                ? Optional.<Object>of(property)
                : Optional.empty();
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#getValues(java.lang.String)
     */
    @Override
    public Optional<List<Object>> getValues(String key) {
        return Values.checkTypes(this.properties.get(key), Object.class);
    }

    /**
     * @see de.se_rwth.commons.configuration.Configuration#hasProperty(java.lang.String)
     */
    @Override
    public boolean hasProperty(String key) {
        return this.properties.containsKey(key);
    }

}
