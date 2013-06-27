#!/bin/env ruby
require 'rake/clean'

LibDirectory = File.expand_path(File.join(File.dirname(__FILE__), 'ch.elexis.core.releng', 'foreign-libraries'))
JettyPort    = 8753

desc "Create p2-site with libraries needed for Elexis 3"
task :p2_lib_site do
  Dir.chdir(LibDirectory)
  system("mvn p2:site") unless File.directory?(File.join('target', 'repository', 'plugins'))
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
