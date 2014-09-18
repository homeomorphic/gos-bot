java -cp out/production/gos-bot/:lib/gson-2.3.jar gos.bot.tourney.Tourney bot-master.jar bot-v2.jar > tourney1 &
java -cp out/production/gos-bot/:lib/gson-2.3.jar gos.bot.tourney.Tourney bot-master.jar bot-v2.jar > tourney2 &
java -cp out/production/gos-bot/:lib/gson-2.3.jar gos.bot.tourney.Tourney bot-master.jar bot-v2.jar > tourney3 &
java -cp out/production/gos-bot/:lib/gson-2.3.jar gos.bot.tourney.Tourney bot-master.jar bot-v2.jar > tourney4 &

tail -f tourney{1,2,3,4}
