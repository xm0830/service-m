#!/usr/bin/env bash

DIR=$(cd $(dirname $0); pwd)
HOME=$(cd $(dirname $DIR); pwd)

if [ -f "$HOME/data/process.pid" ]; then
  TARGET_PID=$(cat "$HOME/data/process.pid")
  if kill -0 $TARGET_PID > /dev/null 2>&1; then
    echo process will stop after all runnint service completed!
    kill $TARGET_PID
  else
    echo no process to stop
  fi
else
  echo no process to stop
fi
