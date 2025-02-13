/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.cli;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Parses and represents arguments passed on the command line.
 * @deprecated use Apache commons-cli
 */
@Deprecated
public final class CLIArguments {
  
  /**
   * Factory method for {@link CLIArguments}.
   */
  public static CLIArguments forArguments(String[] arguments) {
    return new CLIArguments(arguments);
  }
  
  /**
   * The set of set parameters in this argument representation.
   */
  private final Set<String> parameters;
  
  /**
   * The map of set parameters and their respective values.
   */
  private final Map<String, List<String>> valueMap;
  
  /**
   * Constructor for de.monticore.cli.CLIArguments
   */
  public CLIArguments(@Named("arguments") String[] arguments) {
    
    ImmutableSet.Builder<String> parameters = ImmutableSet.builder();
    // ImmutableListMultimap.Builder<String, String> valueMap =
    // ImmutableListMultimap.builder();
    Map<String, List<String>> valueMap = new HashMap<>();
    
    Iterator<String> i = Iterators.forArray(arguments);
    
    CharMatcher parameterDelimiter = CharMatcher.is('-');
    
    String parameter = null;
    List<String> values = newArrayList();
    while (i.hasNext()) {
      String argument = i.next();
      if (parameterDelimiter.indexIn(argument) == 0) {
        // argument is a parameter (starts with "-")
        if (parameter != null) {
          // we found a new parameter and need to store all values collected up
          // to here into the previous parameter
          parameters.add(parameter);
          valueMap.put(parameter, values);
        }
        // reset values for the newly found parameter and store it as current
        // one
        values = newArrayList();
        parameter = parameterDelimiter.trimLeadingFrom(argument);
      }
      else {
        // found a regular value; add it to the current parameter
        values.add(argument);
      }
    }
    // for the last found parameter we need to add all gathered values as a last
    // step outside the loop
    if (parameter != null) {
      parameters.add(parameter);
      valueMap.put(parameter, values);
    }
    
    this.parameters = parameters.build();
    this.valueMap = valueMap;
    
  }
  
  /**
   * @return returns the arguments as an immutable map.
   */
  public Map<String, Iterable<String>> asMap() {
    return Collections.unmodifiableMap(this.valueMap);
  }
  
  /**
   * @return the set of all set parameters.
   */
  public Set<String> getParameters() {
    return this.parameters;
  }
  
  /**
   * @return the values associated with the given parameter. Returns an empty
   * list for not set parameters; never null.
   */
  public List<String> getValues(String parameter) {
    return this.valueMap.get(parameter);
  }
  
  /**
   * @return true if the arguments contain the given parameter.
   */
  public boolean hasParameter(String parameter) {
    return this.parameters.contains(parameter);
  }
  
}
