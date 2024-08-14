find . -name "eclipse.jdt.core.prefs" -type f -exec grep -H 'org.eclipse.jdt.core.compiler.codegen.targetPlatform=11' {} +

find . -name "eclipse.jdt.core.prefs" -type f -exec grep -H 'org.eclipse.jdt.core.compiler.compliance=11' {} +

find . -name "eclipse.jdt.core.prefs" -type f -exec grep -H 'org.eclipse.jdt.core.compiler.source=11' {} +

find . -name ".classpath" -type f -exec grep -H 'JavaSE-11' {} +

find . -name "MANIFEST.MF" -type f -exec grep -H 'JavaSE-11' {} +

find -name "org.eclipse.jdt.core.prefs" -type f -exec sed -i 's/org.eclipse.jdt.core.compiler.codegen.targetPlatform=11/org.eclipse.jdt.core.compiler.codegen.targetPlatform=17/g' {} \;

find -name "org.eclipse.jdt.core.prefs" -type f -exec sed -i 's/org.eclipse.jdt.core.compiler.compliance=11/org.eclipse.jdt.core.compiler.compliance=17/g' {} \;

find -name "org.eclipse.jdt.core.prefs" -type f -exec sed -i 's/org.eclipse.jdt.core.compiler.source=11/org.eclipse.jdt.core.compiler.source=17/g' {} \;

find -name "org.eclipse.jdt.core.prefs" -type f -exec sed -i 's/org.eclipse.jdt.core.compiler.compliance=11/org.eclipse.jdt.core.compiler.compliance=17/g' {} \;

find -name ".classpath" -type f -exec sed -i 's/JavaSE-11/JavaSE-17/g' {} \;

find -name "MANIFEST.MF" -type f -exec sed -i 's/JavaSE-11/JavaSE-17/g' {} \;

# MAC

find . -name "MANIFEST.MF" -type f -exec sed -i '' 's/JavaSE-11/JavaSE-17/g' {} +
find . -name "MANIFEST.MF" -type f -exec sed -i '' 's/JavaSE-17/JavaSE-21/g' {} +
find . -name ".classpath" -type f -exec sed -i '' 's/JavaSE-17/JavaSE-21/g' {} +

find . -name "feature.xml" -type f -exec sed -i '' 's/version="3.12.0.qualifier"/version="3.13.0.qualifier"/g' {} +

find . -name "MANIFEST.MF" -type f -exec sed -i '' 's/Bundle-Version: 3.12.0.qualifier/Bundle-Version: 3.13.0.qualifier/g' {} +

find . -name "eclipse.jdt.core.prefs" -type f -exec grep -H 'org.eclipse.jdt.core.compiler.codegen.targetPlatform=17' {} +
find . -name "eclipse.jdt.core.prefs" -type f -exec grep -H 'org.eclipse.jdt.core.compiler.codegen.targetPlatform=11' {} +

find . -name "org.eclipse.jdt.core.prefs" -type f -exec sed -i '' 's/org.eclipse.jdt.core.compiler.compliance=11/org.eclipse.jdt.core.compiler.compliance=17/g' {} \;
find . -name "org.eclipse.jdt.core.prefs" -type f -exec sed -i '' 's/org.eclipse.jdt.core.compiler.compliance=17/org.eclipse.jdt.core.compiler.compliance=21/g' {} \;

find . -name "org.eclipse.jdt.core.prefs" -type f -exec sed -i '' 's/org.eclipse.jdt.core.compiler.source=11/org.eclipse.jdt.core.compiler.source=17/g' {} \;
find . -name "org.eclipse.jdt.core.prefs" -type f -exec sed -i '' 's/org.eclipse.jdt.core.compiler.source=17/org.eclipse.jdt.core.compiler.source=21/g' {} \;

find . -name "org.eclipse.jdt.core.prefs" -type f -exec sed -i '' 's/org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6/org.eclipse.jdt.core.compiler.codegen.targetPlatform=21/g' {} \;

find . -name "pom.xml" -type f -exec sed -i '' 's/<version>3.12.0-SNAPSHOT<\/version>/<version>3.13.0-SNAPSHOT<\/version>/g' {} +

## Partial update dependencies

find . -name "MANIFEST.MF" -type f -exec sed -i '' 's/ch.elexis.core.ui;bundle-version="3.10.0"/ch.elexis.core.ui;bundle-version="3.13.0"/g' {} +

find . -name "MANIFEST.MF" -type f -exec sed -i '' 's/ch.elexis.core;bundle-version="3.10.0"/ch.elexis.core;bundle-version="3.13.0"/g' {} +