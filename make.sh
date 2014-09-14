#!/bin/bash
ant jar
rm -f bot.jar
mv build/jar/java-bot.jar bot.jar
zip bot.zip bot.jar
