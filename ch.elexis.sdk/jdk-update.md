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
