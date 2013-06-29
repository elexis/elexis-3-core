#!/bin/env ruby
require 'rake/clean'
require 'pp'

TychoVersion  = '0.18.0'
ElexisVersion = '3.0.0-SNAPSHOT'
    
LibDirectory = File.expand_path(File.join(File.dirname(__FILE__), 'ch.elexis.core.releng', 'elexis.3rdpart.libraries'))
JettyPort    = 8753

desc "Create p2-site with libraries needed for Elexis 3"
task :p2_lib_site do
  Dir.chdir(LibDirectory)
  system("mvn p2:site") unless File.directory?(File.join('target', 'repository', 'plugins'))
  Dir.chdir(File.dirname(__FILE__))
end

desc "Start http://localhost:#{JettyPort}/site with libraries needed for Elexis 3"
task 'p2-libs' =>  :p2_lib_site do
  Dir.chdir(LibDirectory)
  cmd = "lsof -i :#{JettyPort} | grep LISTEN >/dev/null"
  res = system(cmd)
  puts "res for #{cmd} ist #{res} " if $VERBOSE
  if res
    puts "jetty seems to be running on port #{JettyPort}"
  else
    puts "Starting jetty"
    system("mvn -Djetty.port=#{JettyPort} jetty:run > jetty.log 2>&1 &")
  end
end

CLEAN.include(File.join(LibDirectory, 'target'))

desc 'init'
task :init_pom => :p2_lib_site do
  source = File.join(LibDirectory, 'pom.template')
  backup = File.join(LibDirectory, 'pom.xml.backup')
  File.chdir('ch.elexis.core.releng')
  cmd = "mvn org.eclipse.tycho:tycho-pomgenerator-plugin:generate-poms -DgroupId=ch.elexis -Dversion=#{ElexisVersion} -DextraDirs='..'"
  pp Dir.glob('**/pom.xml')
  FileUtils.rm_f(['pom.xml', Dir.glob('*/pom.xml')], :verbose => true)
  pp Dir.glob('**/pom.xml')
  puts cmd
  system(cmd)
  FileUtils.mv(backup, source, :verbose => true)
  FileUtils.cp('pom.template', 'pom.xml', :verbose => true)
  # ch.elexis.core.application/pom.template
end if false

