#!/bin/bash

# Find all pom.xml files in subdirectories
find . -name "pom.xml" | while read -r file; do
  # Use sed to remove the specified XML entry
  sed -i '' '/<requirement>/,/<\/requirement>/ {
    /<requirement>/d
    /<type>eclipse-plugin<\/type>/d
    /<id>org.eclipse.persistence.oracle<\/id>/d
    /<versionRange>0.0.0<\/versionRange>/d
    /<\/requirement>/d
  }' "$file"
done
