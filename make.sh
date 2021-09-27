#!/bin/bash -xe
git clean -d -f -x build dist

export PATH="/c/Program Files/Amazon Corretto/jdk11.0.8_10/bin:$PATH"

mkdir -p build dist
javac --add-modules jdk.jdi -d build debug/*/*.java
cd build
jar cfe ../dist/jdb-plus.jar debug.tty.TTY debug/*/*.class


