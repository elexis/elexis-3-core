#!/usr/bin/env ruby
# ========================================================================
# Copyright (c) 2014 Niklaus Giger iklaus.giger@member.fsf.org
# ------------------------------------------------------------------------
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# and Apache License v2.0 which accompanies this distribution.
# The Eclipse Public License is available at 
# http://www.eclipse.org/legal/epl-v10.html
# The Apache License v2.0 is available at
# http://www.opensource.org/licenses/apache2.0.php
# You may elect to redistribute this code under either of these licenses. 
# ========================================================================
# 

require "find"
require "erb"
require "pathname"
require "fileutils"
require 'set'
require 'pp'

ENV['ROOT'] ||= '/srv/www/download.elexis.info'
root = ENV['ROOT']
nrVersions = 3
root ||= '/srv/www/download.elexis.info'
root = File.expand_path(root)
puts "Cleaning up versions_4_snapshot under #{root}. Preserving #{nrVersions} snapshots"

Dir.glob("#{root}/*").each {
  |dir|
    snapshot_path = "#{dir}/versions_4_snapshot"
    next unless Dir.glob(snapshot_path).size > 0
    
    snapshots = Dir.glob("#{snapshot_path}/*")
    if snapshots.size <= nrVersions
      puts "Skipping als only #{snapshots.size} directories in #{snapshot_path}"
    else
      snapshots.sort.reverse[0..(nrVersions-1)].each{|version| 
                puts "Preserving #{version}"
              }
      snapshots.sort.reverse[(nrVersions-1)..-1].each{ |version| 
                FileUtils.rm_rf(version, :verbose => true)                                               
              }
    end
}
