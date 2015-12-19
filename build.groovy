// Copyright (c) 2015 by Niklaus Giger niklaus.giger@member.fsf.org
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Helper for Jenkins-CI build. Will be invoked by Jenkinsfile if doing a Multibranch build
// or by a workflow like elexis-releng/Jenkinsfile
// For more explanations look at https://github.com/ngiger/elexis-releng

// Get the maven tool.
// ** NOTE: This 'M3' maven tool must be configured
// **       in the global configuration.
def mvnHome = tool 'M3'
env.PATH = "${mvnHome}/bin:$env.PATH"

if ( env.VARIANT == null ) { env.VARIANT='snapshot' }
if ( env.INSTALL_BUILD == null ) { env.INSTALL_BUILD="https://raw.githubusercontent.com/ngiger/elexis-releng/master/install_repo.rb" }
if ( env.downloads == null ) { env.downloads='/var/jenkins_home/userContent' }


stage 'Build'
// TODO: Must change ch.elexis.core.data.tests/src/ch/elexis/data/AbstractPersistentObjectTest.java:
// to allow specifying another host instead of localhost to reenable -Delexis.run.dbtests=true

wrap([$class: 'Xvfb']) {
  sh "mvn clean install -Drepo_variant=${env.VARIANT} -Dtycho.localArtifacts=ignore -Pall-archs"
}

step([$class: 'ArtifactArchiver', artifacts:  "*p2site/target/repository/**", fingerprint: true])
step([$class: 'ArtifactArchiver', artifacts:  "*p2site/target/products/*.zip", fingerprint: true])

sh "rm -f install_build.rb"
sh "wget ${env.INSTALL_BUILD}"
withEnv(["ROOT=${env.downloads}/elexis.3.core"]) {
  sh "ruby ${new File(env.INSTALL_BUILD).toPath().getFileName()} --variant=${env.VARIANT}"
}

