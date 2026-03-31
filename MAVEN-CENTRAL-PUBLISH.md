# Publishing to Maven Central

Steps to publish `dev.dogeared:fart-maven-plugin` to Maven Central.

## 1. Register a namespace on Central Portal

1. Go to **https://central.sonatype.com** and sign in (GitHub login works)
2. Go to **Namespaces** and claim `dev.dogeared`
3. Verify domain ownership by adding a TXT DNS record they provide to the `dogeared.dev` domain
4. Wait for verification (usually minutes)

## 2. Generate a GPG signing key

```bash
gpg --gen-key
```

Then publish your public key to a keyserver:

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys <YOUR_KEY_ID>
```

NOTE: Alternate key servers are:
* pgp.mit.edu
* keys.openpgp.org

Note your key ID — you'll need it in step 4.

## 3. Generate a Central Portal token

1. On **https://central.sonatype.com**, go to your account → **Generate User Token**
2. You'll get a `username` and `password` token pair

## 4. Configure `~/.m2/settings.xml`

```xml
<settings>
  <servers>
    <server>
      <id>central</id>
      <username>YOUR_TOKEN_USERNAME</username>
      <password>YOUR_TOKEN_PASSWORD</password>
    </server>
  </servers>
  <profiles>
    <profile>
      <id>central</id>
      <properties>
        <gpg.keyname>YOUR_GPG_KEY_ID</gpg.keyname>
      </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>central</activeProfile>
  </activeProfiles>
</settings>
```

## 5. Add required POM metadata

The `pom.xml` needs these elements added (required by Central):

```xml
<url>https://github.com/dogeared/fart-maven-plugin</url>

<licenses>
  <license>
    <name>The Apache License, Version 2.0</name>
    <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
  </license>
</licenses>

<developers>
  <developer>
    <name>Micah Silverman</name>
    <email>micah@dogeared.dev</email>
    <organization>dogeared.dev</organization>
    <organizationUrl>https://dogeared.dev</organizationUrl>
  </developer>
</developers>

<scm>
  <connection>scm:git:git://github.com/dogeared/fart-maven-plugin.git</connection>
  <developerConnection>scm:git:ssh://github.com:dogeared/fart-maven-plugin.git</developerConnection>
  <url>https://github.com/dogeared/fart-maven-plugin</url>
</scm>
```

## 6. Add publishing plugins to `pom.xml`

Add these to the `<build><plugins>` section:

```xml
<!-- Source JAR (required) -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-source-plugin</artifactId>
  <version>3.3.1</version>
  <executions>
    <execution>
      <id>attach-sources</id>
      <goals><goal>jar-no-fork</goal></goals>
    </execution>
  </executions>
</plugin>

<!-- Javadoc JAR (required) -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-javadoc-plugin</artifactId>
  <version>3.11.2</version>
  <executions>
    <execution>
      <id>attach-javadocs</id>
      <goals><goal>jar</goal></goals>
    </execution>
  </executions>
</plugin>

<!-- GPG signing (required) -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-gpg-plugin</artifactId>
  <version>3.2.7</version>
  <executions>
    <execution>
      <id>sign-artifacts</id>
      <phase>verify</phase>
      <goals><goal>sign</goal></goals>
    </execution>
  </executions>
</plugin>

<!-- Central Publishing Plugin -->
<plugin>
  <groupId>org.sonatype.central</groupId>
  <artifactId>central-publishing-maven-plugin</artifactId>
  <version>0.6.0</version>
  <extensions>true</extensions>
  <configuration>
    <publishingServerId>central</publishingServerId>
    <autoPublish>true</autoPublish>
  </configuration>
</plugin>
```

## 7. Set the release version

Remove `-SNAPSHOT` from the version:

```xml
<version>1.0.0</version>
```

## 8. Deploy

```bash
mvn clean deploy
```

This will build, sign, and upload the bundle to Central Portal. If `autoPublish` is `true`, it will automatically publish after validation passes. Otherwise, log in to **https://central.sonatype.com** and click **Publish** on the deployment.

## 9. Verify

After publishing, the artifact will be available at:

```
https://central.sonatype.com/artifact/dev.dogeared/fart-maven-plugin
```

It may take ~30 minutes to appear in search and be resolvable by Maven.

## Checklist

- [X] Namespace `dev.dogeared` verified on central.sonatype.com
- [X] GPG key created and published
- [X] Token configured in `~/.m2/settings.xml`
- [X] POM updated with `url`, `licenses`, `developers`, `scm`, and the 4 plugins above
- [X] Version set to `1.0.0` (non-SNAPSHOT)
