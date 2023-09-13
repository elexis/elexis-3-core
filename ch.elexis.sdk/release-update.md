How to prepare a new Elexis release

*Framsteg GmbH\<admin@framsteg.ch\>* Last update: 14.9.2023

# Preparations
- cd /PATH/TO/ELEXIS_CORE
# Derive branch
- git branch master
- git branch X.Y (whereas X stands for 3 and Y for 10/11/12 etc.)
- git checkout X.Y
# Adjust MANIFEST.MF
- find . -name "MANIFEST.MF" -type f -exec grep -H 'Bundle-Version: 3.XX.0.qualifier' {} +
- Check affected files (result=x1)
- find -name "MANIFEST.MF" -type f -exec sed -i 's/Bundle-Version: 3.XX.0.qualifier/Bundle-Version: 3.YY.0.qualifier/g' {} \;
- find . -name "MANIFEST.MF" -type f -exec grep -H 'Bundle-Version: 3.XX.0.qualifier' {} + 
- Check affected files (result should be zero)
- find . -name "MANIFEST.MF" -type f -exec grep -H 'Bundle-Version: 3.YY.0.qualifier' {} +
- Check affected files (result=x1)
# Adjust pom.xml
- find . -name "pom.xml" -type f -exec grep -H '3.XX.0-SNAPSHOT' {} +
- Check affected files (result=x2)
- find -name "pom.xml" -type f -exec sed -i 's/3.XX.0-SNAPSHOT/3.YY.0-SNAPSHOT/g' {} \;
- find . -name "pom.xml" -type f -exec grep -H '3.XX.0-SNAPSHOT' {} +
- Check affected files (result should be zero)
- find . -name "pom.xml" -type f -exec grep -H '3.YY.0-SNAPSHOT' {} +
- Check affected files (result=x2)
# Adjust feature.xml
- find . -name "feature.xml" -type f -exec grep -H '3.XX.0.qualifier' {} + 
- Check affected files (result=x3)
- find -name "feature.xml" -type f -exec sed -i 's/3.XX.0.qualifier/3.YY.0.qualifier/g' {} \;
- find . -name "feature.xml" -type f -exec grep -H '3.XX.0.qualifier' {} +
- Check affected files (result should be zero)
- find . -name "feature.xml" -type f -exec grep -H '3.YY.0.qualifier' {} + 
- Check affected files (result=x3)
# Adjust Elexis.product
- vi ch.elexis.core.p2site/Elexis.product
- Adjust <product name="Elexis OpenSource" uid="Elexis3" id="ch.elexis.core.application.product" application="ch.elexis.core.application.ElexisApp" version="3.XX.0.qualifier" useFeatures="true" includeLaunchers="true" autoIncludeRequirements="true">
- Adjust Elexis 3.X - www.elexis.info
- Copyright 2005-XXXX G. Weirich und Elexis-Team
# Adjust splash image
- Open the file bundles/ch.elexis.core.product/splash.xcf
- Choose the layer containing the release number
- Adjust the number
- Save the xcf file
- Export as splash.bmp
# Start build cycle
- mvn -V clean verify -Dtycho.localArtifacts=ignore -DskipTests 
