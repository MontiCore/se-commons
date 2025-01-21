# SE-Codestyle

This plugin handles code formatting for the chair's Gradle-based projects and
maintains the `se-codestyle` configuration for IDEs. It employs the code
formatter [spotless][spotless] and provides the SE-specific configuration.

## Applying the Plugin

You can apply the plugin by adding

```
id 'de.se_rwth.codestyle' version '7.7.0'
```

to Gradle's plugin section. Gradle's `check` task depends on the
added `spotlessCheck` task that verifies the correct formatting of the code.
Gradle's `build` task depends on the added `spotlessApply` that reformats the
code.

Running the plugin extracts the `se-codestyle-eclipse.xml` that configures
spotless and the `.editorconfig` files to the root of the Gradle project.
Therefore, add it to the XML file to the repository's .gitignore. You should
configure IntelliJ such that it formats files according to
the [.editorconfig][editorconfigIntelliJ].

> You may have to unfocus your IDE's window to reload the .editorconfig after
> changes.

Spotless allows a [ratchet mode][ratchet] that applies the formatting only to
modified files. However, we recommend doing a reformatting commit.

## Codestyle

The config files in [se-codestyle][codestyle] define the complete codestyle.
This section gives only an overview of the most basic rules.

* Indent Size: `2`
* Indent Style: `Space`
* Max line length for code: `100`
* Max line length for continuous text: `80`
* Wrap if long: `true`
* New Final Line: `true`
* End of Line: `lf`
* Toggle the formatter off: `@formatter:off`
* Toggle the formatter on: `@formatter:on`

## Updating the Codestyle

The [`se-codestyle-eclipse.xml`][eclipse] defines codestyle for Java. Spotless
and the IntelliJ codestyle [`se-codestyle-intellij.xml`][intellij] depend on it.
Furthermore, the IntelliJ configuration defines the codestyle for
other file formats. The [`.editorconfig`][editorconfig] is the exported variant.

[editorconfigIntelliJ]: https://www.jetbrains.com/help/idea/editorconfig.html

[spotless]: https://github.com/diffplug/spotless

[ratchet]: https://github.com/diffplug/spotless/tree/main/plugin-gradle#ratchet

[codestyle]: src/main/resources/se-codestyle

[eclipse]: src/main/resources/se-codestyle/se-codestyle-eclipse.xml

[intellij]: src/main/resources/se-codestyle/se-codestyle-intellij.xml

[editorconfig]: src/main/resources/se-codestyle/.editorconfig
