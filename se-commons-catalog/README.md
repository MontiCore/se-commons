# SE Catalog

Simply add this to your `settings.gradle` file:

```groovy
dependencyResolutionManagement {
  repositories {
    maven {
      url "https://nexus.se.rwth-aachen.de/content/groups/public/"
    }
  }
  versionCatalogs {
    seLibs {
      from("de.se_rwth.commons:se-commons-catalog:$mc_version")
    }
  }
}
```

You can then add any library or plugin using the `seLibs` variable inside your
`gradle.build`:

```groovy
plugins {
  alias(seLibs.plugins.mc.generator)
}

dependencies {
  implementation seLibs.mc.statecharts
}
```

Read more about version catalogs in
the [Gradle documentation](https://docs.gradle.org/current/userguide/version_catalogs.html).
