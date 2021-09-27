#!/bin/bash -xe

export PATH="/c/Program Files/Amazon Corretto/jdk11.0.8_10/bin:$PATH"

java -jar dist/jdb-plus.jar -connect com.sun.jdi.SocketAttach:hostname=127.0.0.1,port=8888
