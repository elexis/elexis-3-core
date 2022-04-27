#!/usr/bin/env ruby

require 'open-uri'

source = File.join(File.dirname(__FILE__), '..', '3lexisFormatterProfile.xml')
puts source
Profile = open(source).read
# Copyright 2011 by Niklaus Giger <niklaus.giger@member.fsf.org>
#
# Based on http://www.peterfriese.de/formatting-your-code-using-the-eclipse-code-formatter/
# Can be compared to https://raw.githubusercontent.com/elexis/elexis-3-core/master/3lexisFormatterProfile.xml
# You should just see the differences based on the syntax (XML versus ini-file)
# and the code here and below which is a simple ruby script
#

# ECLIPSE=`which eclipse`.chomp
ECLIPSE='eclipse'
if ARGV.length == 0
  puts "Missing dir argument. "
  puts "   #{__FILE__} will enforce the ElexisFormatterProfile for all *.java files below"
  exit 2
end

DryRun = false

def system(cmd, mayFail=false)
  puts "cd #{Dir.pwd} && #{cmd} # mayFail #{mayFail}"
  if DryRun then return
  else res =Kernel.system(cmd)
  end
  if !res and !mayFail then
    puts "running #{cmd} #{mayFail} failed"
    exit 2
  end
end

puts "ARGV #{ARGV.inspect} in #{Dir.pwd}"
if ARGV.length == 1 and File.directory?(ARGV[0])
  dir = ARGV.shift
  Dir.chdir(dir)
  files = Dir.glob("**/src/**/*.java")
else
  files = Dir.glob(ARGV)
end
tmpName = 'profile.tmp'
tmpProfile = File.open(tmpName, 'w+') { |f| f.puts Profile }
cmd="#{ECLIPSE} -data '#{Date.today}' -application org.eclipse.jdt.core.JavaCodeFormatter -verbose " +
    "-config #{tmpName} #{files.join(' ')}"
puts cmd
system(cmd)
cmd="git commit -m 'JavaCodeFormatter enforced' #{files.join(' ')}"
system(cmd) # fails if nothing has changed, which is good as jenkins will not push a commit in this case
