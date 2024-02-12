#!/bin/bash

if [ -z "$1" ]; then
    echo "Usage: $0 <message>"
    exit 1
fi

message=$1

curl $BOT_HOST/jobs/$CI_JOB_ID/messages \
  -X POST \
  -H "Content-Type: application/json; charset=utf-8" \
  -d "$message"
