#!/usr/bin/env ruby
# coding: utf-8
# License: Eclipse Public License 1.0
# Copyright Niklaus Giger, 2011, niklaus.giger@member.fsf.org

require 'optparse'
require 'fileutils'
require "#{File.dirname(__FILE__)}/helpers"

module JubulaOptions

  JubulaOptions::Fields = [
    :application,
    :autid, 
    :auto_xml, 
    :data, 
    :dataDir,
    :dbscheme,
    :dburl,
    :dbuser,
    :dbpw,
    :dryRun,
    :exeFile,
    :installer,
    :instDest,
    :jubulaHome, 
    :kblayout,
    :os,
    :portNumber,
    :project,
    :testResults,
    :server, 
    :testsuite,
    :version,
    :vm,
    :vmargs,
    :workspace,
    :wrapper,
  ]


  # set default value for workspace
  if ENV['WORKSPACE']
    @workspace = ENV['WORKSPACE']
  else
    @workspace ||= Dir.pwd
  end
  ENV['TEST_UDV_WORKSPACE'] = @workspace # pass it to jubula 

  # set default value for os
  require 'rbconfig'

  @cpu = RbConfig::CONFIG['target_cpu']
  case RbConfig::CONFIG['host_os']
    when WINDOWS_REGEXP
      @os='windows'
      @winType = 'win32'
    when /linux/i
      @os = 'linux'
      @winType = 'gtk'
    when /sunos|solaris/i
        # Solaris
    when MACOSX_REGEXP
      @os='macosx'
      @winType = 'cocoa'
    else
    puts "unknown RbConfig::CONFIG['host_os'] #{RbConfig::CONFIG['host_os']}"
    exit 3
  end
  @cpu = 'x86' if /i.86/.match(@cpu) # needed for i486 on Windows/linux-x86

  # default values for remaining variable. Adapt it to your Jubula Installation
  @application ||= 'jubula' # or guidancer
  @autid       ||= "elexis"
  @auto_xml    ||= "auto_install.xml"
  @data        ||= "#{ENV['HOME']}/.jubula"
  @dataDir     ||= "#{@workspace}/test-data"
  
  # If you want to use a mysql-DB, you still must have a working Jubula-installation, which defines
  # a dbscheme "mysql" (via the Preferences..Test..Database menu)
  # Use a ssh-tunnel to connect to remote MySQL installations
  # 1) generate the tunnel: ssh -L 1234:localhost:3306 mysql.server.remote
  # 2) jdbc:mysql://localhost:1234/[database]
  @dbscheme    ||= "mysql"
  @dburl       ||= "jdbc:mysql://localhost:3306/jubula_1_1"
  @dbuser      ||= "elexis"
  @dbpw        ||= "elexisTest"
  @dryRun      ||= false

  version = '7.1.00054'
  ["/opt/jubula_#{version}", "c:/Program Files/jubula_#{version}", "E:/jubula_#{version}", "/Applications/jubula_#{version}", ].each {
     |default|
      if File.exists?(default) # File.directory? chokes under Windows
        @jubulaHome  ||= default
        break
      end
  }
  @kblayout    ||= 'de_DE'
  @portNumber  ||= 60011
  @project     ||= 'ElexisCore'
  @testResults ||= "#{@workspace}/test-results"
  @server      ||= 'localhost'
  @testsuite   ||= 'sample'
  @version     ||= '1.0'
  @vmargs      ||= ""  
  @wrapper     ||= "#{@workspace}/test-runner.bat" # use bat for windows!
  @vm          ||= 'java'
  @exeFile     ||= "#{File.expand_path(File.dirname(__FILE__))}/../product/target/products/ch.elexis.core.application.product/#{@os}/#{@winType}/#{@cpu}/Elexis 3.0"
  host_os = RbConfig::CONFIG['host_os']
  case RbConfig::CONFIG['host_os']
    when WINDOWS_REGEXP
      @exeFile = File.expand_path(@exeFile.sub('/windows/', '/win32/') + '.exe')
    when /linux/i
      @vm = 'java'
    when /sunos|solaris/i
        # Solaris
    when MACOSX_REGEXP
    else
      puts "unknown RbConfig::CONFIG['host_os'] #{RbConfig::CONFIG['host_os']}"
      exit 3
  end
  if Dir.glob(@exeFile).size == 0
    puts "Could not find exeFile #{@exeFile}. host_os is #{host_os}"
    exit 1
  end
  @instDest    ||= File.dirname(@exeFile)
  
  def JubulaOptions::parseArgs
    options = OptionParser.new do |opts|
      opts.banner = %(Usage: #{File.basename($0)} [@options]
	runs a jubula test for elexis. Parameters it the choosen test, eg. sample, FULLTEST, FULLTEST_BROKEN
	Default steps are:
	- Unpack #{@installer} into #{@instDest}
	- Create wrapper script '#{@wrapper}' to pass the desired VM and arguments to the program '#{@exeFile}'
	- Remove '#{@instDest}', #{@dataDir}, #{@testResults} and all content within
	- Load '#{@project}_#{@version}.xml' into the Jubula database
	- Start autagent with port #{@portNumber}
	- Start AUT via wrapper 
	- Run testsuite #{@testsuite}
	- Stop autagent
	Mosts parameters can be overridden on the command line
    )
      opts.on("-n", "--[no-]dry-run", "Don't run commands, just show them") do |v|
	@dryRun = v
      end
      opts.on("--dbscheme dbscheme", "database dbscheme for jubula. Defaults to '#{@dbscheme}'") do |v|
	@dbscheme = v
      end
      opts.on("--dburl dburl", "database URL for jubula. Defaults to '#{@dburl}'") do |v|
	@dburl = v
      end
      opts.on("--dbuser dbuser", "database user for jubula. Defaults to '#{@dbuser}'") do |v|
	@dbuser = v
      end
      opts.on("--dbpw dbpw", "database password for jubula. Defaults to '#{@dbpw}'") do |v|
	@dbpw = v
      end
      opts.on("-e", "--exeFile exeFile", "exeFile to use. Defaults to '#{@exeFile}'") do |v|
	@os = v
      end
      opts.on("--jubulaHome jubulaHome", "Home of Jubula installation. Defaults to '#{@vmargs}'") do |v|
	@jubulaHome = v
      end
      opts.on("-o", "--os os", "os to use. One of linux, macosx, windows. Defaults to '#{@os}'") do |v|
	@os = v
      end
      opts.on("-p", "--portNumber portNumber", "portNumber for autagent to use. Defaults to '#{@portNumber}'") do |v|
  @portNumber = v
      end
      opts.on("--project", "project to use. Defaults to '#{@project}'") do |v|
  @project = v
      end
      opts.on("--projectVersion", "version of project to use. Defaults to '#{@version}'") do |v|
  @version = v
      end
      opts.on("-i", "--installer file_or_http_link", "installer to use (either a file or a http-link). Defaults to '#{@installer}'") do |v|
	@installer = v
      end
      opts.on("-s", "--server server", "server to run. Defaults to '#{@server}'") do |v|
	@server = v
      end
      opts.on("-t", "--testsuite testsuite", "testsuite to run. Defaults to '#{@testsuite}'") do |v|
	@testsuite = v
      end
      opts.on("--vm vm", "Java virtual machine (VM) to use. Defaults to '#{@vm}'") do |v|
	@vm = v
      end
      opts.on("--vmargs vmargs", "Arguments passed to the VM. Defaults to '#{@vmargs}'") do |v|
	@vmargs = v
      end
      opts.on("-h", "--help", "Show this help") do |v|
	puts opts
	exit
      end
      return opts
    end
    options.parse!
  end

  Fields.each { |x| eval( %(
    def JubulaOptions::#{x}
      @#{x}
    end
      )
    ) 
  }
end

if $0 == __FILE__
  opts = JubulaOptions::parseArgs
  opts.parse(ARGV)
end
