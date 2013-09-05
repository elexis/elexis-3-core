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


defaultUrl = 'http://ngiger.dyndns.org/jenkins/job/Elexis-3.0-Core/lastSuccessfulBuild/artifact/ch.elexis.core.p2site/target/products/ch.elexis.core.application.product-linux.gtk.x86_64.zip'
defaultDest = '/tmp/elexis-3.0.test'

def unzip_elexis_3 (file, destination)
  tempName = File.basename(file)
  puts "Downloading #{file} via #{tempName} into #{destination}"
  open(file) {
    |f|
    ausgabe = File.open(tempName, 'w+')
    ausgabe.write f.read
    ausgabe.close
  } unless File.exists?(tempName) and File.size(tempName) > 1024

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
ARGV << "--exeFile=#{exeFile}"
ARGV << "--vmargs='-Dch.elexis.username=007 -Dch.elexis.password=topsecret -Delexis-run-mode=RunFromScratch'"
ARGV << '--projectVersion=1.1'
require "#{File.dirname(__FILE__)}/jubulaoptions"
$stdout.sync=true
opts = JubulaOptions::parseArgs
opts.parse!(ARGV)
require "#{File.dirname(__FILE__)}/jubularun"
JubulaOptions::dryRun == true ? DryRun = true : DryRun = false
jubula = JubulaRun.new(:portNumber => 60000 + (Process.pid % 1000),
                       :autid => 'elexis')
# For unknown reasons (which took me a few hours to code around) I decided
# that is is not my aim to use a MySQL database to store the Jubula testcases
# Instead we also start from a fresh, empty workspace and an empty embedded H2 db
# Costs me a good minute

wsDir = "#{jubula.workspace}/test-ws"
FileUtils.rm_rf(wsDir, :verbose => true, :noop => DryRun)

jubula.useH2(Dir.pwd)
jubula.prepareRcpSupport
jubula.genWrapper
exit
jubula.rmTestcases 	# only if using h2 
jubula.loadTestcases    # only if using h2
res_TST_UPGRADE = jubula.runOneTestcase('TST_UPGRADE', 15) # 30 Sekunden waren nicht genug auf Windows bis Elexis aufgestartet war
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
