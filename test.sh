#!/bin/bash -xe

export PATH="/c/Program Files/Amazon Corretto/jdk11.0.8_10/bin:$PATH"

java -agentlib:jdwp=transport=dt_socket,server=y,address=127.0.0.1:8888,suspend=n -jar ...
