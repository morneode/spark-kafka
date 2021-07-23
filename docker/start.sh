#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
echo $DIR
echo $BASH_SOURCE
cd $DIR
docker-compose down
docker-compose up -d
#docker-compose up
