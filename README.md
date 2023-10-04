<!-- (c) https://github.com/MontiCore/monticore -->
# SE Commons
SE Commons is a collection of utilities and tools. It is among others used 
development of [MontiCore](https://www.monticore.de). 
However, these libraries can be used independently from MontiCore.
SE Commons comprises the following components:

* **se-commons-utilities**
  * utility classes such as SourceCodePosition, CLIArguments, and helpers
  for handling (qualified or lists of) names or Strings.
* **se-commons-logging**
  * Logging utilities used by MontiCore and its derivates
* **se-commons-groovy** 
  * helpers to interpret Groovy scripts such as the GroovyRunner and GroovyInterpreter.
* **se-commons-montitoolbox**
  * Java Doc doclet that produces Wiki pages in Markdown format
  that can be uploaded to
    the [MontiToolBox Wiki](https://git.rwth-aachen.de/monticore/MontiToolBox/wikis/home).
    Further information on MontiToolBox can be found [here](https://git.rwth-aachen.de/monticore/MontiToolBox/wikis/home).  

## Licenses
* [LGPL V3.0](https://github.com/MontiCore/monticore/tree/master/00.org/Licenses/LICENSE-LGPL.md) (for handwritten Java code)
* [BSD-3-Clause](https://github.com/MontiCore/monticore/tree/master/00.org/Licenses/LICENSE-BSD3CLAUSE.md) (for templates and all generated artifacts)
* Allows us free use of the software.

## Build
Please make sure that your complete workspace only uses UNIX line endings (LF) and all files are UTF-8 without BOM.
On Windows you should configure git to not automatically replace LF with CRLF during checkout by executing the following configuration:

    git config --global core.autocrlf input

## Further Information

* [Project root: MontiCore @github](https://github.com/MontiCore/monticore)
* [MontiCore documentation](https://www.monticore.de/)
* [**List of languages**](https://github.com/MontiCore/monticore/blob/opendev/docs/Languages.md)
* [**MontiCore Core Grammar Library**](https://github.com/MontiCore/monticore/blob/opendev/monticore-grammar/src/main/grammars/de/monticore/Grammars.md)
* [Best Practices](https://github.com/MontiCore/monticore/blob/opendev/docs/BestPractices.md)
* [Publications about MBSE and MontiCore](https://www.se-rwth.de/publications/)
* [Licence definition](https://github.com/MontiCore/monticore/blob/master/00.org/Licenses/LICENSE-MONTICORE-3-LEVEL.md)

