#!/usr/bin/env ruby
# coding: utf-8
# License: Eclipse Public License 1.0
# Copyright Niklaus Giger, 2011, niklaus.giger@member.fsf.org
require 'fileutils'
require 'tempfile'
require "#{File.dirname(__FILE__)}/helpers"
require 'rexml/document'
require 'pp'
require "#{File.dirname(__FILE__)}/jubulaoptions"
include REXML

if $0.index(File.basename(__FILE__))
  puts "Please call run_jenkins.rb to parse the options!"
  exit 2
end

class JubulaRun

  DefaultSleepTime =  /linux/.match(RbConfig::CONFIG['host_os']) ? 15 : 30
public
    JubulaOptions::Fields.each { 
      |x|
    eval(
      %(
      def #{x}
	@#{x}
      end
	)
    )
  }
    
  @@myFail = true

  # pass JubulaOptions like this: :autid = 'myAutId', :instDest = '/opt/myInstallation'
  def initialize(options = nil) 
    unless JubulaOptions::jubulaHome
      puts("JubulaOptions::jubulaHome not defined!")
      exit 1
    end
    JubulaOptions::Fields.each { |x| eval("@#{x} = JubulaOptions::#{x}") }
    options.each { |opt, val| eval( "@#{opt} = '#{val}'") } if options
	["#{JubulaOptions::jubulaHome}/server/autagent*",
	"#{JubulaOptions::jubulaHome}/#{@application}/#{@application}*",
	"#{JubulaOptions::jubulaHome}/#{@application}/testexec*",
	"#{JubulaOptions::jubulaHome}/#{@application}/dbtool*",
	].each { 
	  |file|
		if Dir.glob(file.gsub('"','')).size == 0
			puts("Jubula not correctly installed in #{JubulaOptions::jubulaHome}")
			puts("We could not find the needed application: #{file}")
			exit 1
                end
	}
    [@testResults, @dataDir].each { #  @data,
      |x|
	FileUtils.rm_rf(File.expand_path(x), :verbose => true, :noop => @dryRun)
	FileUtils.makedirs(x, :verbose => true, :noop => @dryRun)
    }
    instDest = File.dirname(exeFile)
    ENV['TEST_UPV_WORKSPACE'] = @workspace
  end

  def autoInstall
    FileUtils.rm_rf(File.expand_path(@instDest), :verbose => true, :noop => @dryRun)
    if /2\.1\.6/.match(@installer) and MACOSX_REGEXP.match(RbConfig::CONFIG['host_os'])
      # Elexis 2.1.6 used a zip file for the mac installer
      short = wgetIfNotExists(@installer.sub('.jar','.zip'))
      saved = Dir.pwd
      FileUtils.makedirs(@instDest)
      Dir.chdir(@instDest)
      system("unzip -qu #{saved}/#{short}")
      Dir.chdir(saved)
    else
      short = wgetIfNotExists(@installer)
      doc   = Document.new(File.new(@auto_xml))
      path  = XPath.first(doc, "//installpath" )
      path.text= @instDest
      file = Tempfile.new('auto_inst.xml')
      file.write(doc.to_s)
      file.rewind
      file.close
      system("java -jar #{short} #{file.path}")
      # file.unlink    # deletes the temp file
    end
  end
  
 def dbSpec
    "-dburl '#{@dburl}' -dbuser '#{@dbuser}' -dbpw '#{@dbpw}' "
 end
 
 def useH2(where = @data)
  @data     = where
  @dbscheme = 'Default Embedded (H2)'
  @dburl    = "jdbc:h2:#{where}/database/embedded;MVCC=TRUE;AUTO_SERVER=TRUE;DB_CLOSE_ON_EXIT=FALSE"
  @dbuser   = 'sa'
  @dbpw     = ''
 end
 
  def prepareRcpSupport
    savedDir = Dir.pwd
    cmd = "#{JubulaOptions::jubulaHome}/development/rcp-support.zip"
    if @dryRun
      puts "should cd #{File.join(@instDest, 'plugins')} && unzip #{cmd}"
    elsif @instDest and File.directory?(@instDest)
      FileUtils.makedirs(File.join(@instDest, 'plugins'))
      Dir.chdir(File.join(@instDest, 'plugins'))
    end
    if WINDOWS_REGEXP.match(RbConfig::CONFIG['host_os'])
      cmd = "#{File.expand_path(File.dirname(__FILE__))}/7z x -y #{cmd} > test-unzip.log"
      cmd.gsub!('\\', '\\\\')
    else 
      cmd = "unzip -qu #{cmd}"
    end
    fileName = File.expand_path("#{@instDest}/plugins/org.eclipse.jubula.rc.rcp_*.jar")
    if Dir.glob(fileName).size != 1 or File.size(Dir.glob(fileName)[0]) < 4000
      FileUtils.rm_f(Dir.glob(fileName)[0]) if Dir.glob(fileName).size > 0
      system(cmd)
    end
    ini_name = File.join(@instDest, 'configuration', 'config.ini')
    if Dir.glob(@exeFile).size == 0
      puts "prepareRcpSupport: Could not find ini_name #{ini_name}. host_os is #{RbConfig::CONFIG['host_os']}"
      exit 1
    end
    FileUtils.chmod(0755, @exeFile, :verbose => true)
    config_ini = IO.readlines(ini_name)
    needsJubulaRcpSupport = true
    rcpStart = ',org.eclipse.jubula.rc.rcp@start'
    config_ini.each{ 
      |line|
        needsJubulaRcpSupport = false if /^osgi.bundles=/.match(line) and /#{rcpStart}/.match(line)
    }   
    puts "#{File.expand_path(ini_name)}: #{needsJubulaRcpSupport ? ' Will patch to add' : ' Already patched to'} start jubula.rc.rcp."
    if needsJubulaRcpSupport
      FileUtils.cp(ini_name, ini_name + '.bak', :verbose => true);
      config_ini.each{ 
        |line|
          if /^osgi.bundles=/.match(line)
            puts "must patch #{ini_name}"
            puts line if $VERBOSE
            line.sub!(/\n/, rcpStart + "\n")
            break
          end
      }
      File.open(ini_name, 'w') { |file| file.write config_ini.join('') }
    end
    Dir.chdir(savedDir)
  end
  
  def rmTestcases(tc = @project, version = @version)
    if /jdbc:h2/i.match(@dburl)
      # Just remove the directory where the h2 database is stored. Is a lot faster then the other
      dbDir = File.dirname(@dburl.split(';')[0].split(':')[-1])
      FileUtils.rm_rf(File.expand_path(dbDir), :verbose => true, :noop => @dryRun)
    else
      system("#{JubulaOptions::jubulaHome}/#{@application}/dbtool -data #{@data} -delete #{project} #{version} #{dbSpec}", @@myFail)
    end
  end
  
  def patchXML
    xmlFile = "#{project}_#{version}.xml"
    cmd = "ruby patch_views_pref_persp.rb #{xmlFile} #{@instDest}/plugins"
    exit 1 unless system(cmd, false)
  end
  
  def loadTestcases(xmlFile = "#{project}_#{version}.xml")
    ["unbound_modules_swt", "unbound_modules_concrete",  "unbound_modules_rcp"].each{ 
      |tcModule|
      tcs = Dir.glob("#{JubulaOptions::jubulaHome}/examples/testCaseLibrary/#{tcModule}_*.xml")
      if tcs.size != 1
	puts "Should have found exactly 1 one file. Got #{tcs.inspect}"
	exit 1
      end
      system("#{JubulaOptions::jubulaHome}/#{@application}/dbtool -data #{@data} -import #{tcs[0]} #{dbSpec}", @@myFail)
      } if true
    system("#{JubulaOptions::jubulaHome}/#{@application}/dbtool -data #{@data} -import #{xmlFile} #{dbSpec}")
  end

  def adaptCmdForMacOSx(cmd)
    [ '.app/Contents/MacOS/JavaApplicationStub',
      '.app/Contents/MacOS/autagent'].each {
	|tst|
	return cmd+tst if Dir.glob(cmd+tst).size == 1
      }
    return cmd
  end
  
  def startAgent(sleepTime = DefaultSleepTime)
    puts("# Sleeping for #{sleepTime} after startAgent" )
  cmd = adaptCmdForMacOSx("#{JubulaOptions::jubulaHome}/server/autagent")
	cmd = "#{cmd} -p #{portNumber}"
	if WINDOWS_REGEXP.match(RbConfig::CONFIG['host_os'])
		res = system("start #{cmd}")
	else
		res = system("#{cmd} &")
	end
	if !res then puts "failed. exiting"; exit(3); end
    sleep(sleepTime)
  end
  
  def startAUT(sleepTime = DefaultSleepTime)
    puts("# Sleeping for #{sleepTime} after startAUT" )
    @@nrRun ||= 0
    @@nrRun += 1
    log = "#{@testResults}/test-console-#{@@nrRun}.log"
    cmd = "#{JubulaOptions::jubulaHome}/server/autrun --workingdir #{@testResults} -rcp --kblayout #{@kblayout} -i #{@autid} --exec #{@wrapper} --generatename true --autagentport #{@portNumber}"
    if WINDOWS_REGEXP.match(RbConfig::CONFIG['host_os'])
      cmd = "start #{cmd}"
    else
      cmd += " 2>&1 | tee #{log} &"
    end
    res = system(cmd)
    if !res then puts "failed. exiting"; exit(3); end
      sleep(sleepTime)
    end
  
  def stopAgent(sleepTime = 3)
    cmd = adaptCmdForMacOSx("#{JubulaOptions::jubulaHome}/server/stopautagent")
    system("#{cmd} -p #{@portNumber} -stop", @@myFail)
    sleep(sleepTime)
  end

  def runTestsuite(testsuite = @testsuite)
    res = system("#{JubulaOptions::jubulaHome}/#{@application}/testexec -project  #{project} -port #{@portNumber} " +
	  "-version #{@version} -testsuite '#{testsuite}' -server #{server} -autid #{@autid} "+
	  "-resultdir #{@testResults} -language  #{@kblayout} #{dbSpec} " +
	  "-datadir #{@dataDir} -data #{@data}")
    puts "runTestsuite  #{testsuite} returned #{res.inspect}"
    res
  end
  
  def runOneTestcase(testcase, sleepTime = DefaultSleepTime)
    startAgent
    startAUT(sleepTime)
    okay = runTestsuite(testcase)
    stopAgent(10)
    system("killall #{File.basename(@exeFile)}") unless WINDOWS_REGEXP.match(RbConfig::CONFIG['host_os']) # if still alive
    okay
  end

  def run(testsuite=@testsuite)
    genWrapper
    autoInstall
    prepareRcpSupport
    useH2
    loadTestcases
    startAgent
    startAUT
    res = runTestsuite(testsuite)
    stopAgent
    res
  end

  def genWrapper
    unless @exeFile
      puts "no ExeFile defined";
      exit 2
    end
    wrapper = "#{JubulaOptions.wrapper}"
    exe  = File.expand_path(@exeFile)
    doc = "\"#{exe}\" #{vm.eql?('java') ? "" : " -vm #{vm}"} -data #{@dataDir} -vmargs #{vmargs}"
    File.open(wrapper, 'w') {|f| f.puts(doc) }
    FileUtils.chmod(0744, wrapper)
    puts "#{dryRun ? 'Would create' : 'Created'} wrapper script #{wrapper} with content"
    puts doc
  end

  def checkOutcome(res, label)
    if res
      puts "#{label} completed successfully"
    else
      puts "#{label} #{label} failed"
      exit(2)
    end
  end

  def saveImages(dest = @testResults)
    FileUtils.makedirs(dest)
    puts "Would save images/htm/log to #{dest}" if DryRun
    (Dir.glob("**/*shot*/*.png")+Dir.glob("**/*.log")+Dir.glob("**/*htm")).each{
      |x|
          next if /images/.match(x)
          next if /plugins/.match(x)
          next if /#{File.basename(@testResults)}/.match(x)
          FileUtils.cp(x, dest, :verbose => true, :noop => DryRun)
    }
  end

end

if $0 == __FILE__
  JubulaOptions::parseArgs
  JubulaOptions::dryRun == true ? @dryRun = true : @dryRun = false

  # run with defaults
  jubula = JubulaRun.new
  jubula.run
end
