#!/usr/bin/env ruby
# Copyright (c) 2015-2107 by Niklaus Giger, niklaus.giger@member.fsf.org

# Small helper scripts for the release management of (Med-)Elexis
# I usually just create logical links like
#   ln -s elexis-3-core/ch.elexis.core.releng/repos.rb .
#   ln -s elexis-3-core/ch.elexis.core.releng/for_each.rb .
#
# Usage:
# ------
# ./for_each.rb git log --pretty=oneline release/3.0.4..release/3.0.4 | tee Changelog.3.0.4
# ./for_each.rb "git commit -m '[5507]Update changelog' Changelog"
# ./for_each.rb git tag release/3.1.4
# ./for_each.rb git push --tags --dry-run
# ./for_each.rb /opt/elexis-3/elexis-3-core/ch.elexis.core.releng/update_changelog.rb -f release/3.1.4
# ./for_each.rb ../elexis-3-core/ch.elexis.core.releng/update_changelog.rb -c Changelog.tst -f 'upcoming 3.1.4'


require 'pp'
root = File.expand_path(File.dirname(__FILE__))
@user = 'ngiger'
require File.join(root, 'repos')

unless ARGV.size >= 1
  puts "You must specify a cmd to execute"
  exit 2
end

userCmd = ARGV.join(' ')

puts "Will execute the following cmd in all reposrs:\n#{userCmd}"

Repos.each{
  |repo|
  dir = File.basename(repo).sub('.git','')
  cmd = "cd #{File.join(root, dir)} && #{userCmd}"
  puts cmd
  unless system(cmd)
    puts "Running #{cmd} failed!"
    exit 1
  end
}
