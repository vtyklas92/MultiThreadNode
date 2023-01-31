#!/bin/bash

# Start 3 network nodes, then terminate them
java DatabaseNode -tcpport 9000 -record 1:1 &
sleep 1
java DatabaseNode -tcpport 9001 -connect localhost:9000 -record 2:2 &
sleep 1
java DatabaseNode -tcpport 9002 -connect localhost:9000 -connect localhost:9001 -record 3:3 &
sleep 1
java DatabaseClient -gateway localhost:9000 -operation get-value 3
java DatabaseClient -gateway localhost:9000 -operation terminate
java DatabaseClient -gateway localhost:9001 -operation terminate
java DatabaseClient -gateway localhost:9002 -operation terminate
lsof -ti:9000 | xargs kill -9
lsof -ti:9001 | xargs kill -9
lsof -ti:9002 | xargs kill -9