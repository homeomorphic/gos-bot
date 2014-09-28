#!/bin/bash

REMOTE="$1"
BOT="$2"

rsync -aprv --rsh="ssh -i /home/jochem/random/zernike.pem" . "ec2-user@$1":~
ssh -i /home/jochem/random/zernike.pem ec2-user@$1 ./do_tourneys.sh $BOT

