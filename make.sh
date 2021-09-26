#!/bin/bash -xe
git clean -d -f -x build dist

mkdir -p build dist
javac -verbose --add-modules jdk.jdi -d build debug/*/*.java
cd build
jar cfe ../dist/jdb-plus.jar debug.tty.TTY debug/*/*.class


