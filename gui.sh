#!/bin/bash
env JAVACMD=java JAVA_OPTS="-Xms512m -Xmx2048m -enableassertions" scala -deprecation -classpath \
target/classes:\
$M2_REPO/log4j/log4j/1.2.14/log4j-1.2.14.jar:\
$M2_REPO/junit/junit/4.8.1/junit-4.8.1.jar  com.hartenbower.QodApp

  
