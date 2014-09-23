#!/bin/bash
BOT1=$1
BOT2=bot-v5.jar

java -cp out/production/gos-bot/:lib/gson-2.3.jar gos.bot.tourney.Tourney "$BOT1" "$BOT2"
#java -cp out/production/gos-bot/:lib/gson-2.3.jar gos.bot.tourney.Tourney "$BOT1" "$BOT2" > tourney2 &
#java -cp out/production/gos-bot/:lib/gson-2.3.jar gos.bot.tourney.Tourney "$BOT1" "$BOT2" > tourney3 &
#java -cp out/production/gos-bot/:lib/gson-2.3.jar gos.bot.tourney.Tourney "$BOT1" "$BOT2" > tourney4 &
