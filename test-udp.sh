#!/usr/bin/env bash

echo "message" | nc -w 1 -u 127.0.0.1 1234
