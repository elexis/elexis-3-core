// Copyright (c) 2015 by Niklaus Giger niklaus.giger@member.fsf.org
//
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// This file is called by Jenkinsfile from this project for multibranch projects
// and from https://github.com/ngiger/elexis-releng/Jenkinsfile to build complete releases
//
// see http://jenkins-ci.org/content/pipeline-code-multibranch-workflows-jenkins
node {
  wrap([$class: 'TimestamperBuildWrapper']) {
      // Mark the code checkout 'stage'....
      stage 'Checkout'

      // Checkout code from repository
      checkout scm

      // Mark the code build 'stage'....
      stage 'Build'

      // we delegate the actual building to build.groovy to allow
      // the release workflow to easily build all our project
      build = load 'build.groovy'
  }
}