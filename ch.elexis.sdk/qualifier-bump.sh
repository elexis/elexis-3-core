#!/bin/bash
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
for dir in ../bundles/*/
do
dir=${dir%*/}
echo "${dir##*/}"
echo $dir
git rm $dir/bumpit
done
git commit -m "Removed dummy file to each bundle"
