# Installation #

Planning4J is best use with [Apache Maven](http://maven.apache.org/), it may however be used without it as well, but there are quite a few dependencies to download with the project, so consider well, if you shouldn't really be using Maven for your build. If not, see below.

Currently Planning4J consists of 6 artifacts:

  * **planning4j-base** Interfaces and other base classes needed by all the other artifacts
  * **external-planners-executor** Interface to use external planners that comply with the [International Planning Competition](http://ipc.icaps-conference.org/) specifications. Refactored from [ItSimple project](http://code.google.com/p/itsimple/).
  * **external-planners-pack** A pack of compiled planners for windows and unix platforms to be run with external-planners-executor
  * **external-validation** Compiled VAL plan validator for both Windows and Linux, to be used in connection with the validation API
  * **sicstus-planners** Planners running with Sicstus Prolog, currently under development.
  * **ANA planner connector** A connector for ANA`*` JAVA Planner. See [Using ANA\*](UsingANA.md) for specific instructions.

# Maven installation #

Add AMIS repository to your project
```
        <repository>
            <id>amis-artifactory</id>
            <name>AMIS Artifactory</name>
            <url>http://diana.ms.mff.cuni.cz:8081/artifactory/repo</url>
        </repository>

```

Add dependency on Planning4J artifacts to your project:

```
        <dependency>
            <groupId>cz.cuni.amis.planning4j</groupId>
            <artifactId>planning4j-base</artifactId>
            <version>1.1</version>
        </dependency>
        <dependency>
            <groupId>cz.cuni.amis.planning4j.external</groupId>
            <artifactId>external-planners-pack</artifactId>
            <version>1.1</version>
        </dependency>
        <dependency>
            <groupId>cz.cuni.amis.planning4j.external</groupId>
            <artifactId>external-planners-executor</artifactId>
            <version>1.1</version>
        </dependency>
```

# Downloading JARs manually #

If you do not use maven, you can download the jars manually from our artifactory. The most current version (snapshot) can be found at

http://diana.ms.mff.cuni.cz:8081/artifactory/libs-snapshot-local/cz/cuni/amis/planning4j/

The current stable version can be found at

http://diana.ms.mff.cuni.cz:8081/artifactory/libs-release-local/cz/cuni/amis/planning4j/

The artifacts have quite a few dependencies (consider using Maven, it handles them automagically  ;-)). A zip with the dependencies can be found at http://diana.ms.mff.cuni.cz:8081/artifactory/libs-release-local/cz/cuni/amis/planning4j/planning4j-dependencies/ however, it is not built regularly and might get outdated.

To see an up-to-date list of dependencies see the [maven generated site](http://diana.ms.mff.cuni.cz:8080/job/Planning4J/site/) of our project. (look under artifact name -> dependencies)

Currently the most dependencies are connected to the external-planners part and those can be seen at http://diana.ms.mff.cuni.cz:8080/job/Planning4J/site/external-planners-executor/dependencies.html

If the dependencies pack is outdated, you may also try to ask nicely at [Planning4J forum](http://groups.google.com/group/planning4j-support) and it is very likely I will update the dependencies file.