#!/usr/bin/env ruby
# Copyright 2018 by Niklaus Giger <niklaus.giger@member.fsf.org>
#
# Small utility to convert the dump of a demoDB into postgreSQL format
#
# Notes: for converting mysql to postgresq look at https://github.com/dimitri/pgloader. I used it like this
# > sudo apt-get install pgloader
# > pgloader mysql://elexis:elexisTest@localhost/bruno  postgresql://elexis:elexisTest@localhost/unittests
#
# To dump/restore the demoDB use something like the following
# to export the demoDB
# > java -cp /opt/elexis-3.4/os/plugins/*h2*jar  org.h2.tools.Script -url "jdbc:h2:$HOME/elexis/demoDB/db;AUTO_SERVER=TRUE" -user sa  -script dump.sql
# to read the cump
# > java -cp /opt/elexis-3.4/os/plugins/*h2*jar  org.h2.tools.RunScript -url "jdbc:h2:$HOME/elexis/demoDB/db;AUTO_SERVER=TRUE" -user sa  -script dump.sql
#
require 'trollop'
begin
  require 'pry'
rescue LoadError
end
$stdout.sync = true

parser = Trollop::Parser.new do
  version "#{File.basename(__FILE__, '.rb')} (c) 2017 by Niklaus Giger <niklaus.giger@member.fsf.org>"
  banner <<-EOS
#{version}
License: Eclipse Public License 1.0 (EPL)
Useage: #{File.basename(__FILE__)} sql_dump_file_to_read
  convert sql dump from one dialect to another 
EOS
  opt :to_postgresql,   "Convert dump to postgresql format and save it", :type => String, :default => 'converted_postgres.sql', :short => '-P'
  opt :from_h2,   "Assume input is a h2 dump", :type => String, :default => nil, :short => '-h'
end

Options = Trollop::with_standard_exception_handling parser do
  raise Trollop::HelpNeeded if ARGV.empty? # show help screen
  parser.parse ARGV
end

def convert_to_postres
  if  Options[:from_h2_given]
    input_file = Options[:from_h2]
  else
    raise "Not handled options #{Options}"
  end
  content = IO.readlines(input_file) # [0..1000]
  puts "#{Time.now}: read #{content.size} of #{IO.readlines(input_file).size} bytes from #{input_file}"
  u00_or_lf_regex = /(\\[n"])|\\u([\da-f]{4})/

  content.each do |line|
    x  = line.dup
    
    # Handle some cases where postgresql needs other stuff for global setting/creating/modifying tables
    # eg. ALTER TABLE PUBLIC.ARTIKELSTAMM_CH ADD CONSTRAINT PUBLIC.CONSTRAINT_96 PRIMARY KEY(ID);        
    line.sub!(/^SET (.*)/, '-- set \1')
    line.sub!(/^CREATE USER (.*)/i, '-- CREATE USER \1')
    line.sub!(/TABLE PUBLIC\./i, 'TABLE ')
    line.sub!(/CREATE CACHED TABLE (\w+)/, 'DROP TABLE if exists \1; CREATE TABLE \1 ')
    line.sub!(/INSERT INTO PUBLIC\./i, 'INSERT INTO ')
    line.sub!(/CREATE INDEX PUBLIC.(\w+)/o, 'CREATE INDEX if not exists \1')
    line.gsub!(/ALTER TABLE (\w+) ADD CONSTRAINT PUBLIC.(\w+)/i,  'ALTER TABLE \1 DROP CONSTRAINT IF EXISTS \2; ALTER TABLE \1 ADD CONSTRAINT \2')
    
    # Postgres has some other type (see also ch.rgw.utility JdbcLink
    line.gsub!(/\s(LONGVARCHAR|LONGTEXT)([\s,\,])/im, ' TEXT\2')
    line.gsub!(/\sLONGVARBINARY([\s,\,])/im, ' BYTEA\1')
    line.gsub!(/\sSELECTIVITY\s+\d+/im, '')
    

    # Handle quoted characters
    while m = u00_or_lf_regex.match(line)
      char = eval('"' + ( m[0] ? m[0] : m[1]) + '"')
      line.sub!(u00_or_lf_regex, char)
    end

    # e.g. ALTER TABLE PUBLIC.ZUSATZADRESSE ADD CONSTRAINT PUBLIC.FK_ZUSATZADRESSE_KONTAKT_ID FOREIGN KEY(KONTAKT_ID) REFERENCES PUBLIC.KONTAKT(ID) NOCHECK;              
    line.gsub!(/REFERENCES (?:PUBLIC\.|)([\w\(\)]+)(?:\s+NOCHECK|)/i, 'REFERENCES \1')
    line.gsub!(/CREATE FORCE VIEW PUBLIC\./, 'CREATE VIEW ') 

    line.gsub!(/STRINGDECODE((?:(?!STRINGDECODE).)*?'\),)/im, '\1')
    line.gsub!('STRINGDECODE', '')
    
    # Handle content LONGVARBINARY -> bytea, eg. extinfo
    line.gsub!(/, X'([\da-h]*)'/i, ', decode(\'\1\', \'hex\')')
  end
  File.open(Options[:to_postgresql], 'w+') { |f| f.write (content.join("\n")) }
  puts "#{Time.now}: Created #{Options[:to_postgresql]}"
end
convert_to_postres if Options[:to_postgresql_given]
