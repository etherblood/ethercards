#!/bin/bash

GIT=$1
SERVER=$2
CLIENT=$3

cp ${GIT}/gui/target/gui-*[!s].jar ${CLIENT}/a.jar
rsync -rc --delete ${GIT}/gui/target/assets ${CLIENT}/
rsync -rc --delete ${GIT}/gui/target/libs ${CLIENT}/
curl https://destrostudios.com:8080/apps/5/updateFiles
echo updated client files

cd ${SERVER}
pm2 stop ethercards 2> /dev/null
echo stopped server
cp ${GIT}/game-server/target/game-server-*[!s].jar ${SERVER}/a-cards.jar
rsync -rc --delete ${GIT}/game-server/target/libs ${SERVER}/
rsync -rc --delete ${GIT}/game-server/target/assets ${SERVER}/
mv ${GIT}/releng/ecosystem.config.js ${SERVER}/ecosystem.config.js
echo updated server files
pm2 start
echo started server
