#!/bin/bash

#!!!!run this with sudo!!!!
#Use this script to install all npm karma dependencies to run karma tests.
#Before using this script, make sure that nodejs and npm are installed.
#sudo apt-get install nodejs npm

#If nodejs can't be found by karma, take a look at https://github.com/nodejs/node-v0.x-archive/issues/3911
#"I've found this is often a misnaming error, if you install from a package manager you bin may be called nodejs so you just need to symlink it like so "ln -s /usr/bin/nodejs /usr/bin/node" "

npm install karma -g
npm install karma-jasmine@2_0 -g
npm install karma-remote-reporter -g
npm install karma-phantomjs-launcher -g
npm install karma-cli -g
npm install karma-junit-reporter -g
