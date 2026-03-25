# Maven Packages

In order to re-use Elexis bundles in Non-Tycho Maven projects, and allow for different technologies (like alternate CDI or Persistence implementations)
to be used, a "clean non-eclipse biased stack" is made available.

These maven packages are available via https://github.com/orgs/elexis/packages

## State of available packages

`elexis-3-core`

* `ch.elexis.core.l10n`
* `ch.rgw.utility` 
* `ch.elexis.core`
* `ch.elexis.core.cdi` (portable dependency injection)
* `ch.elexis.core.findings`
* `ch.elexis.core.documents` (1 quarkus biased dependency)
* `ch.elexis.core.hl7.v2x`
* `ch.elexis.core.importer.div`
* `ch.elexis.core.mail`
* `ch.elexis.core.spotlight`
* `ch.elexis.core.webdav`
* `ch.elexis.core.tasks.model`
* `ch.elexis.core.text.docx`


`elexis-3-base`

* `ch.elexis.omnivore` -> problem with pom.xml included ch.elexis.core dependencies - will break build
* `ch.elexis.global_inbox.core` -> no additional dependencies added
* `ch.elexis.base.solr`


`medelexis-3` https://gitlab.medelexis.ch/medelexis/medelexis-3/-/packages/

* `at.medevit.elexis.agenda.reminder` 
* `at.medevit.elexis.pdfocrmetadata.model`
* `at.medevit.elexis.pdfocrmetadata`

* WIP

### Required changes

* Adding manual `pom.xml` per project
* Modify target `<pomDependencies>` to [wrapAsBundle](https://tycho.eclipseprojects.io/doc/3.0.4/target-platform-configuration/target-platform-configuration-mojo.html#pomDependencies "title")
* Switch from Holders, resp. OsgiServiceUtil to PortableServiceLoader
* Add additional annotations on Osgi services
* If `@Component(immediate = true)` then you have to use `@Singleton`

## TODO

* `ch.elexis.core.hl7.v2x` do not include lib folder into package, moving to target failed
* * `ch.elexis.core.text.docx` reduce libraries in lib folder
* Create GitHub Action to build/deploy all packages


## Build and publish

Currently executed manually using `run-maven-deploy.sh`

The maven bundles are NOT being built and published on each build run. There exists a separate action to do so.



# Re-Use of Eclipse-biased components with different implementations


## Dependency Injection

In order to use Eclipselink and CDI we attach both annotations to the resp. classes.


## Persistence


# Helpers


### OS X replace holders with PortableServiceLoader
```
find . -type f -name "*.java" -exec sed -i '' 's/CoreModelServiceHolder\.get()/PortableServiceLoader.getCoreModelService()/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/ConfigServiceHolder/PortableServiceLoader.get(IConfigService.class)/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/VirtualFilesystemServiceHolder\.get()/PortableServiceLoader.get(IVirtualFilesystemService.class)/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/CodeElementServiceHolder\.get()/PortableServiceLoader.get(ICodeElementService.class)/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/ContextServiceHolder\.get()/PortableServiceLoader.get(IContextService.class)/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/LabServiceHolder\.get()/PortableServiceLoader.get(ILabService.class)/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/CoverageServiceHolder\.get()/PortableServiceLoader.get(ICoverageService.class)/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/ConfigServiceHolder\.get()/PortableServiceLoader.get(IConfigService.class)/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/MessageServiceHolder\.get()/PortableServiceLoader.get(IMessageService.class)/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/BillingServiceHolder\.get()/PortableServiceLoader.get(IBillingService.class)/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/EncounterServiceHolder\.get()/PortableServiceLoader.get(IEncounterService.class)/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/LocalLockServiceHolder\.get()/PortableServiceLoader.get(ILocalLockService.class)/g' {} +
find . -type f -name "*.java" -exec sed -i '' 's/StoreToStringServiceHolder\.get()/PortableServiceLoader.get(IStoreToStringService.class)/g' {} +
``