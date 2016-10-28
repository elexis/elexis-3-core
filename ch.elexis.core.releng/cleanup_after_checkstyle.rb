#!/usr/bin/env ruby
# Copyright (c) 2005-2015, Niklaus Giger <niklaus.giger@member.fsf.org> and Elexis
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#    N.Giger - initial implementation

# A small utility script to generate missing links after running
# mvn checkstyle:checkstyle-aggregate site:site

require 'fileutils'
files = Dir.glob('**/index.html')
pwd = Dir.pwd
nr_created = 0
nr_existing = 0
files.each do |file|
  Dir.chdir(pwd)
  next if /^target/.match(file)
  expanded = File.expand_path(file)
  parts = file.split('/')
  Dir.chdir(File.join(pwd, 'target/site'))
  cmd = "cd #{pwd}/target/site & ln -s #{File.dirname(expanded)} #{parts[0]}"
  if File.exist?(parts[0])
    nr_existing += 1
    next
  end
  system(cmd)
  nr_created += 1
end
puts "Created #{nr_created} soft links, skipped #{nr_existing} existing files/directories"