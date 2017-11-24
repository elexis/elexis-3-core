# elexis-3-core/ch.elexis.core.releng

This project contains utilities needed for the release engineering for elexis-3.

## Usage

* Very minimal at the time being

## Requirements

* Java 8
* Maven >= 3.3 (We are using 3.5.2)
* As IDE we use Eclipse Oxygen, for the target we use Eclipse Neon
* A fast way to setup a working development environment for Elexis 3 is documented in [ch.elexis.sdk](/ch.elexis.sdk/) 

## Building using the Eclipse IDE

* Import all maven project under elexis-3-core
* open ch.elexis.target/elexis/elexis.target and click on "Set as Target Platform"
* build all projects
* run the elexis application


## headless build (aka command line)

* `export JAVA_HOME=/opt/java-oracle/1.8.0_144` # Set your Java home
* ` mvn clean verify -V -DskipTests=true -Dmaven.javadoc.skip=true all-archs` # compiles and create packages

This creates directories like `ch.elexis.core.releng/product/target/products/ch.elexis.core.application.product/linux/gtk/x86_64` where you find the executable `Elexis 3` application.

The directory `ch.elexis.core.p2site/target/repository` contains a simple P2 update-site for Elexis.

To deploy this site have a look at the excellent "Eclipse Tycho - Tutorial for building Eclipse Plugins and RCP applications":http://www.vogella.com/articles/EclipseTycho/article.html#deploy. Or https://github.com/intalio/tycho-p2-scripts/wiki


### Commonly used maven parameters

`clean verify` cleans and rebuilds everything.

`-Dmaven.test.skip` skips tests (if you want to finish faster).

* We use "Reproducible Version Qualifiers":http://wiki.eclipse.org/Tycho/Reproducible_Version_Qualifiers to ensure that a new artifact id is generated whenever some of its content gets changed. These qualifier are also use when building a release.

For each of these branches our CI build create composite repositories like `https://download.elexis.info/elexis/master` where you find

** a p2 site for each part like elexis-3-core, elexis-3-base, elexis.3.gpl
** products, where you can find installable products for your OS


## GUI tests

We try to maintain Jubula GUI test for ensuring that common operations work well. See  [elexis-jubula](https://github.com/ngiger/elexis-jubula/blob/master/readme.md)

## Hints

To rebuild the elexis.ico for Windows use the following bash snippet

	convert elexis.xpm -bordercolor white -border 0 \
	\( -clone 0 -resize 16x16 -colors 256 \) \( -clone 0 -resize 16x16 \) \
	\( -clone 0 -resize 32x32 -colors 256 \) \( -clone 0 -resize 32x32 \) \
	\( -clone 0 -resize 48x48 -colors 256 \) \( -clone 0 -resize 48x48 \) \
	\( -clone 0 -resize 256x256 \) \
	-delete 0 -alpha off elexis.ico

Because of limitations in the Windows environment, be aware that the splash bmp must be 24-bit and not containt Color information.


## some git commands used by the release manager


* I use of then git option `--dry-run` before doing a push to double check, the opertions that will be performed

* When merging a branch or when others committed changes before I pushed my commits I 
	
	git pull		# get latest commits
	git rebase		# ensure that my commits come at the end, this avoids like merged changes from ..
	git push 		# now this should work without problems

* To create a tag and push it to the remote repository

	git tag release/3.1.4
	git push --tags
	
* To remove local and remote tags

	export tags_to_delete=`git tag -l | grep J-E3C`
	git tag -d $tags_to_delete
	git push --delete origin $tags_to_delete

* To remove outdated remote tags from another clone of the repository

	git fetch --prune origin  "+refs/tags/*:refs/tags/*"
