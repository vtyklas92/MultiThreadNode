#!/bin/bash

# Start 2 network nodes, then terminate them
java DatabaseNode -tcpport 9000 -record 1:1 &
sleep 1
java DatabaseNode -tcpport 9001 -connect localhost:9000 -record 2:2 &
sleep 1
java DatabaseClient -gateway localhost:9000 -operation get-value 2
java DatabaseClient -gateway localhost:9000 -operation terminate
java DatabaseClient -gateway localhost:9001 -operation terminate
lsof -ti:9000 | xargs kill -9
lsof -ti:9001 | xargs kill -9