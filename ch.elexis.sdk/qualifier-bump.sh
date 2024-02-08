#!/bin/bash
for dir in ../bundles/*/
do
dir=${dir%*/}
echo "${dir##*/}"
echo $dir
touch $dir/bumpit
echo "Add string..." > $dir/bumpit
git add $dir/bumpit
#git commit -m "Added dummy file"
#git rm $dir/bumpit
#git commit -m "Removed dummy file"
done
git commit -m "Added dummy file to each bundle"

