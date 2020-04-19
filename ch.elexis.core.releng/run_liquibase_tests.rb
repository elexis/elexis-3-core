#!/usr/bin/env ruby
# Niklaus Giger, April 2020
# Utility script to execute the liquibase setup with different DBs and JDBC driver
# Parameters passed are path to XML file passed to liquibase and
# second whether the database elexis should NOT dropped/create before running the test
# This ruby script will only run, if you had run mvn clean verify before in main directory
# If you want to test newer/other version of JDBC driver, just modify the configs variable below
#
# I placed some very simple liquibase XML file in the directory which just call
# the corresponding files in the JPA directory
# As the database creation and its update are two different steps when starting Elexis
# you must also call this script twice like this
# ./run_liquibase_tests.rb db_create.xml
# ./run_liquibase_tests.rb db_update.xml false
# I had two create these two scripts, as the paths seen from the JPA plugin are different
# than the ones seen in the source checkout

expected_output_is = %(
 INFO run_liquibase_tests.rb: Ran tests with db_update.xml MUST_DROP was false
 INFO run_liquibase_tests.rb: --port=33062 -> Server version: 5.5.5-10.3.22-MariaDB MariaDB Server
 INFO run_liquibase_tests.rb: --port=33062 -> Server version: 8.0.17 Source distribution (Oracle MySQL)
 INFO run_liquibase_tests.rb: Starte Liquibase am So, 19 Apr 2020 17:21:48 MESZ (Version 3.8.5 #42, kompiliert am Thu Jan 09 04:58:55 UTC 2020)
Liquibase Version: 3.8.5

Liquibase Community 3.8.5 by Datical

 INFO run_liquibase_tests.rb: org.h2_1.3.170.jar                      jdbc:h2:///opt/src/elexis-3-core/ch.elexis.core.releng/h2_db;AUTO_SERVER=TRUE: Passed
 INFO run_liquibase_tests.rb: com.mysql.cj_8.0.13.jar                                       jdbc:mysql://192.168.0.70:33062/elexis: Passed
 INFO run_liquibase_tests.rb: com.mysql.cj_8.0.13.jar                                       jdbc:mysql://192.168.0.70:33061/elexis: Passed
 INFO run_liquibase_tests.rb: org.postgresql.jdbc42_42.2.5.jar                          jdbc:postgresql://192.168.0.70:5432/elexis: Passed
 INFO run_liquibase_tests.rb: No errors detected
)

Host = '192.168.0.70'

require 'fileutils'
require 'bundler/inline'
gemfile do
  source 'https://rubygems.org'
  gem 'log4r'
end
require 'log4r' 

unless ARGV.size >= 1
  puts "You must pass as argument the XML to submit to liquibase"
  exit 1
end
# ML_FILE = "elexisdb_master_initial.xml"
XML_FILE = ARGV[0]
MUST_DROP = (ARGV[1] || 'true').to_s.eql?('true')
Logger = Log4r::Logger.new(File.basename(__FILE__))
Logger.outputters << Log4r::StdoutOutputter.new('stdout')
Logger.outputters << Log4r::FileOutputter.new('file', :filename => File.basename(XML_FILE).sub('.xml', '.log'), :trunc=>true)
Logger.info "#{XML_FILE} MUST_DROP is #{MUST_DROP}"
ENV["PGPASSWORD"] = 'elexisTest'
ENV["MYSQL_PWD"] = 'elexisTest'
MYDIR = File.expand_path(File.dirname(__FILE__))
H2DIR = MYDIR + "/h2_db"
@results = {}
@errors = {}

configs = [ # Alle relevante Konfiguration von Elexis
 {
    :username => 'sa',
    :driver => "org.h2.Driver",
    :classpath => "../ch.elexis.core.p2site/target/repository/plugins/org.h2_1.3.170.jar",
    :url => "jdbc:h2://#{H2DIR};AUTO_SERVER=TRUE"
  },
  {
    :username => 'elexis',
    :driver => "com.mysql.jdbc.Driver",
    :classpath => "../ch.elexis.core.p2site/target/repository/plugins/com.mysql.cj_8.0.13.jar", # elexis 3.8
    :url => "jdbc:mysql://#{Host}:33062/elexis" # mariadb 
  },
  {
    :username => 'elexis',
    :driver => "com.mysql.jdbc.Driver",
    :classpath => "../ch.elexis.core.p2site/target/repository/plugins/com.mysql.cj_8.0.13.jar", # elexis 3.8
    :url => "jdbc:mysql://#{Host}:33061/elexis"
  },
  {
    :username => 'elexis',
    :driver => "org.postgresql.Driver",
    :classpath => "../ch.elexis.core.p2site/target/repository/plugins/org.postgresql.jdbc42_42.2.5.jar", # elexis 3.8
    :url => "jdbc:postgresql://#{Host}:5432/elexis"
  },
]

def getPortFromUrl(url)
  port = url.match(/\:(\d+)\//)[1]
end
def cleanPgDbs(url)
  port = getPortFromUrl(url)
  Logger.info "Drop, then create PG database #{url}"
  exit 3 unless system("psql --user=elexis --host=192.168.0.70 --port=#{port} postgres --command 'drop database if exists elexis;'")
  exit 4 unless system("psql --user=elexis --host=192.168.0.70 --port=#{port} postgres --command 'create database elexis;'")
end
def cleanMySQLdbs(url)
  port = getPortFromUrl(url)
  cmd = "mysql --user=elexis --host=192.168.0.70 --port=#{port}  --execute 'drop database if exists elexis; "+
      "create database elexis;'"
  Logger.info cmd
  exit 3 unless system(cmd)
end

def showMySQLtables(url)
  port = getPortFromUrl(url)
  tables= `mysql --user=elexis --host=192.168.0.70 --port=#{port} elexis --execute 'show tables;'`
  Logger.info "Found #{tables.split.size} Tabellen. First 6 are: #{tables.split[0..5].join(' ' )}"
end

def showMySQLVersion(url)
  port = getPortFromUrl(url)
  cmd = "mysql --user=elexis --host=192.168.0.70 --port=#{port} --execute 'SHOW VARIABLES LIKE \"version\";'"
  Logger.info cmd
  exit 3 unless system(cmd)
end


configs.each do | config |
  if MUST_DROP 
    if config[:driver].match(/mysql|mariadb/)
      cleanMySQLdbs(config[:url])
    elsif config[:driver].match(/postgresql/)
      cleanPgDbs(config[:url])
    else
      FileUtils.rm_f(Dir.glob("#{H2DIR}*"), :verbose => true)
    end
  end
  if config[:driver].match(/mysql|mariadb/)
    showMySQLVersion(config[:url])
    showMySQLtables(config[:url])
  end

  pw = config[:driver].match(/postgres|h2/) ? "" : "--password=elexisTest"
  cmd = %(liquibase \
    --driver=#{config[:driver]} \
    --classpath=#{config[:classpath]}  \
    --url='#{config[:url]}' \
    --changeLogFile=#{XML_FILE} \
    --username=#{config[:username]} #{pw} \
    --logLevel=info \
  update
  )
  Logger.info cmd
  status = system(cmd)
  id = sprintf("%-40s%60s", config[:classpath].sub('../ch.elexis.core.p2site/target/repository/plugins/',''), config[:url])
  @results[id] = status ? "Passed" : "Failed"
  info = "cmd using #{XML_FILE} returned #{status} for \n#{cmd}"
  Logger.error info
  unless status
    @errors["#{config[:driver]} #{config[:url]}"] = info
  end
  if config[:driver].match(/mysql|mariadb/)
    showMySQLVersion(config[:url])
    showMySQLtables(config[:url])
  end
end

def report
  Logger.info "Ran tests with #{XML_FILE} MUST_DROP was #{MUST_DROP}"
  Logger.info "--port=33062 -> Server version: 5.5.5-10.3.22-MariaDB MariaDB Server"
  Logger.info "--port=33062 -> Server version: 8.0.17 Source distribution (Oracle MySQL)"
  Logger.info `liquibase --version`
  @results.each do | id, status |
    Logger.info "#{id}: #{status}"
  end
  if @errors.size == 0 
    Logger.info "No errors detected"
  else
    @errors.each do | id, info |
      Logger.error "#{id} #{info}"
    end
  end
end
report
# https://forum.liquibase.org/topic/arrayindexoutofboundsexception-with-liquibase-3-6-2
# JAR von https://downloads.mariadb.org/connector-java/2.5.1/
# <!-- https://forum.liquibase.org/topic/arrayindexoutofboundsexception-with-liquibase-3-6-2 -->

