#!/bin/bash

EC2S=`cat ec2s`;

echo "** Killing java"
for ec2 in $EC2S; do
    ssh $ec2 killall java
done

echo "** git commit"
git commit -am"testing bot"
HEAD=`git log -1 --format=%h`
echo "** build"
./make.sh
TARGET=bot-$HEAD.jar
mv bot.jar $TARGET

echo "** starting competition for $TARGET"
for ec2 in $EC2S; do
    ./run_tourney_on.sh $ec2 $TARGET > out-$ec2 &
done

tail -f out-*amazonaws.com


