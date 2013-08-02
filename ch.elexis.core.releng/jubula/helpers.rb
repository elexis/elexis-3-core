#!/usr/bin/env ruby
# coding: utf-8
# License: Eclipse Public License 1.0
# Copyright Niklaus Giger, 2011, niklaus.giger@member.fsf.org

WINDOWS_REGEXP = /mingw|bccwin|wince|cygwin|mswin32/i
MACOSX_REGEXP  = /macos|darwin/i

def wgetIfNotExists(src)
  puts src
  filename = File.basename(src)
  if src.index('http') == 0
		puts "getting from #{src} -> #{File.expand_path(filename)}"
		return if DryRun
		require 'open-uri'
		writeOut = open(filename, "wb")
		writeOut.write(open(src).read)
		writeOut.close
	return filename
  elsif File.exists?(filename) && !File.exists?(src)
      puts "File #{src} does not exists"
      exit 3
  end
  return src
end

def system(cmd, mayFail=false)
  if WINDOWS_REGEXP.match(RbConfig::CONFIG['host_os'])
  #  cmd.gsub!("/", "\\")
	if cmd[-1..-1].eql?("&")
		cmd = "start "+cmd[0..-2]
	end
  end

  cmd2history =  "date && cd #{Dir.pwd} && #{cmd} # mayFail #{mayFail} #{DryRun ? 'DryRun' : ''}"
  puts cmd2history
  if DryRun then return true
  else res =Kernel.system(cmd)
  end
  if !res and !mayFail then
    puts "running #{cmd} #{mayFail} failed"
    return false
  end
  res
end

def sleep(howManySeconds)
  if DryRun
    puts "sleep #{howManySeconds} # seconds"
  else
    Kernel.sleep(howManySeconds)
  end
end
