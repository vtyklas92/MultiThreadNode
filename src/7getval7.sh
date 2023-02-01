#!/bin/bash
java DatabaseNode -tcpport 9000 -record 1:8 &
sleep 1
java DatabaseNode -tcpport 9001 -connect localhost:9000 -record 2:7 &
sleep 1
java DatabaseNode -tcpport 9002 -connect localhost:9000 -connect localhost:9001 -record 3:6 &
sleep 1
java DatabaseNode -tcpport 9003 -connect localhost:9001 -record 4:5 &
sleep 1
java DatabaseNode -tcpport 9004 -connect localhost:9001 -connect localhost:9003 -record 5:4 &
sleep 1
java DatabaseNode -tcpport 9005 -connect localhost:9002 -connect localhost:9004 -record 6:3 &
sleep 1
java DatabaseNode -tcpport 9006 -connect localhost:9002 -connect localhost:9005 -connect localhost:9003 -record 7:1 &
sleep 1
java DatabaseClient -gateway localhost:9001 -operation get-value 7
sleep 1
java DatabaseClient -gateway localhost:9000 -operation terminate
java DatabaseClient -gateway localhost:9001 -operation terminate
java DatabaseClient -gateway localhost:9002 -operation terminate
java DatabaseClient -gateway localhost:9003 -operation terminate
java DatabaseClient -gateway localhost:9004 -operation terminate
java DatabaseClient -gateway localhost:9005 -operation terminate
java DatabaseClient -gateway localhost:9006 -operation terminate

lsof -ti:9000 | xargs kill -9
lsof -ti:9001 | xargs kill -9
lsof -ti:9002 | xargs kill -9
lsof -ti:9003 | xargs kill -9
lsof -ti:9004 | xargs kill -9
lsof -ti:9005 | xargs kill -9
lsof -ti:9006 | xargs kill -9
