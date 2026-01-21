#!/bin/bash

# Script to enforce tycho to build new qualifiers for bundles/features.
# It creates iteratively dummy file(s) within the bundle root and it commits the
# new file(s). Subsequently the script remove all dummy files
# and commits the changes again. Due to the new commit tycho will 
# create new qualifiers. 

# Step1 (dummy file(s) creation/first commit)
for dir in ../bundles/*/
do
dir=${dir%*/}
echo "${dir##*/}"
echo $dir
touch $dir/bumpit
echo "Add string..." > $dir/bumpit
git add $dir/bumpit
done
git commit -m "Added dummy file"

# Step2 (dummy file(s) removal/second commit)
for dir in ../bundles/*/
do
dir=${dir%*/}
echo "${dir##*/}"
echo $dir
git rm $dir/bumpit
done
git commit -m "Removed dummy file to each bundle"

