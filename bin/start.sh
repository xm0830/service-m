#!/usr/bin/env bash

DIR=$(cd $(dirname $0); pwd)
HOME=$(cd $(dirname $DIR); pwd)

if [ -f "$HOME/data/process.pid" ]; then
  TARGET_PID=$(cat "$HOME/data/process.pid")
  if kill -0 $TARGET_PID > /dev/null 2>&1; then
    echo process already started!
    exit 0
  fi
fi

nohup java -cp $CLASSPATH:$HOME/lib/* com.rainbow.manager.main.ServiceManager $HOME >/dev/null 2>&1 &

echo $! > "$HOME/data/process.pid"