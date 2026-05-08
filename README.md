# Nginx Config Formatter

An IntelliJ IDEA plugin that formats and beautifies nginx configuration files.

## Features

- Formats nginx blocks with four-space indentation.
- Preserves full-line and inline `#` comments.
- Keeps `#` characters inside quoted strings as normal text.
- Adds a `Code | Format Nginx Config` action for the current editor.
- Reuses the same shortcut as IntelliJ IDEA's `Code | Reformat Code` action. On macOS, press `Option+Command+L` in an nginx configuration file to run this formatter.

## Compatibility

- IntelliJ IDEA 2026.1.1
- IntelliJ Platform build `261.*`
- Java 21 bytecode for the plugin
- Java 17+ runtime for Gradle, Java 21 recommended

## Repository Contents

This directory contains the complete plugin source code and build metadata. It is ready to maintain in GitHub.

Recommended files to commit:

- `gradlew`, `gradlew.bat`, and `gradle/wrapper/` - Gradle Wrapper files so contributors do not need a system Gradle install.
- `gradle.properties` - plugin version, IntelliJ Platform version, and build range settings.
- `src/main/java/` - plugin action and nginx formatter implementation.
- `src/main/resources/META-INF/plugin.xml` - IntelliJ plugin metadata and compatibility range.
- `src/test/java/` - formatter behavior tests.
- `build.gradle.kts` and `settings.gradle.kts` - standard Gradle build configuration.
- `.github/workflows/build.yml` - GitHub Actions workflow for testing, verifying, and packaging the plugin.
- `scripts/build-plugin.sh` - local packaging script that uses an installed IntelliJ IDEA app.
- `.gitignore` - excludes local and generated files from Git.
- `README.md` - project documentation.

Generated files can be ignored for normal source maintenance:

- `build/`
- `dist/`

## Build

There are three useful build paths. Use the Gradle build for reproducible releases and CI. Use the local IDEA script when you only need a quick local zip.

### Requirements

- JDK 17 or newer available on `PATH` or through `JAVA_HOME`.
- JDK 21 is recommended because the plugin targets Java 21 bytecode.
- Internet access on the first standard Gradle build so Gradle can download the wrapper distribution and IntelliJ Platform dependencies.

### Option 1: Standard Gradle Build

Recommended for GitHub, CI, and contributors who want a reproducible build:

```bash
./gradlew buildPlugin
```

The installable plugin zip is produced under `build/distributions/` with this naming format:

```text
nginx-config-formatter-<pluginVersion>-<platformType>-<platformVersion>.zip
```

For the current settings, that is:

```text
build/distributions/nginx-config-formatter-0.0.1-IU-2026.1.1.zip
```

If Gradle 9.0+ is installed and available in `PATH`, you can also use the system Gradle command:

```bash
gradle buildPlugin
```

You can verify the installed Gradle version with:

```bash
gradle --version
```

This build uses:

```kotlin
useInstaller = false
```

That means Gradle does not download the macOS `.dmg` installer, but it still downloads IntelliJ Platform archive artifacts such as:

```text
idea-2026.1.1.zip
```

Those artifacts contain the IntelliJ Platform jars needed to compile and package the plugin. The first run can be slow because these files are large. Later runs use the Gradle cache.

If there is no system Gradle command and Gradle cannot be downloaded automatically, but you already have the distribution zip, use:

```bash
./scripts/build-with-local-gradle.sh /path/to/gradle-9.5.0-bin.zip buildPlugin
```

For example:

```bash
./scripts/build-with-local-gradle.sh /Volumes/typercode/tools/gradle-9.5.0-bin.zip buildPlugin
```

This only supplies Gradle itself. It does not replace the IntelliJ Platform dependency download.

Common development commands:

```bash
./gradlew test
./gradlew runIde
./gradlew buildPlugin
./gradlew verifyPlugin
```

The IntelliJ Platform target and compatibility range are configured in `gradle.properties`:

```properties
platformType=IU
platformVersion=2026.1.1
pluginSinceBuild=261
pluginUntilBuild=261.*
gradleWrapperVersion=9.5.0
```

### Option 2: Fast Local Zip From Installed IDEA

Use this when you have IntelliJ IDEA installed locally and only need a quick zip for local installation/testing:

```bash
./scripts/build-plugin.sh
```

This script uses:

```text
/Applications/IntelliJ IDEA.app/Contents
```

It compiles against the jars from that installed IDEA app and produces:

```text
dist/nginx-config-formatter-0.0.1-IU-2026.1.1.zip
```

This path is fast because it does not use Gradle, does not download IntelliJ Platform artifacts, and does not run plugin verification. It is useful for local smoke testing, but not ideal as the main GitHub/CI build.

If IntelliJ IDEA is installed somewhere else, set `IDEA_HOME`:

```bash
IDEA_HOME="/path/to/IntelliJ IDEA.app/Contents" ./scripts/build-plugin.sh
```

### Option 3: Gradle With A Local IDEA SDK

Gradle can also be configured to use an installed IntelliJ IDEA as the target platform instead of downloading `idea-2026.1.1.zip`.

Conceptually, the dependency would use:

```kotlin
dependencies {
    intellijPlatform {
        local("/Applications/IntelliJ IDEA.app/Contents")
        pluginVerifier()
    }
}
```

This is useful for local development, but it depends on a machine-specific path and installed IDEA version, so it is not the default repository configuration. For GitHub and CI, prefer Option 1.

The repository currently keeps this behavior as a documented option rather than enabling it by default.

## GitHub Maintenance

- Commit source files, Gradle Wrapper files, and documentation.
- Do not commit generated `build/` or `dist/` directories.
- Publish release zips from `build/distributions/` as GitHub Release assets.
- For CI, run `./gradlew test buildPlugin verifyPlugin`.

## Local Installation

1. Open IntelliJ IDEA.
2. Go to `Settings | Plugins`.
3. Click the gear icon.
4. Choose `Install Plugin from Disk...`.
5. Select the generated zip file.
6. Restart IntelliJ IDEA when prompted.

After installation, open an nginx config file and run `Code | Format Nginx Config`.

You can also use the standard IntelliJ IDEA formatting shortcut:

```text
Option+Command+L
```

The plugin reuses IDEA's `Code | Reformat Code` shortcut and promotes the nginx formatter for nginx configuration files, so the same muscle memory works for normal code formatting and nginx config formatting.
