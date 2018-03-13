/* (c) https://github.com/MontiCore/monticore */
package de.se_rwth.commons.montitoolbox;

import com.sun.javadoc.*;
import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Log;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * The Javadoc2Markdown doclet.
 *
 * @author BS
 */
public class MarkdownDoclet {

  private static Map<Taglist, List<Tag>> tagmap;

  private static PrintWriter pw;

  private static Optional<File> pomfile = Optional.empty();

  /**
   * Root method of the doclet. This is mandatory for the doclet as it will start from here.
   * It iterates over all classes, that are not excluded in the POM, create a *.md file for each class
   * and prints the essential javadoc informations into the file.
   *
   * @param root The RootDoc
   * @return true
   */
  public static boolean start(RootDoc root) throws Exception {
    // user.dir should be "..\project-name\target\checkout\methods, so we move 3 levels up to get to the dir where the pom lays
    String currDir = Paths.get(System.getProperty("user.dir")).getParent().getParent().getParent().toString() + File.separator + "pom.xml";
    File pom = new File(currDir);
    if(pom.exists()) {
      pomfile = Optional.of(pom);
    }
    for(ClassDoc clazz : root.classes()) {
      if(includesMontiToolBox(clazz.tags())) {
        try {
          File file = new File(clazz.name() + ".md");
          pw = new PrintWriter(file);
          printHeader(clazz);
          if(clazz.constructors().length != 0) {
            pw.println("## **CONSTRUCTORS** \n");
          }
          for(ConstructorDoc constructor : clazz.constructors()) {
            pw.print("### **_" + constructor.modifiers() + "_ " + constructor.name() + "(");
            printBody(constructor);
          }
          if(clazz.methods().length != 0) {
            pw.println("## **METHODS** \n");
          }
          for(MethodDoc method : clazz.methods()) {
            pw.print("### **_" + method.modifiers() + " " +
                    method.returnType().simpleTypeName() + "_ " + method.name() + "(");
            printBody(method);
          }
          pw.close();
        } catch(FileNotFoundException e) {
          Log.error("'" + clazz.name() + ".md' is not found.");
        }
      }
    }
    return true;
  }

  /**
   * Searches in the tags of the javadoc class comment for the @montitoolbox annotation.
   * Only if this annotation is found, the doclet will create
   * the *.md file.
   *
   * @param tags Array of tags
   * @return true, if tags contains an @montitoolbox annotation
   */
  private static boolean includesMontiToolBox(Tag[] tags) {
    for(Tag tag : tags) {
      if(tag.name().equals("@montitoolbox")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Prints the Header (package name, class name and class comment)
   *
   * @param clazz
   */
  private static void printHeader(ClassDoc clazz) {
    pw.println("<p align=\"center\"><strong>THIS IS GENERATED. DO NOT EDIT.</strong></p>" + "\n");
    pw.println("_package " + clazz.containingPackage().name() + "_\n");
    pw.println("# " + clazz.name() + "\n");
    pw.println(handle(clazz.commentText()) + "\n");
    String author = "";
    String version = "";
    for(Tag tag : clazz.tags()) {
      if("@author".equals(tag.name())) {
        author = tag.text();
      }
      if("@version".equals(tag.name())) {
        version = tag.text();
      }
    }
    if(!author.isEmpty()) {
      pw.println("### **AUTHOR:** " + author);
    }
    if(!version.isEmpty()) {
      pw.println("### **VERSION:** " + version);
    }
    if(pomfile.isPresent()) {
      List<String> properties = PropertyFinder.propertyToString(pomfile.get());
      pw.println("### **DOWNLOAD:**\n");
      printDownload(properties);
    }
    Map<String, String> constructors = new HashMap<>();
    Map<String, String> methods = new HashMap<>();
    StringBuilder signature;
    StringBuilder link;
    for(ConstructorDoc constructor : clazz.constructors()) {
      signature = new StringBuilder();
      link = new StringBuilder();
      signature.append(constructor.modifiers() + " " + constructor.name() + "(");
      List<Parameter> params = Arrays.asList(constructor.parameters());
      for(Parameter param : params) {
        signature.append(typeToString(param.type()) + param.type().dimension() + " " + param.name());
        //  if there are more parameters, prints a ", "
        if(!param.equals(params.get(params.size() - 1))) {
          signature.append(", ");
        }
      }
      signature.append(")");
      link.append("#" + constructor.modifiers() + "-" + constructor.name());
      link.append(parameterToLinkString(constructor));
      constructors.put(signature.toString(), link.toString().toLowerCase());
    }
    for(MethodDoc method: clazz.methods()) {
      signature = new StringBuilder();
      link = new StringBuilder();
      signature.append(method.modifiers() + " " + method.name() + "(");
      List<Parameter> params = Arrays.asList(method.parameters());
      for(Parameter param : params) {
        signature.append(typeToString(param.type()) + param.type().dimension() + " " + param.name());
        //  if there are more parameters, prints a ", "
        if(!param.equals(params.get(params.size() - 1))) {
          signature.append(", ");
        }
      }
      signature.append(")");
      link.append(("#" + method.modifiers() + "-" + method.returnType().simpleTypeName() + "-" + method.name()).toLowerCase());
      link.append(parameterToLinkString(method));
      methods.put(signature.toString(), link.toString().toLowerCase());
    }
    printLinkList(constructors, methods);
    pw.println();
  }

  private static void printDownload(List<String> properties) {
    pw.println("```");
    pw.println("<dependency>");
    pw.println("  <groupId>" + properties.get(0) + "</groupId>");
    pw.println("  <artifactId>" + properties.get(1) + "</artifactId>");
    pw.println("  <version>" + properties.get(2) + "</version>");
    pw.println("</dependency>");
    pw.println("```");
  }

  private static <T extends ExecutableMemberDoc> String parameterToLinkString(T t) {
    StringBuilder result = new StringBuilder();
    for(Parameter param : t.parameters()) {
      result.append("-" + param.typeName() + "-" + param.name());
    }
    if(result.length() != 0) {
      result.deleteCharAt(0);
    }
    return result.toString();
  }

  private static void printLinkList(Map<String, String> constructors, Map<String, String> methods) {
    pw.println("### **LINKS:**\n");
    pw.println("**CONSTRUCTORS:**\n");
    for(Map.Entry entry : constructors.entrySet()) {
      pw.println("* " + "[" + entry.getKey() + "]" + "(" + entry.getValue() + ")");
    }
    pw.println();
    pw.println("**METHODS:**\n");
    for(Map.Entry entry : methods.entrySet()) {
      pw.println("* " + "[" + entry.getKey() + "]" + "(" + entry.getValue() + ")");
    }
  }


  /**
   * Prints the Body. (methods and constructors)
   * <p>
   * Warning: There are 3 types which extend ExecutableMemberDoc:
   * MethodDoc, ConstructorDoc and AnnotationTypeELementDoc. This method only wants to
   * make use of MethodDoc and ConstructorDoc.
   *
   * @param t   method or constructor -doc
   * @param <T> type (MethodDoc or ConstructorDoc)
   */
  private static <T extends ExecutableMemberDoc> void printBody(T t) throws IllegalParameterTagException {
    initializeTagMap(t.tags());
    List<Parameter> params = Arrays.asList(t.parameters());
    if(tagmapContainsParamNotListedInSignature(params)) {
      throw new IllegalParameterTagException("There are parameter tags in " + setSourcePosition(t.position()) + ", which are not listed in the signature. Remove that javadoc tag to continue.");
    }
    for(Parameter param : params) {
      //  checks wether all parameters are documented
      if(!doesTagmapContainParam(param)) {
        Log.warn("'" + param.name() + "' is not documented.", setSourcePosition(t.position()));
      }
      pw.print("_" + typeToString(param.type()) + param.type().dimension() + "_ " + param.name());
      //  if there are more parameters, prints a ", "
      if(!param.equals(params.get(params.size() - 1))) {
        pw.print(", ");
      }
    }
    pw.println(")**  ");
    if(!t.commentText().isEmpty()) {
      pw.println("_" + handle(t.commentText()) + "_");
    }
    pw.println();
    printTags(Taglist.PARAMETER);
    printTags(Taglist.RETURN);
    printTags(Taglist.INPUT);
    printTags(Taglist.OUTPUT);
    pw.println();
  }

  /**
   * Initializes the Tagmap. There are only four viable
   * annotations: @param, @return, @input and @output.
   * Other annotations will be ignored.
   * (more can be added)
   *
   * @param tags Array of all tags in the comment
   */
  private static void initializeTagMap(Tag[] tags) {
    List<Tag> params = new ArrayList<>();
    List<Tag> returns = new ArrayList<>();
    List<Tag> inputs = new ArrayList<>();
    List<Tag> outputs = new ArrayList<>();
    for(Tag tag : tags) {
      if("@param".equals(tag.name())) {
        params.add(tag);
      }
      if("@return".equals(tag.name())) {
        returns.add(tag);
      }
      if("@input".equals(tag.name())) {
        inputs.add(tag);
      }
      if("@output".equals(tag.name())) {
        outputs.add(tag);
      }
    }
    tagmap = new HashMap<>();
    tagmap.put(Taglist.PARAMETER, params);
    tagmap.put(Taglist.RETURN, returns);
    tagmap.put(Taglist.INPUT, inputs);
    tagmap.put(Taglist.OUTPUT, outputs);
  }

  /**
   * Checks wether the initialized tagmap contains the parameter from the
   * method/constructor signature.
   *
   * @param param The parameter to look for.
   * @return true, if the tagmap contains that parameter
   */
  private static boolean doesTagmapContainParam(Parameter param) {
    for(Tag tag : tagmap.get(Taglist.PARAMETER)) {
      String parameterTag = tag.text().split(" ")[0];
      if(parameterTag.equals(param.name())) {
        return true;
      }

    }
    return false;
  }

  /**
   * Checks wether the tagmap does not contain any @param tags not listed in the signature.
   *
   * @param parameterList the parameter list of the method/constructor
   * @return true, if the tagmap does contain further parameter tags
   */
  private static boolean tagmapContainsParamNotListedInSignature(List<Parameter> parameterList) {
    outerLoop:
    for(Tag tag : tagmap.get(Taglist.PARAMETER)) {
      String paramTag = tag.text().split(" ")[0];
      for(Parameter param : parameterList) {
        if(paramTag.equals(param.name())) {
          continue outerLoop;
        }
      }
      return true;
    }
    return false;
  }

  /**
   * Prints the tags.
   */
  private static void printTags(Taglist taglist) {
    if(!tagmap.get(taglist).isEmpty()) {
      pw.println("* " + taglist.getTag());
      for(Tag tag : tagmap.get(taglist)) {
        if(tagTextIsValid(tag)) {
          pw.println("  * " + handle(tag.text()));
        } else {
          Log.warn("'" + tag.name().substring(1) + "' has to be commented.", setSourcePosition(tag.position()));
        }
      }
    }
  }

  private static boolean tagTextIsValid(Tag tag) {
    if(tag.text().isEmpty()) {
      return false;
    }
    //TODO check if @param contains more than just param name
    return true;
  }

  /**
   * Substitutes e.g. every @link name annotation with [name](name).
   * This is relevant for Markdown to work properly.
   *
   * @param text The comment which can contain these annotations
   * @return the substituted text.
   */
  private static String handle(String text) {
    if(text.contains("{@link")) {
      int begin = text.indexOf("{@link");
      int end = text.indexOf("}");
      String name = text.substring(begin + "{@link".length(), end).trim();
      StringBuilder sb = new StringBuilder(text);
      sb.replace(begin, end + 1, "[" + name + "](methods/" + name + ")");
      return handle(sb.toString());
    } else if(text.contains("{@code")) {
      int begin = text.indexOf("{@code");
      int end = text.indexOf("}");
      String name = text.substring(begin + "{@code".length(), end).trim();
      StringBuilder sb = new StringBuilder(text);
      sb.replace(begin, end + 1, "```" + name + "```");
      return handle(sb.toString());
    } else {
      return text;
    }
  }

  /**
   * Prints the type of the parameter properly.
   * E.g. if its a generic.
   *
   * @param type the parameter type
   * @return the correct String form of the parameter
   */
  private static String typeToString(Type type) {
    if(type.asParameterizedType() != null) {
      ParameterizedType typo = type.asParameterizedType();
      StringBuilder ret = new StringBuilder(typo.simpleTypeName() + "\\<");
      List<Type> arguments = Arrays.asList(typo.typeArguments());
      for(Type arg : arguments) {
        ret.append(arg.simpleTypeName());
        if(!arg.equals(arguments.get(arguments.size() - 1))) {
          ret.append(", ");
        }
      }
      ret.append(">");
      return ret.toString();
    }
    return type.simpleTypeName();
  }

  /**
   * Simply converts the javadoc SourcePosition to the log SourcePosition.
   *
   * @param source SourcePosition (javadoc)
   * @return SourcePosition (Log)
   */
  private static SourcePosition setSourcePosition(com.sun.javadoc.SourcePosition source) {
    return new SourcePosition(source.line(), source.column(), source.file().getName());
  }

  /**
   * NOTE: Without this method present and returning LanguageVersion.JAVA_1_5,
   * Javadoc will not process generics because it assumes LanguageVersion.JAVA_1_1
   *
   * @return language version (hard coded to LanguageVersion.JAVA_1_5)
   */
  public static LanguageVersion languageVersion() {
    return LanguageVersion.JAVA_1_5;
  }

}
