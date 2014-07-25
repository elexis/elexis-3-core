#!/bin/bash -v
set -e # exit on error
sleep 1
mkdir /tmp/tst
mkdir /tmp/home
export HOME=/tmp/home
export DISPLAY=0:0
export LANG=de_CH.UTF-8
export LANGUAGE=de_CH:de
/usr/bin/Xvfb -screen 0 1280x1024x24 -fbdir /tmp/tst &
sleep 1
cd /opt/elexis-3-core/ch.elexis.core.releng/jubula && ./run_jenkins.rb
