# fart-maven-plugin

A Maven plugin that plays fart sounds while your build scrolls by.

Inspired by [fartscroll.js](https://github.com/theonion/fartscroll.js) and modeled after [music-maven-plugin](https://github.com/xdev-software/music-maven-plugin).

> "Everyone farts. And now your builds can too."

The plugin bundles all 10 original fart sounds from fartscroll.js and plays them at random intervals during your Maven build.

## Requirements

- Java 17+
- Maven 3.9+
- A system with audio output (and the courage to use it)

## Build

```bash
git clone https://github.com/dogeared/fart-maven-plugin.git
cd fart-maven-plugin
mvn clean install
```

This installs the plugin to your local Maven repository.

## Usage

### Quick test

Run the `fart` goal directly:

```bash
mvn dev.dogeared:fart-maven-plugin:fart validate
```

### Add to a project

Add the plugin to your project's `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>dev.dogeared</groupId>
            <artifactId>fart-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <executions>
                <execution>
                    <goals>
                        <goal>fart</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

Then run any build:

```bash
mvn clean compile
```

A fart plays immediately when the plugin activates, then continues at random intervals throughout the build.

### Stop farts at a specific phase

Bind the `stop-fart` goal to a later phase to silence farts partway through:

```xml
<executions>
    <execution>
        <id>start</id>
        <goals>
            <goal>fart</goal>
        </goals>
    </execution>
    <execution>
        <id>stop</id>
        <phase>verify</phase>
        <goals>
            <goal>stop-fart</goal>
        </goals>
    </execution>
</executions>
```

## Configuration

| Parameter | Property | Default | Description |
|-----------|----------|---------|-------------|
| `skip` | `fart.skip` | `false` | Disable fart playback |
| `minInterval` | `fart.minInterval` | `500` | Minimum milliseconds between farts |
| `maxInterval` | `fart.maxInterval` | `500` | Maximum milliseconds between farts |
| `volume` | `fart.volume` | `0.8` | Volume level (0.0 to 1.0) |

### Examples

Skip farts from the command line:

```bash
mvn clean install -Dfart.skip=true
```

Crank up the frequency:

```bash
mvn clean install -Dfart.minInterval=500 -Dfart.maxInterval=2000
```

Configure in `pom.xml`:

```xml
<plugin>
    <groupId>dev.dogeared</groupId>
    <artifactId>fart-maven-plugin</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <configuration>
        <volume>0.5</volume>
        <minInterval>1000</minInterval>
        <maxInterval>5000</maxInterval>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>fart</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Goals

| Goal | Default Phase | Description |
|------|---------------|-------------|
| `fart` | `validate` | Starts playing fart sounds |
| `stop-fart` | *(none)* | Stops fart playback |

## Sound Credits

Fart sounds sourced from [fartscroll.js](https://github.com/theonion/fartscroll.js) by The Onion. Original audio from SoundDogs / Bjorn Lynne FX.

## License

Apache-2.0
