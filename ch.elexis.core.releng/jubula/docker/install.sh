#!/bin/bash -v
# Here we install all the stuff needed to run a Jenkins-CI slave to 
# build elexis and/or run the Jubula GUI tests

apt-get update && apt-get  upgrade -y

# Install JDK 7 (latest edition)
apt-get install -y --no-install-recommends curl expect git maven net-tools openjdk-7-jdk ruby sudo vim xvfb locales-all xclip
apt-get remove --purge openjdk-6-jre-headless:amd64 --yes
# Add user jenkins to the image
adduser --quiet jenkins

# Set password for the jenkins user (you may want to alter this).
echo "jenkins:jenkins" | chpasswd
cd  /opt/downloads/jubula_8.0/ && cat responses.txt | ./installer-jubula_linux-gtk-x86_64.sh

# Install some ruby gems we might need
gem install --no-ri --no-rdoc bundler xml-simple
gem install --no-ri --no-rdoc rubyzip --version=0.9.9

# Clean Jubula download
rm -rf /opt/downloads

# Clean up APT when done.
apt-get clean && rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* 
