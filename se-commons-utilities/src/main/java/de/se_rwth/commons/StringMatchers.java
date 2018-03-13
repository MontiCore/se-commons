/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;

/**
 * Utilities for matching substrings in strings.
 *
 *
 */
public final class StringMatchers {
  
  /**
   * Matches a single dot.
   */
  public static final CharMatcher DOT = CharMatcher.is('.');
  
  /**
   * Matches a backslash.
   */
  public static final CharMatcher BACKSLASH = CharMatcher.is('\\');
  
  /**
   * Matches a linebreak.
   */
  public static final CharMatcher LINEBREAK = CharMatcher.is('\n');
  
  /**
   * Matches a quotation mark.
   */
  public static final CharMatcher QUOTATION_MARK = CharMatcher.is('\"');
  
  /**
   * Matches an underscore mark.
   */
  public static final CharMatcher UNDERSCORE = CharMatcher.is('_');
  
  /**
   * Matches a java letter or a dot.
   */
  public static final CharMatcher JAVA_LETTER_OR_DOT = CharMatcher.JAVA_LETTER.or(DOT);
  
  /**
   * Matches a java identifier.
   */
  public static final CharMatcher JAVA_IDENTIFIER = CharMatcher
      .JAVA_LETTER_OR_DIGIT.or(DOT).or(UNDERSCORE);
  
  /**
   * Compares two Strings representing qualified Name by comparing only the
   * simple name, thus, ignoring the qualifier (e.g. a.b.c.Name == x.y.z.Name).
   */
  public static final boolean equalsIgnoreQualifier(String a, String b) {
    
    checkNotNull(a);
    checkNotNull(b);
    
    boolean equal = false;
    
    if (a.equals(b)) {
      equal = true;
    }
    else if (a.endsWith("." + b)) {
      equal = true;
    }
    else if (b.endsWith("." + a)) {
      equal = true;
    }
    
    return equal;
  }
  
  /**
   * @return true, of the given qualifier is a valid Java qualifier, e.g.
   *         "a.b.c" or "a"
   */
  public static final boolean isValidQualifier(String qualifier) {
    return qualifier != null
        && qualifier.length() > 0
        && CharMatcher.JAVA_LETTER.matches(qualifier.charAt(0))
        && CharMatcher.JAVA_LETTER_OR_DIGIT.matches(qualifier.charAt(qualifier.length() - 1))
        && JAVA_IDENTIFIER.matchesAllOf(qualifier);
  }
  
  /**
   * @return a subcollection of the given collection of strings filtered
   *         containing all strings that lave a levenshtein distance less then
   *         the given maximumdistance in relation to the given reference
   *         string.
   */
  public static final Collection<String> filterByLevenshteinDistance(
      final Collection<String> strings, final String reference, final int maximumDistance) {
    
    checkNotNull(strings);
    checkNotNull(reference);
    checkArgument(maximumDistance >= 0);
    
    ImmutableList.Builder<String> matches = ImmutableList.builder();
    
    for (String string : strings) {
      int d = getLevenshteinDistance(string, reference);
      if (d <= maximumDistance) {
        matches.add(string);
      }
    }
    
    return matches.build();
  }
  
  /**
   * Calculates the Levenshtein distance between two strings, that is, the
   * number of atomic substitutions necessary to convert one string into
   * another.
   */
  public static final int getLevenshteinDistance(CharSequence a, CharSequence b) {
    
    checkNotNull(a);
    checkNotNull(b);
    
    int n = a.length();
    int m = b.length();
    
    if (n == 0) {
      return m;
    }
    else if (m == 0) {
      return n;
    }
    
    if (n > m) {
      CharSequence swap = a;
      a = b;
      b = swap;
      n = m;
      m = b.length();
    }
    
    int costs[] = new int[n + 1];
    int previousCosts[] = new int[n + 1];
    int costsSwapBuffer[];
    
    int i;
    int j;
    
    char t_j;
    
    int cost;
    
    for (i = 0; i <= n; i++) {
      previousCosts[i] = i;
    }
    
    for (j = 1; j <= m; j++) {
      t_j = b.charAt(j - 1);
      costs[0] = j;
      
      for (i = 1; i <= n; i++) {
        cost = a.charAt(i - 1) == t_j
            ? 0
            : 1;
        costs[i] = Math.min(
            Math.min(
                costs[i - 1] + 1,
                previousCosts[i] + 1),
            previousCosts[i - 1] + cost);
      }
      
      costsSwapBuffer = previousCosts;
      previousCosts = costs;
      costs = costsSwapBuffer;
    }
    
    return previousCosts[n];
  }
  
  /**
   * Returns true if the given wildcard pattern (e.g. *Foo*Bar) can be found in
   * the given text.
   */
  public static boolean matchesWildcardPattern(String text, String pattern) {
    
    checkNotNull(text);
    checkNotNull(pattern);
    
    boolean matches = true;
    
    if (pattern.isEmpty() && !text.isEmpty()) {
      matches = false;
    }
    else {
      
      Iterable<String> cards = Splitters.WILDCARD.split(pattern);
      
      for (String card : cards)
      {
        int index = text.indexOf(card);
        if (index == -1)
        {
          return false;
        }
        text = text.substring(index + card.length());
      }
      
    }
    
    return matches;
  }
  
  /**
   * Private constructor for class with static utilities.
   */
  private StringMatchers() {
  }
  
}
