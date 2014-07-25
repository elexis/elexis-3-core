# This Dockerfile is used to build an image containing basic stuff to be used as a Jenkins slave build node.
FROM ngiger/baseimage-wheezy:0.2
MAINTAINER Niklaus Giger "niklaus.giger@member.fsf.org"

ENV DEBIAN_FRONTEND noninteractive

# http://testing.bredex.de/jubula-download-page.html?file=files/content_closed/software/installer-jubula_linux-gtk-x86_64.sh
RUN mkdir -p /opt/downloads/jubula_8.0/
ADD installer-jubula_linux-gtk-x86_64.sh /opt/downloads/jubula_8.0/installer-jubula_linux-gtk-x86_64.sh
ADD responses.txt /opt/downloads/jubula_8.0/responses.txt

RUN mkdir -p    /build
ADD install.sh  /build/
RUN /build/install.sh

