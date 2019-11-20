#!/bin/bash

sshpass -p 'lejos' ssh -o KexAlgorithms=+diffie-hellman-group1-sha1 -c aes128-cbc root@10.0.1.1 'cd lejos && echo -e "lejos.keyclick_volume=0\nlejos.volume=50\nlejos.default_program=/home/lejos/programs/org.mindroid.ev3App-1.0.jar" > settings.properties && cd config && echo "USB * * 192.168.42.253 0.0.0.0 0.0.0.0 0.0.0.0 0.0.0.0" > pan.config'

sshpass -p 'lejos' scp -o KexAlgorithms=+diffie-hellman-group1-sha1 -c aes128-cbc ../../impl/ev3App/build/libs/org.mindroid.ev3App-1.0.jar root@10.0.1.1:/home/lejos/programs/

sshpass -p 'lejos' ssh -o KexAlgorithms=+diffie-hellman-group1-sha1 -c aes128-cbc root@10.0.1.1 'shutdown now'
