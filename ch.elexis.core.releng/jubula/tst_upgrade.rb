#!/usr/bin/env ruby
# coding: utf-8
# License: Eclipse Public License 1.0
# Copyright Niklaus Giger, 2013, niklaus.giger@member.fsf.org

# TODO: Install paste.exe as xlip.exe under Windows, see http://www.c3scripts.com/tutorials/msdos/paste.html
startTime = Time.now

require 'fileutils'
require 'open-uri'
require 'tmpdir'
require 'pp'
require "#{File.dirname(__FILE__)}/jubulaoptions"
require "#{File.dirname(__FILE__)}/jubularun"
opts = JubulaOptions::parseArgs
opts.parse!(ARGV)
JubulaOptions::dryRun == true ? DryRun = true : DryRun = false

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


SavedDir = Dir.pwd

def unzip_elexis_3 (file, destination)
  tempName = File.basename(file)
  puts "Downloading into #{destination} from #{file}\n   via #{tempName} "
  FileUtils.makedirs(destination)
  Dir.chdir(destination)
  cmd ="wget --quiet --no-check-certificate --timestamping #{file}"
  puts cmd
  res = system(cmd)
  exit(2) unless res or File.exists?(tempName) or DryRun

  if false
  Zip::ZipFile.open(tempName) { |zip_file|
   zip_file.each { |f|
     f_path= f.name
     FileUtils.mkdir_p(File.dirname(f_path))
     zip_file.extract(f, f_path) unless File.exist?(f_path)
   }
  } unless DryRun
  else
    system("unzip -o #{tempName} > unzip.log")
  end
end

@swInstId ||= 1
@summary  = []
@testResultsRoot = File.expand_path(File.join(File.dirname(__FILE__), '..', '..', 'test-results'))
def report(msg)
  puts "#{@swInstId}: #{@version}: #{Time.now} #{msg}"
end

def report_add_separator
  puts '----------------------------------------------------------------'
end

def runOneInstallTest(url, expectation, instDest = File.join(Dir.pwd, "sw-upgrade-tst-#{@swInstId}"))
  m = url.match(/job\/([^\/]+).*\/(\w+)\/artifact/)
  build = m[2].to_i ? "build #{m[2]}" : m[2]
  @version = sprintf('%-40s', (m ? "#{m[1]} build #{m[2]}" : 'version not found'))
  report "instDest -> #{instDest} expecting #{expectation}"
  ENV['TEST_UDV_SW_MUST_UPGRADE'] = expectation
  unzip_elexis_3(url, instDest)
  exeFile = -1
  if DryRun and not File.directory?(instDest)
    exeFile = File.join(Dir.pwd, 'dummy-exe-file-for-dryRun')
  elsif Dir.glob("#{instDest}/*lexis*").size > 0
    Dir.glob("#{instDest}/*lexis*").each{
      |exe|
        next if exe.match(/(zip|ini)$/)
        next unless exe.match(/lexis/)
        exeFile = exe
        @versionDate = File.mtime(exeFile).strftime('%d.%m.%Y %H:%m')
        FileUtils.chmod(0755, exeFile, :verbose => true) if exeFile and File.exists?(exeFile)
        break
    }
  else
    puts "No elexis found in zip from #{url}. Saved in #{instDest}"
    exit 2
  end
  Dir.chdir(File.join(instDest, 'plugins'))
  origJars = Dir.glob('*.jar')
  Dir.chdir(SavedDir)

  @jubula = JubulaRun.new(:portNumber => 60000 + (Process.pid % 1000),
                        # browser: workaround see https://bugs.eclipse.org/bugs/show_bug.cgi?id=404776, which was not backported to 3.8.2, only in 4.3.2
                        # and for p2.unsignedPolicy https://bugs.eclipse.org/bugs/show_bug.cgi?id=235526
                        :exeFile => exeFile,
                        :testResults => File.join(@testResultsRoot, @swInstId.to_s),
                        :instDest => File.dirname(exeFile),
                        :vmargs => "-Declipse.p2.unsignedPolicy=allow -Dorg.eclipse.swt.browser.DefaultType=mozilla -Dch.elexis.username=007 -Dch.elexis.password=topsecret -Delexis-run-mode=RunFromScratch",
                        :autid => 'elexis')

  @jubula.useH2(Dir.pwd)
  @jubula.prepareRcpSupport
#  @jubula.patchXML not needed for SW-Upgrade
  @jubula.genWrapper
  if @swInstId == 1
    @jubula.rmTestcases  # only if using h2
    @jubula.loadTestcases    # only if using h2
  end
  tstCase2run = 'TST_UPGRADE'
  res = @jubula.runOneTestcase(tstCase2run)
  @jubula.saveImages
  Dir.chdir(File.join(instDest, 'plugins'))
  newJars = Dir.glob('*.jar')
  Dir.chdir(SavedDir)
  puts "origJars were: #{origJars}"
  report "newJars  are: #{newJars}"
  report_add_separator
  nrNewJars = origJars.size - newJars.size
  report "difference is: #{origJars.size} ->  #{newJars.size} jars: #{(newJars-origJars).sort.uniq}"
  report "our expectation using TEST_UDV_SW_MUST_UPGRADE was #{expectation}."
  info = "Upgrade Elexis created #{@versionDate} added #{nrNewJars} jars #{res ? 'was succesfull' : 'failed'}"
  report info
  @summary << info
  report_add_separator
#  @jubula.checkOutcome(res, tstCase2run)
  @swInstId += 1
end

ci_base = 'https://srv.elexis.info/jenkins/'
urls = {
  ci_base + 'job/Elexis-3.0-Core/lastSuccessfulBuild/artifact/ch.elexis.core.p2site/target/products/ch.elexis.core.application.ElexisApp-linux.gtk.x86_64.zip' => "true", # last success, will install new SW
  ci_base + 'job/Elexis-3.0-Core/893/artifact/ch.elexis.core.p2site/target/products/ch.elexis.core.application.ElexisApp-linux.gtk.x86_64.zip' => "true", # Build vom 16.05.2014
  ci_base + 'job/Elexis-3.0-Core-Releases/53/artifact/ch.elexis.core.p2site/target/products/ch.elexis.core.application.ElexisApp-linux.gtk.x86_64.zip' => "true", # Beta 2
  # we cannot test the Beta 1 with jubula because before jubula connects to the AUT we get the dialog "Eine neue Datenbank wurde angelegt"
  # but it seems to have (as per 24.05.2014 at least the same problem as the Beta2 org.apache.commons.io not found)
  # ci_base + 'job/Elexis-3.0-Core-Releases/23/artifact/ch.elexis.core.p2site/target/products/ch.elexis.core.application.product-linux.gtk.x86_64.zip' => "true", # Beta 1
  }
urls.each{ |url, expectation| runOneInstallTest(url, expectation)
}


endTime = Time.now
seconds = (endTime-startTime).to_i
report_add_separator

puts "Summary over #{@swInstId-1} installations after #{seconds/60} minutes and #{seconds%60} seconds is:"
cmd = "grep Upgrade #{File.expand_path(File.join(@jubula.testResults, '..', '*', '*.log'))}"
puts @summary.join("\n")
report_add_separator

