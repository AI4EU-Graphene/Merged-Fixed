#!/bin/bash
export JAVA_HOME=/lib/jvm/java-1.8.0-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
export SPRING_APPLICATION_JSON=$(cat application.json)
mvn spring-boot:run
