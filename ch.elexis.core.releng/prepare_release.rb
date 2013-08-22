#!/usr/bin/env ruby
# encoding: UTF-8

# helper for an Elexis 3.x release.
# Will try to delegate as much as possible to the http://maven.apache.org/maven-release/maven-release-plugin/usage.html
# or may be for git we better use http://blogs.atlassian.com/2013/05/maven-git-flow-plugin-for-better-releases/
# http://stackoverflow.com/questions/10694139/maven-3-0s-mvn-releaseperform-doesnt-like-a-pom-xml-that-isnt-in-its-git-r
require 'optparse'
require 'pp'
require File.join(File.dirname(__FILE__), 'version_helper.rb')
                  
unless /^(1\.9|2)/.match(RUBY_VERSION)
  puts "Must be run with RUBY >= 1.9, is #{RUBY_VERSION}"
  exit
end


def updateAll(rootDir, newVersion)
  variant = /snapshot|qualifier/i.match(newVersion) ? 'snapshot' : 'release'
  Dir.glob("#{rootDir}/**/pom.xml").sort.uniq.each              { |file| VersionUpdater.new(file, newVersion, @dryRun).update_pom }
  Dir.glob("#{rootDir}/**/META-INF/MANIFEST.MF").sort.uniq.each { |file| VersionUpdater.new(file, newVersion, @dryRun).update_mf }
  Dir.glob("#{rootDir}/**/feature.mf").sort.uniq.each           { |file| VersionUpdater.new(file, newVersion, @dryRun).update_feature }
  Dir.glob("#{rootDir}/**/category.xml").sort.uniq.each         { |file| VersionUpdater.new(file, newVersion, @dryRun).update_category }
  Dir.glob("#{rootDir}/**/p2.inf").sort.uniq.each              { 
               |file| VersionUpdater.new(file, newVersion, @dryRun).update_using_regexp(/addRepository.*(snapshot|release),/i, variant) }
  Dir.glob("#{rootDir}/**/*.product").sort.uniq.each { 
    |file| 
    next if File.directory?(file)
    VersionUpdater.new(file, newVersion, @dryRun).update_product # update_using_regexp(/version="([^"]*)".*useFeatures/) 
  }
end

@root_dir = nil
@dryRun = false
@newVersion = nil
options = OptionParser.new do |opts|
      opts.banner = %(Usage: #{File.basename($0)} root_dir
Prepares for an Elexis release
   This utility should be able to prepare a release of the Elexis 3
   Tasks to fullfill:
   * update all versions in 
   ** MANIFEST.MF
   ** feature.xml
   ** pom.xml
   ** category.xml
   ** p2.inf
   ** Elexis.product
TODO:   ** Rakefile
    )
      opts.on("--version version", "new version to use") do |v|
        @newVersion = v
      end
      opts.on("-n", "--dry-run", "Don't execute the steps. Just show what would happe") do |v|
        @dryRun = true
      end
      opts.on("-h", "--help", "Show this help") do |v|
  puts opts
  exit
      end
end

args = options.parse!
pp args
pp ARGV.size
pp @newVersion
unless ARGV.size == 1 and @newVersion
  puts options.help
  exit 1
end
updateAll(ARGV[0], @newVersion)