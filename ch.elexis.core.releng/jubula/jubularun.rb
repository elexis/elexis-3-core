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
        raise "We could not find the needed application: #{file}"
      end
    }
    ENV['TEST_UPV_WORKSPACE'] = @workspace
  end

  # set the helper variables for @instDest, @config_ini, @plugins_dir
  def getInfoForExe
    if @installer
      FileUtils.makedirs(@instDest, :verbose => true, :noop => @dryRun)
      case RbConfig::CONFIG['host_os']
        when WINDOWS_REGEXP
          pathname = File.join(@instDest, '*elexis*.exe')
          if (Dir.glob(File.expand_path(pathname)).size == 1)
            @exeFile = Dir.glob(File.expand_path(pathname))[0]
          end
        when MACOSX_REGEXP
          pathname = "#{@instDest}/*app/configuration/config.ini"
          files = Dir.glob(File.expand_path(pathname))
          if (files.size == 1)
            pathname =files[0].sub('.app/Contents/macos/configuration/config.ini', '')
            pathname =pathname.sub('configuration/config.ini', '')
            appName = File.basename(pathname).sub('.app', '')
            @exeFile = File.join(pathname, 'Contents', 'MacOS', appName)
          end
        else # linux
          if @dryRun
            @exeFile = File.expand_path(@instDest+'/Elexis')
          else
            Dir.glob(File.expand_path(@instDest+'/*.ini')).each{ |f|
              f = f.sub('.ini', '')
              if File.executable?(f) and not File.directory?(f)
                @exeFile = f; break;
              end
            }
          end
      end
    elsif @exeFile
      puts " @exeFile defined as #{@exeFile}"
      @plugins_dir = File.join(@instDest, 'plugins')
      if MACOSX_REGEXP.match(RbConfig::CONFIG['host_os'])
        @plugins_dir = File.expand_path(File.join(@exeFile, '..', '..', '..', 'plugins'))
      end
    else # Try to find the Elexis exe in our workspace
      puts "Try to find the Elexis exe in our workspace"
      pathname = ''; @exeFile = ''
      case RbConfig::CONFIG['host_os']
        when WINDOWS_REGEXP
        pathname = File.expand_path(File.join('..', '..', '**',@winType, '**', @cpu, "*elexis*.exe"))
        if (Dir.glob(File.expand_path(pathname)).size == 1)
          @exeFile = Dir.glob(File.expand_path(pathname))[0]
          @instDest =  File.dirname(@exeFile)
        end
        when MACOSX_REGEXP
        pathname = File.expand_path("../../*/target/products/*/#{@os}/#{@winType}/#{@cpu}/*app/configuration/config.ini")
        files = Dir.glob(File.expand_path(pathname))
        if (files.size == 1)
          pathname =files[0].sub('.app/Contents/macos/configuration/config.ini', '')
          pathname =pathname.sub('configuration/config.ini', '')
          appName = File.basename(pathname).sub('.app', '')
          @exeFile = File.join(pathname, 'Contents', 'MacOS', appName)
          @instDest =  pathname
        end
      else
        pathname = File.expand_path(File.join(__FILE__, "../../../*site*/target/products/*/#{@os}/#{@winType}/#{@cpu}/configuration/config.ini"))
        if (Dir.glob(File.expand_path(pathname)).size == 1)
          pathname = pathname.sub('configuration/config.ini', '*.ini')
          if (Dir.glob(File.expand_path(pathname)).size == 1)
          @exeFile = Dir.glob(File.expand_path(pathname))[0].sub('.ini', '')
          @instDest =  File.dirname(@exeFile)
          end
        end
      end
      raise "Could not find an exefile using #{pathname}" unless File.exists?(@exeFile)
    end
    @plugins_dir = File.join(@instDest, 'plugins')
    puts "self #{self.inspect}"
    if MACOSX_REGEXP.match(RbConfig::CONFIG['host_os'])
      @plugins_dir = File.expand_path(File.join(@exeFile, '..', '..', '..', 'plugins'))
    end
    puts "exe: #{@exeFile} installer: #{@installer} plugins #{@plugins_dir}"
  end

  def installFromZip
    if @installer
      @installer = File.expand_path(@installer)
      if File.directory?(@instDest)
        puts "Skip unzipping from #{@installer} as #{@instDest} already present"
      else
        saved = Dir.pwd
        FileUtils.makedirs(@instDest)
        raise "No installer #{@installer} found. Cannot unzip" unless File.exists?(@installer)
        Dir.chdir(@instDest) unless @dryRun
        system("unzip -qu #{@installer}")
        Dir.chdir(saved)
      end
    end
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
      puts "should cd #{@plugins_dir} && unzip #{cmd}"
    else
      Dir.chdir(@plugins_dir)
    end
    if WINDOWS_REGEXP.match(RbConfig::CONFIG['host_os'])
      cmd = "#{File.expand_path(File.dirname(__FILE__))}/7z x -y #{cmd} > test-unzip.log"
      cmd.gsub!('\\', '\\\\')
    else 
      cmd = "unzip -qu #{cmd}"
    end
    jubula_jar = File.expand_path("#{@plugins_dir}/org.eclipse.jubula.rc.rcp_*.jar")
    ini_name = File.join(@instDest, 'configuration', 'config.ini')
    if MACOSX_REGEXP.match(RbConfig::CONFIG['host_os'])
      ini_name = File.expand_path(File.join(@exeFile, '..', '..', '..', 'configuration', 'config.ini'))
    end
    if Dir.glob(jubula_jar).size != 1 or File.size(Dir.glob(jubula_jar)[0]) < 4000
      FileUtils.rm_f(Dir.glob(jubula_jar)[0]) if Dir.glob(jubula_jar).size > 0
      system(cmd)
    end
    if @dryRun then puts "prepareRcpSupport: would patch #{ini_name}"; return end
    jubula_jar = File.basename(Dir.glob(jubula_jar)[0])
    if Dir.glob(@exeFile).size == 0
      puts "prepareRcpSupport: Could not find ini_name #{ini_name}. host_os is #{RbConfig::CONFIG['host_os']}"
      exit 1
    end
    FileUtils.chmod(0755, @exeFile, :verbose => true)
    config_ini = IO.readlines(ini_name)
    needsJubulaRcpSupport = true
    #           reference\:file\:org.slf4j.jcl_1.7.2.v20130115-1340.jar@1\:start,
    rcpStart = 'reference\:file\:'+ jubula_jar+'@4\:start,'
    config_ini.each{ 
      |line|
        needsJubulaRcpSupport = false if /^osgi.bundles=/.match(line) and /#{rcpStart}/.match(line)
    }   
    puts "#{File.expand_path(ini_name)}: #{needsJubulaRcpSupport ? ' Will patch to add' : ' Already patched to'} start " + jubula_jar
    if needsJubulaRcpSupport
      FileUtils.cp(ini_name, ini_name + '.bak', :verbose => true);
      found = false
      config_ini.each{ 
        |line|
          if /^osgi.bundles=/.match(line)
            found = true
            puts line if $VERBOSE
            line.sub!(/osgi.bundles=/, 'osgi.bundles='+rcpStart )
            break
          end
      }
      raise "Could not find line osgi.bundle in #{File.expand_path(ini_name)}" unless found
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
    getInfoForExe unless @plugins_dir
    xmlFile = "#{project}_#{version}.xml"
    savedDir = Dir.pwd
    Dir.chdir(File.dirname(__FILE__))
    cmd = "ruby patch_views_pref_persp.rb #{xmlFile} #{@plugins_dir}"
    exit 1 unless system(cmd, false)
    Dir.chdir(File.dirname(__FILE__))
    Dir.chdir(savedDir)
  end
  
  def loadTestcases(xmlFile = "#{File.dirname(__FILE__)}/#{project}_#{version}.xml")
    ["unbound_modules_swt", "unbound_modules_concrete",  "unbound_modules_rcp"].each{ 
      |tcModule|
      tcs = Dir.glob("#{JubulaOptions::jubulaHome}/examples/testCaseLibrary/#{tcModule}_*.xml")
      raise "Should have found exactly 1 one file. Got #{tcs.inspect}" if tcs.size != 1
      system("#{JubulaOptions::jubulaHome}/#{@application}/dbtool -data #{@data} -import #{tcs[0]} #{dbSpec}", @@myFail)
    }
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

  def cleanDemoDb
    demoDb = File.join(Dir.home, 'elexis', 'demoDB')
    FileUtils.rm_rf(demoDb, :verbose => true) if File.exists?(demoDb)
  end

  def cleanup_from_old_runs
    elexis_home = File.join(Dir.home, 'elexis')
    [@testResults, @dataDir, @instDest, elexis_home].each {
      |x|
        FileUtils.rm_rf(File.expand_path(x), :verbose => true, :noop => @dryRun) if File.directory?(x)
    }
    if WINDOWS_REGEXP.match(RbConfig::CONFIG['host_os'])
      puts "TODO: remove elexis configuration from registry"
    elsif MACOSX_REGEXP.match(RbConfig::CONFIG['host_os'])
      system("rm -rf #{File.join(Dir.home, 'Library/Preferences/ch.*elexis*')}")
    else
      java_elexis_setting = File.join(Dir.home, '.java/.userPrefs/ch/elexis')
      FileUtils.rm_rf(java_elexis_setting, :verbose => true) if File.directory?(java_elexis_setting)
    end
    FileUtils.makedirs File.join(Dir.home, 'elexis', 'Eingangsfach')
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
    if @exeFile and not WINDOWS_REGEXP.match(RbConfig::CONFIG['host_os'])
      # killit if it is still alive
      system("ps -ef | grep #{@exeFile}")
      system("ps -ef | grep #{File.basename(@exeFile)}")
      system("killall #{File.basename(@exeFile)}")
    end
    okay
  end

  def run(testsuite=@testsuite)
    autoInstall
    genWrapper
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
    getInfoForExe
    unless @exeFile
      raise "genWrapper: no ExeFile defined";
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
    (Dir.glob("**/*shot*/*.png")+Dir.glob("**/*.log")+Dir.glob("**/*htm")+Dir.glob(File.join(@dataDir, '*.log'))).each{
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
