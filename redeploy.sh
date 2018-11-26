#!/bin/bash

mvn clean install -Dmaven.javadoc.skip=true -Dmaven.test.skip=true -Dgpg.skip=true

SRC=target/root.war

DEST=~/softwares/ant-media-server/webapps/


rm -rf $DEST/ConsoleApp $DEST/root
cp  $SRC  $DEST



#go to red5 dir
cd ~/softwares/ant-media-server

#shutdown red5 
./stop.sh


#start red5
./start-debug.sh

