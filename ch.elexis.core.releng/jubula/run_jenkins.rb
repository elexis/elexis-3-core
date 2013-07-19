#!/usr/bin/env ruby
# coding: utf-8
# License: Eclipse Public License 1.0
# Copyright Niklaus Giger, 2013, niklaus.giger@member.fsf.org

# Allows us to run the Elexis Jubula GUI-Tests as Jenkis CI-job

require "#{File.dirname(__FILE__)}/jubulaoptions"
require "#{File.dirname(__FILE__)}/jubularun"

opts = JubulaOptions::parseArgs
opts.parse!(ARGV)
JubulaOptions::dryRun == true ? DryRun = true : DryRun = false
jubula = JubulaRun.new(:portNumber => 60000 + (Process.pid % 1000),
                       :vmargs => "-Dch.elexis.username=007 -Dch.elexis.password=topsecret -Delexis-run-mode=RunFromScratch",
                       :autid => 'elexis')

# For unknown reasons (which took me a few hours to code around) I decided
# that is is not my aim to use a MySQL database to store the Jubula testcases
# Instead we also start from a fresh, empty workspace and an empty embedded H2 db
# Costs me a good minute

wsDir = "#{jubula.workspace}/test-ws"
FileUtils.rm_rf(wsDir, :verbose => true, :noop => DryRun)

jubula.useH2(Dir.pwd)
# jubula.rmTestcases 	# only if using h2 
# jubula.loadTestcases    # only if using h2
jubula.prepareRcpSupport
jubula.genWrapper

okay1 = true	
okay1 = jubula.runOneTestcase('sample', 60) if false
puts "okay1 ist #{okay1}"
okay2 = true
okay2 = jubula.runOneTestcase('FULLTEST', 60) # 30 Sekunden waren nicht genug auf Windows bis Elexis aufgestartet war
puts "okay2 ist #{okay2}"

Dir.glob("**/*shot*/*.png").each{ 
  |x|
      next if /images/.match(x)
      next if /plugins/.match(x)
      next if /#{File.basename(jubula.testResults)}/.match(x)
      FileUtils.cp(x, "#{jubula.testResults}", :verbose => true, :noop => DryRun)
}
if okay1 and okay2 
  puts "Sample and FULLTEST were okay!"
  exit(0);
else
  puts "Sample #{okay1} or FULLTEST #{okay2} failed"
  exit(2)
end
