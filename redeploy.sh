#!/bin/bash

mvn clean package -Dmaven.javadoc.skip=true -Dmaven.test.skip=true

SRC=target/Console.war

DEST=~/softwares/ant-media-server/webapps/


rm -rf $DEST/Console
cp  $SRC  $DEST



#go to red5 dir
cd ~/softwares/ant-media-server

#shutdown red5 
./stop.sh


#start red5
./start-debug.sh

