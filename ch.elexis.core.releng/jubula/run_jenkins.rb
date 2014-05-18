#!/usr/bin/env ruby
# coding: utf-8
# License: Eclipse Public License 1.0
# Copyright Niklaus Giger, 2013, niklaus.giger@member.fsf.org

# Allows us to run the Elexis Jubula GUI-Tests as Jenkis CI-job

require "#{File.dirname(__FILE__)}/jubulaoptions"
require "#{File.dirname(__FILE__)}/jubularun"
$stdout.sync=true

opts = JubulaOptions::parseArgs
opts.parse!(ARGV)
JubulaOptions::dryRun == true ? DryRun = true : DryRun = false
jubula = JubulaRun.new(:portNumber => 60000 + (Process.pid % 1000),
                       # browser: workaround see https://bugs.eclipse.org/bugs/show_bug.cgi?id=404776, which was not backported to 3.8.2, only in 4.3.2
                       # and for p2.unsignedPolicy https://bugs.eclipse.org/bugs/show_bug.cgi?id=235526
                       :vmargs => "-Declipse.p2.unsignedPolicy=allow -Dorg.eclipse.swt.browser.DefaultType=mozilla -Dch.elexis.username=007 -Dch.elexis.password=topsecret -Delexis-run-mode=RunFromScratch",
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
jubula.patchXML
jubula.rmTestcases 	# only if using h2 
jubula.loadTestcases    # only if using h2
res_FULLTEST = jubula.runOneTestcase('FULLTEST', 15) # 30 Sekunden waren nicht genug auf Windows bis Elexis aufgestartet war
puts "res_FULLTEST ist #{res_FULLTEST}"

Dir.glob("**/*shot*/*.png").each{ 
  |x|
      next if /images/.match(x)
      next if /plugins/.match(x)
      next if /#{File.basename(jubula.testResults)}/.match(x)
      FileUtils.cp(x, "#{jubula.testResults}", :verbose => true, :noop => DryRun)
}
if res_FULLTEST 
  puts "FULLTEST completed successfully"
  exit(0);
else
  puts "FULLTEST #{res_FULLTEST} failed"
  exit(2)
end
