#!/usr/bin/env ruby
# coding: utf-8
# License: Eclipse Public License 1.0
# Copyright Niklaus Giger, 2013, niklaus.giger@member.fsf.org

# Allows us to run the Elexis Jubula GUI-Tests as Jenkis CI-job

# http://ngiger.dyndns.org/jenkins/job/Elexis-3.0-Core/113/artifact/ch.elexis.core.p2site/target/products/ch.elexis.core.application.product-linux.gtk.x86_64.zip

require 'fileutils'
require 'open-uri'
begin 
  require  'zip/zip'
rescue LoadError
  puts "Failed loading 'zip'" if $VERBOSE # Probably using rubyzip 1.0.0 or later
end
begin 
  require 'zip'
rescue LoadError
  puts "Failed loading 'zip'" # if $VERBOSE # Probably using rubyzip 0.9.9 or earlier
end


defaultUrl = 'https://srv.elexis.info/jenkins/job/Elexis-3.0-Core/lastSuccessfulBuild/artifact/ch.elexis.core.p2site/target/products/ch.elexis.core.application.ElexisApp-linux.gtk.x86_64.zip'
defaultDest = '/tmp/elexis-3.0.test'
SavedDir = Dir.pwd

def unzip_elexis_3 (file, destination)
  tempName = File.basename(file)
  puts "Downloading #{file} via #{tempName} into #{destination}"
  FileUtils.makedirs(destination)
  Dir.chdir(destination)
  cmd ="wget --no-check-certificate --timestamping #{file}"
  puts cmd
  res = system(cmd)
  exit(2) unless res and File.exists?(tempName)

  if false
  open(file) {
    |f|
    ausgabe = File.open(tempName, 'w+')
    ausgabe.write f.read
    ausgabe.close
  } unless File.exists?(tempName) and File.size(tempName) > 1024
  end
  
  Zip::ZipFile.open(tempName) { |zip_file|
   zip_file.each { |f|
     f_path=File.join(destination, f.name)
     FileUtils.mkdir_p(File.dirname(f_path))
     zip_file.extract(f, f_path) unless File.exist?(f_path)
   }
  }
end

unzip_elexis_3(defaultUrl, defaultDest)
require 'pp'
if Dir.glob("#{defaultDest}/*lexis*").size > 0
  exeFile = Dir.glob("#{defaultDest}/*lexis*")[0]
else
  exeFile = 'unknown_elexis_exe'
end
Dir.chdir(SavedDir)
$stdout.sync=true

require "#{File.dirname(__FILE__)}/jubulaoptions"
require "#{File.dirname(__FILE__)}/jubularun"
opts = JubulaOptions::parseArgs
opts.parse!(ARGV)
JubulaOptions::dryRun == true ? DryRun = true : DryRun = false
puts "Calling JubulaRun.new"
jubula = JubulaRun.new(:portNumber => 60000 + (Process.pid % 1000),
                       # Workaround see https://bugs.eclipse.org/bugs/show_bug.cgi?id=404776, which was not backported to 3.8.2, only in 4.3.2
                       # and for p2.unsignedPolicy https://bugs.eclipse.org/bugs/show_bug.cgi?id=235526
                       :exeFile => exeFile,
                       :instDest => File.dirname(exeFile),
                       :vmargs => "-Declipse.p2.unsignedPolicy=allow -Dorg.eclipse.swt.browser.DefaultType=mozilla -Dch.elexis.username=007 -Dch.elexis.password=topsecret -Delexis-run-mode=RunFromScratch",
                       :autid => 'elexis')
wsDir = "#{jubula.workspace}/test-ws-upgrade"
FileUtils.rm_rf(wsDir, :verbose => true, :noop => DryRun)

jubula.useH2(Dir.pwd)
jubula.prepareRcpSupport
jubula.genWrapper
jubula.patchXML
jubula.rmTestcases  # only if using h2
jubula.loadTestcases    # only if using h2
res_TST_UPGRADE = jubula.runOneTestcase('TST_UPGRADE')
puts "res_TST_UPGRADE ist #{res_TST_UPGRADE}"

Dir.glob("**/*shot*/*.png").each{ 
  |x|
      next if /images/.match(x)
      next if /plugins/.match(x)
      next if /#{File.basename(jubula.testResults)}/.match(x)
      FileUtils.cp(x, "#{jubula.testResults}", :verbose => true, :noop => DryRun)
}
if res_TST_UPGRADE 
  puts "TST_UPGRADE completed successfully"
  exit(0);
else
  puts "TST_UPGRADE #{res_TST_UPGRADE} failed"
  exit(2)
end
