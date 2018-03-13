/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * Unit test for {@link ProtectedSpaceSplitter}.
 *
 *
 */
public class ProtectedSpaceSplitterUnitTest {
  
  @Test
  public void test__split_withOneDelimiterOmittingEmptyStrings() {
    
    Iterable<String> parts;
    
    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .omitEmptyStrings()
        .split("");
    
    assertThat(Iterables.size(parts), is(0));
    
    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .omitEmptyStrings()
        .split("\"\"");

    assertThat(Iterables.size(parts), is(0));
    
    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .omitEmptyStrings()
        .split("\" \"");
    
    assertThat(Iterables.size(parts), is(1));

    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .omitEmptyStrings()
        .split(" \" \"");
    
    assertThat(Iterables.size(parts), is(1));

  }
  
  @Test
  public void test__split_withOneDelimiterExcludingDelimiters() {
    
    Iterable<String> parts;
    
    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .split("Ich sage: \"Hello, World!\".");

    assertThat(Iterables.size(parts), is(4));
    assertThat(Iterables.get(parts, 0), is("Ich"));
    assertThat(Iterables.get(parts, 1), is("sage:"));
    assertThat(Iterables.get(parts, 2), is("Hello, World!"));
    assertThat(Iterables.get(parts, 3), is("."));
    
    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .split("Ich sage: \"Hello, World!\"");
    
    assertThat(Iterables.size(parts), is(3));
    assertThat(Iterables.get(parts, 0), is("Ich"));
    assertThat(Iterables.get(parts, 1), is("sage:"));
    assertThat(Iterables.get(parts, 2), is("Hello, World!"));
    
    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .split("\"Hello, World!\", sagte ich.");
    
    assertThat(Iterables.size(parts), is(4));
    assertThat(Iterables.get(parts, 0), is("Hello, World!"));
    assertThat(Iterables.get(parts, 1), is(","));
    assertThat(Iterables.get(parts, 2), is("sagte"));
    assertThat(Iterables.get(parts, 3), is("ich."));
    
    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .split("\"Hello, World!\"");
    
    assertThat(Iterables.size(parts), is(1));
    assertThat(Iterables.get(parts, 0), is("Hello, World!"));
    
    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .split("\"Hello, World!\"");
    
    assertThat(Iterables.size(parts), is(1));
    assertThat(Iterables.get(parts, 0), is("Hello, World!"));

    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .split("\"\"");
    
    assertThat(Iterables.size(parts), is(1));

    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .split("\" \"");
    
    assertThat(Iterables.size(parts), is(1));
    assertThat(Iterables.get(parts, 0), is(" "));

    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .split(" \" \"");

    assertThat(Iterables.size(parts), is(2));
    assertThat(Iterables.get(parts, 0), is(""));
    assertThat(Iterables.get(parts, 1), is(" "));

    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .split(" \" \" ");
    
    assertThat(Iterables.size(parts), is(4));
    assertThat(Iterables.get(parts, 0), is(""));
    assertThat(Iterables.get(parts, 1), is(" "));
    assertThat(Iterables.get(parts, 2), is(""));
    assertThat(Iterables.get(parts, 3), is(""));

  }
  
  @Test
  public void test__split_withOneDelimiterIncludingDelimiters() {
    
    Iterable<String> parts;

    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .includeDelimiters(true)
        .split("Ich sage: \"Hello, World!\".");
    
    assertThat(Iterables.size(parts), is(4));
    assertThat(Iterables.get(parts, 0), is("Ich"));
    assertThat(Iterables.get(parts, 1), is("sage:"));
    assertThat(Iterables.get(parts, 2), is("\"Hello, World!\""));
    assertThat(Iterables.get(parts, 3), is("."));

    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .includeDelimiters(true)
        .split("Ich sage: \"Hello, World!\"");
    
    assertThat(Iterables.size(parts), is(3));
    assertThat(Iterables.get(parts, 0), is("Ich"));
    assertThat(Iterables.get(parts, 1), is("sage:"));
    assertThat(Iterables.get(parts, 2), is("\"Hello, World!\""));

    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .includeDelimiters(true)
        .split("\"Hello, World!\", sagte ich.");
    
    assertThat(Iterables.size(parts), is(4));
    assertThat(Iterables.get(parts, 0), is("\"Hello, World!\""));
    assertThat(Iterables.get(parts, 1), is(","));
    assertThat(Iterables.get(parts, 2), is("sagte"));
    assertThat(Iterables.get(parts, 3), is("ich."));

    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .includeDelimiters(true)
        .split("\"Hello, World!\"");
    
    assertThat(Iterables.size(parts), is(1));
    assertThat(Iterables.get(parts, 0), is("\"Hello, World!\""));

    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiter('"')
        .includeDelimiters(true)
        .split("\"Hello, World!\"");
    
    assertThat(Iterables.size(parts), is(1));
    assertThat(Iterables.get(parts, 0), is("\"Hello, World!\""));

  }
  
  @Test
  public void test__split_withTwoDelimitersExcludingDelimiters() {
    
    Iterable<String> parts;

    parts = ProtectedSpaceSplitter
        .on(' ')
        .withProtectionDelimiters('{', '}')
        .split("Ich sage: {Hello, World!}.");
    
    assertThat(Iterables.size(parts), is(4));
    assertThat(Iterables.get(parts, 0), is("Ich"));
    assertThat(Iterables.get(parts, 1), is("sage:"));
    assertThat(Iterables.get(parts, 2), is("Hello, World!"));
    assertThat(Iterables.get(parts, 3), is("."));

  }

}
