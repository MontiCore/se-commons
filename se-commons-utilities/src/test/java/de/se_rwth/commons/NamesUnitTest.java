/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link Names}.
 *
 *
 */
public class NamesUnitTest {
  
  @Test
  public void test__getParameterizedTypeName() {
    
    assertThat(
        Names.getParameterizedTypeName("cc.clarc.foo.Blubb", ImmutableList.of("A")),
        is("cc.clarc.foo.Blubb<A>"));
    
    assertThat(
        Names.getParameterizedTypeName("cc.clarc.foo.Blubb", ImmutableList.of("A", "B")),
        is("cc.clarc.foo.Blubb<A,B>"));
    
    assertThat(
        Names.getParameterizedTypeName("cc.clarc.foo.Blubb", ImmutableList.of()),
        is("cc.clarc.foo.Blubb"));
    
  }
  
  @Test
  public void test__getQualifiedParameterizedTypeName() {
    
    assertThat(
        Names.getQualifiedParameterizedTypeName("cc.clarc.foo", "Blubb", ImmutableList.of("A")),
        is("cc.clarc.foo.Blubb<A>"));

    assertThat(
        Names.getQualifiedParameterizedTypeName("cc.clarc.foo.", "Blubb", ImmutableList.of("A")),
        is("cc.clarc.foo.Blubb<A>"));
    
    assertThat(
        Names.getQualifiedParameterizedTypeName("", "Blubb", ImmutableList.of("A")),
        is("Blubb<A>"));
    
    assertThat(
        Names.getQualifiedParameterizedTypeName("cc.clarc.foo.", ".Blubb", ImmutableList.of("A", "B")),
        is("cc.clarc.foo.Blubb<A,B>"));
    
    assertThat(
        Names.getQualifiedParameterizedTypeName("cc.clarc.foo", "Blubb", ImmutableList.of()),
        is("cc.clarc.foo.Blubb"));
    
  }
  
  @Test
  public void test__getParameterList() {
    
    assertThat(
        Names.getParameterList(ImmutableList.of("A")),
        is("<A>"));
    
    assertThat(
        Names.getParameterList(ImmutableList.of("A", "B")),
        is("<A,B>"));
    
    assertThat(
        Names.getParameterList(ImmutableList.of()),
        is(""));
    
  }
  
  @Test
  public void test__getQualifiedName() {
    
    assertThat(
        Names.getQualifiedName("cc.clarc.foo", "Blabb"),
        is("cc.clarc.foo.Blabb"));
    
    assertThat(
        Names.getQualifiedName("cc.clarc.foo.", "Blabb"),
        is("cc.clarc.foo.Blabb"));
    
    assertThat(
        Names.getQualifiedName("cc.clarc.foo", ".Blabb"),
        is("cc.clarc.foo.Blabb"));
    
    assertThat(
        Names.getQualifiedName("cc.clarc.foo.", ".Blabb"),
        is("cc.clarc.foo.Blabb"));
    
    assertThat(
        Names.getQualifiedName("", "Blabb"),
        is("Blabb"));
    
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void test__getQualifiedName_invalidArguments() {
    
    Names.getQualifiedName("", "");
    Names.getQualifiedName("cc.blubb", "");
    
  }
  
  @Test
  public void test__getQualifier() {
    
    assertThat(
        Names.getQualifier("cc.clarc.foo.Blubb"),
        is("cc.clarc.foo"));
    
    assertThat(
        Names.getQualifier("foo.Blubb"),
        is("foo"));
    
    assertThat(
        Names.getQualifier(""),
        is(""));
    
    assertThat(
        Names.getQualifier("Blubb"),
        is(""));
    
    assertThat(
        Names.getQualifier(".Blubb"),
        is(""));

    assertThat(
        Names.getQualifier(".a.Blubb"),
        is("a"));
    
    assertThat(
        Names.getQualifier("foo."),
        is("foo"));
    
    assertThat(
        Names.getQualifier("foo.Bar<Blubb>"),
        is("foo"));
    
    assertThat(
        Names.getQualifier(""),
        is(""));
    
  }
  
  @Test
  public void test__getSimpleName() {
    
    assertThat(
        Names.getSimpleName("cc.clarc.foo.Blubb"),
        is("Blubb"));
    
    assertThat(
        Names.getSimpleName("foo.Blubb"),
        is("Blubb"));
    
    assertThat(
        Names.getSimpleName(""),
        is(""));
    
    assertThat(
        Names.getSimpleName("Blubb"),
        is("Blubb"));
    
    assertThat(
        Names.getSimpleName(".Blubb"),
        is("Blubb"));
    
    assertThat(
        Names.getSimpleName("foo."),
        is(""));
    
    assertThat(
        Names.getSimpleName("foo.Bar<Blubb>"),
        is("Bar<Blubb>"));
    
    assertThat(
        Names.getSimpleName(""),
        is(""));
    
  }

  @Test
  public void test__getPathFromQualifiedName() {
    assertThat(Names.getPathFromQualifiedName("d"), is(""));
    assertThat(Names.getPathFromQualifiedName("D"), is(""));
    assertThat(Names.getPathFromQualifiedName("amt.test.name"), is("amt" + File.separator + "test"));
    assertThat(Names.getPathFromQualifiedName("a.b.C"), is("a" + File.separator + "b"));
    assertThat(Names.getPathFromQualifiedName("a.b."), is("a" + File.separator + "b"));
    assertThat(Names.getPathFromQualifiedName(".a.b."), is("a" + File.separator + "b"));
  }
  
  @Test
  public void test__getPathFromPackage() {
    assertThat(Names.getPathFromPackage(""), is(""));
    assertThat(Names.getPathFromPackage("a.b.C"), is("a" + File.separator + "b" + File.separator
        + "C"));
    assertThat(Names.getPathFromPackage("a.b."), is("a" + File.separator + "b" + File.separator));
    assertThat(Names.getPathFromPackage(".a.b"), is(File.separator + "a" + File.separator + "b"));
  }
  
  @Test
  public void test__getPathFromFilename() {
    assertThat("File.separator must be used as default separator",
        Names.getPathFromFilename("a" + File.separator + "b"), is("a"));

    assertThat(Names.getPathFromFilename("a/b/c.ext", "/"), is("a/b"));
    assertThat(Names.getPathFromFilename("/a/b/c", "/"), is("/a/b"));
    assertThat(Names.getPathFromFilename("", "/"), is(""));
    assertThat(Names.getPathFromFilename("/a/", "/"), is("/a"));
    assertThat(Names.getPathFromFilename("/a/b", "/"), is("/a"));
  }
  
  @Test
  public void test__getPackageFromPath() {
    assertThat("File.separator must be used as default separator",
        Names.getPackageFromPath("a" + File.separator + "b"), is("a.b"));
    
    assertThat(Names.getPackageFromPath("a/b", "/"), is("a.b"));
    assertThat(Names.getPackageFromPath("a/b/", "/"), is("a.b."));
    assertThat(Names.getPackageFromPath("/a/b/c", "/"), is(".a.b.c"));
  }
}
