#!/usr/bin/env bash

DIR=$(cd $(dirname $0); pwd)
HOME=$(cd $(dirname $DIR); pwd)

nohup java -cp $CLASSPATH:$HOME/lib/* com.rainbow.manager.main.ServiceManager $HOME >/dev/null 2>&1 &