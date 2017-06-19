#!/bin/bash

if uname -m > /dev/null 2>&1; then
	arch=`uname -m`
else
	arch=`uname -p`
fi
# Massage arch for Eclipse-uname differences
case $arch in
	i[0-9]*86)
		arch=x86 ;;
	ia64)
		arch=ia64 ;;
	ppc)
		arch=ppc ;;
	ppc64)
		arch=ppc ;;
	x86_64)
		arch=x86_64 ;;
	*)
	echo "ERROR: Unrecognized architecture:  $arch"
	exit 1 ;;
esac
echo $arch