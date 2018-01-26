PARENT POM
==========

This directory contains the jMCS parent pom.
It may be used for any other project that want to use it as parent.

Proceed first to its installation.

### Install:

```bash
cd parent-pom
mvn -Dassembly.skipAssembly -Djarsigner.skip=true clean install
```

### Use:

```xml
<parent>
    <groupId>fr.jmmc</groupId>
    <artifactId>jmmc</artifactId>
    <version>TRUNK</version>
</parent>
```

By default parent pom sign jar of classes. To skip this operation (developer profile), please set the *jarsigner.skip=true* property.
Signing step requires to prepare a keystore and some properties (see below) to make signing process valid
Else you will have :

```xml
<settings>
    <!-- offline
    | Determines whether maven should attempt to connect to the network when executing a build.
    | This will have an effect on artifact downloads, artifact deployment, and others.
    |
    | Default: false
    -->
    <offline>false</offline>

    <proxies>
        <proxy>
            <active>false</active>
            <protocol>http</protocol>
            <host>www-cache.ujf-grenoble.fr</host>
            <port>3128</port>
            <nonProxyHosts>*.jmmc.fr</nonProxyHosts>
        </proxy>
    </proxies>
...

    <profiles>
        <profile>
            <id>dev</id>
            <properties>
		<!-- disable jar signer -->
                <jarsigner.skip>true</jarsigner.skip>
		<!-- disable javadoc -->
		<maven.javadoc.skip>true</maven.javadoc.skip>
		<!-- disable tests -->
                <maven.test.skip>true</maven.test.skip>
            </properties>
        </profile>

        <profile>
            <id>deployer</id>
            <properties>
                <jarsigner.skip>false</jarsigner.skip>
                <jarsigner.alias>codesigningcert</jarsigner.alias>
                <jarsigner.keystore>/home/MCS/etc/globalsign.jks</jarsigner.keystore>
                <jarsigner.keypass>XXXXXX</jarsigner.keypass>
                <jarsigner.storepass>XXXXXX</jarsigner.storepass>
<!--
                <jarsigner.tsa>tsa server url</jarsigner.tsa>
-->
<!-- enable timestamping but globalsign tsa server is too slow so left disabled. -->
<!--
                <jarsigner.tsa>http://timestamp.globalsign.com/scripts/timestamp.dll</jarsigner.tsa>
-->
            </properties>
        </profile>
    </profiles>

    <activeProfiles>
        <activeProfile>dev</activeProfile>
    </activeProfiles>

...
</settings>
```

